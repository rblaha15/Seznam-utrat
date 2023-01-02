package cz.rblaha15.seznamUtrat

import java.io.OutputStream

interface UtratyRepository {

    var mena: String

    var nazevAkce: String

    var seznamUtrat: List<Utrata>

    var seznamUcastniku: List<Ucastnik>

    fun ulozitSoubor(fileName: String, poVybrani: (OutputStream) -> Unit)
}
