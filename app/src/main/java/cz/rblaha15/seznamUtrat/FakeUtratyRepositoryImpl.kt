package cz.rblaha15.seznamUtrat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.OutputStream

class FakeUtratyRepositoryImpl : UtratyRepository {

    private val _mena = MutableStateFlow("Kč")
    override val mena: Flow<String> = _mena.asStateFlow()
    override fun mena(mena: String) { _mena.value = mena }

    private val _nazevAkce = MutableStateFlow("")
    override val nazevAkce: Flow<String> = _nazevAkce.asStateFlow()
    override fun nazevAkce(nazevAkce: String) { _nazevAkce.value = nazevAkce }

    private val _seznamUcastniku = MutableStateFlow(emptyList<Ucastnik>())
    override val seznamUcastniku: Flow<List<Ucastnik>> = _seznamUcastniku.asStateFlow()
    override fun seznamUcastniku(seznamUcastniku: List<Ucastnik>) { _seznamUcastniku.value = seznamUcastniku }

    private val _seznamUtrat = MutableStateFlow(emptyList<Utrata>())
    override val seznamUtrat: Flow<List<Utrata>> = _seznamUtrat.asStateFlow()
    override fun seznamUtrat(seznamUtrat: List<Utrata>) { _seznamUtrat.value = seznamUtrat }


    override fun ulozitSoubor(fileName: String, poVybrani: (OutputStream) -> Unit) {

    }
}