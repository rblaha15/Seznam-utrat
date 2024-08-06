package cz.rblaha15.seznamUtrat.ui.seznam

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.rblaha15.seznamUtrat.GenericActivityResultLauncher
import cz.rblaha15.seznamUtrat.data.LocalDataSource
import cz.rblaha15.seznamUtrat.data.Razeni
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.OutputStream
import kotlin.time.Duration.Companion.seconds

class SeznamViewModel(
    private val dataSource: LocalDataSource,
    private val launcher: GenericActivityResultLauncher<String, Uri?>,
    private val useOutputStream: Uri.((OutputStream) -> Unit) -> Unit,
) : ViewModel() {

    private val razeni = MutableStateFlow<Razeni>(Razeni.Datum1)

    fun onEvent(e: SeznamEvent) = viewModelScope.launch {
        when (e) {
            is AktualizovatMenu -> dataSource.mena(e.mena)
            is AktualizovatNazevAkce -> dataSource.nazevAkce(e.nazev)
            is AktualizovatRazeni -> razeni.value = e.razeni
            is AktualizovatUtratu -> dataSource.upravitSeznamUtrat {
                this[indexOfFirst { it.id == e.utrata.id }] = e.utrata
            }

            is OdstranitUtratu -> dataSource.upravitSeznamUtrat {
                removeIf { it.id == e.id }
            }

            is PridatUtratu -> dataSource.upravitSeznamUtrat {
                add(e.utrata)
            }

            Resetovat -> {
                dataSource.upravitSeznamUcastniku {
                    clear()
                }
                dataSource.upravitSeznamUtrat {
                    clear()
                }
                dataSource.mena("")
                dataSource.nazevAkce("")
            }

            VygenerovatPDF -> vygenerovatPDF(
                dataSource.seznamUcastniku.first(),
                dataSource.seznamUtrat.first(),
                dataSource.mena.first(),
                dataSource.nazevAkce.first(),
                launcher,
                useOutputStream,
            )
        }
    }

    val state: StateFlow<SeznamState> =
        combine(dataSource.seznamUcastniku, dataSource.seznamUtrat, dataSource.mena, dataSource.nazevAkce, razeni) { ucastnici, utraty, mena, nazevAkce, razeni ->
            SeznamState.OK(
                ucastnici = ucastnici,
                mena = mena,
                nazevAkce = nazevAkce,
                aktivovaniUcastnici = ucastnici.filter { it.aktivovan }.map { it.id },
                razeni = razeni,
                suma = utraty.sumOf { it.cena.toDouble() },
                utraty = utraty.map { utrata ->
                    UtrataVSeznamu(
                        id = utrata.id,
                        nazev = utrata.nazev,
                        cena = utrata.cena,
                        datum = utrata.datum,
                        cas = utrata.cas,
                        ucastniciIds = utrata.ucastnici,
                        ucastnici = ucastnici.filter { it.id in utrata.ucastnici }.let { filtered ->
                            when {
                                ucastnici.all { it in filtered } -> ""
                                filtered.isEmpty() -> "Žádný účastník"
                                else -> filtered.joinToString { it.jmeno }
                            }
                        },
                    )
                },
            )
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), SeznamState.Loading)
}