package cz.rblaha15.seznamUtrat

import android.app.Activity
import cz.rblaha15.seznamUtrat.ucastnici.Ucastnik
import cz.rblaha15.seznamUtrat.utraty.Utrata
import java.io.OutputStream

class FakeUtratyRepositoryImpl : UtratyRepository {

    private var nazevAkceField: String = ""
    override var nazevAkce: String
        get() = nazevAkceField
        set(value) {
            nazevAkceField = value
        }
    private var menaField: String = ""
    override var mena: String
        get() = menaField
        set(value) {
            menaField = value
        }
    private var seznamUtratField: List<Utrata> = emptyList()
    override var seznamUtrat: List<Utrata>
        get() = seznamUtratField
        set(value) {
            seznamUtratField = value
        }
    private var seznamUcastnikuField: List<Ucastnik> = emptyList()
    override var seznamUcastniku: List<Ucastnik>
        get() = seznamUcastnikuField
        set(value) {
            seznamUcastnikuField = value
        }

    override fun <T : Activity> startActivity(activity: Class<T>) {

    }

    override fun ulozitSoubor(fileName: String, poVybrani: (OutputStream) -> Unit) {

    }
}