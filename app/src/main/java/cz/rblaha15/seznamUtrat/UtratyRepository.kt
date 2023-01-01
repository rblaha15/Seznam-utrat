package cz.rblaha15.seznamUtrat

import android.app.Activity
import cz.rblaha15.seznamUtrat.ucastnici.Ucastnik
import cz.rblaha15.seznamUtrat.utraty.Utrata
import java.io.OutputStream

interface UtratyRepository {

    var mena: String

    var nazevAkce: String

    var seznamUtrat: List<Utrata>

    var seznamUcastniku: List<Ucastnik>

    fun <T : Activity> startActivity(activity: Class<T>)

    fun ulozitSoubor(fileName: String, poVybrani: (OutputStream) -> Unit)
}
