package cz.rblaha15.seznamUtrat.ui.ucastnici

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.rblaha15.seznamUtrat.data.LocalDataSource
import cz.rblaha15.seznamUtrat.data.Ucastnik
import cz.rblaha15.seznamUtrat.data.cloveka
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class UcastniciViewModel(
    private val dataSource: LocalDataSource,
) : ViewModel() {

    fun onEvent(e: UcastniciEvent) = viewModelScope.launch {
        dataSource.upravitSeznamUcastniku {
            when(e) {
                PridatUcastnika -> {
                    add(Ucastnik(jmeno = "", aktivovan = true))
                }
                is AktualizovatAktivovanost -> {
                    val i = indexOfFirst { it.id == e.uuid }
                    this[i] = this[i].copy(aktivovan = e.aktivovan)
                }
                is AktualizovatJmeno -> {
                    val i = indexOfFirst { it.id == e.uuid }
                    this[i] = this[i].copy(jmeno = e.jmeno)
                }
                is OdstranitUcastnika -> {
                    removeIf { it.id == e.uuid }
                }
            }
        }
    }

    val state: StateFlow<UcastniciState> = combine(dataSource.seznamUcastniku, dataSource.seznamUtrat, dataSource.mena) { ucastnici, utraty, mena ->
        UcastniciState.OK(
            ucastnici = ucastnici.map { ucastnik ->
                UcastnikVSeznamu(
                    jmeno = ucastnik.jmeno,
                    aktivovan = ucastnik.aktivovan,
                    uuid = ucastnik.id,
                    utrata = utraty.cloveka(ucastnik.id).sumOf { it.cena.toDouble() / it.ucastnici.size },
                    muzemeOdstranit = utraty.cloveka(ucastnik.id).isEmpty()
                )
            },
            mena = mena
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), UcastniciState.Loading)
}