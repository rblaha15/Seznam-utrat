package cz.rblaha15.seznamUtrat

import kotlinx.coroutines.flow.Flow
import java.io.OutputStream

interface UtratyRepository {

    val mena: Flow<String>
    fun mena(mena: String)

    val nazevAkce: Flow<String>
    fun nazevAkce(nazevAkce: String)

    val seznamUtrat: Flow<List<Utrata>>
    fun seznamUtrat(seznamUtrat: List<Utrata>)

    val seznamUcastniku: Flow<List<Ucastnik>>
    fun seznamUcastniku(seznamUcastniku: List<Ucastnik>)

    fun ulozitSoubor(fileName: String, poVybrani: (OutputStream) -> Unit)
}
