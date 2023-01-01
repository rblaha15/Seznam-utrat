package cz.rblaha15.seznamUtrat

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.rblaha15.seznamUtrat.ucastnici.Ucastnik
import cz.rblaha15.seznamUtrat.utraty.Utrata
import java.io.OutputStream

class UtratyRepositoryImpl(private val ctx: ComponentActivity) : UtratyRepository {

    private val prefs = ctx.getSharedPreferences("PREFS_SEZNAM_UTRAT_RBLAHA15", Context.MODE_PRIVATE)

    private lateinit var nazevAkceField: MutableState<String>
    override var nazevAkce: String
        get() {
            if (!::nazevAkceField.isInitialized)
                nazevAkceField = mutableStateOf(prefs.getString("nazevAkce", "") ?: "")
            return nazevAkceField.value
        }
        set(value) {
            nazevAkceField.value = value
            prefs.edit { putString("nazevAkce", nazevAkceField.value) }
        }
    private lateinit var menaField: MutableState<String>
    override var mena: String
        get() {
            if (!::menaField.isInitialized)
                menaField = mutableStateOf(prefs.getString("mena", "Kč") ?: "Kč")
            return menaField.value
        }
        set(value) {
            menaField.value = value
            prefs.edit { putString("mena", menaField.value) }
        }
    private lateinit var seznamUtratField: MutableState<List<Utrata>>
    override var seznamUtrat: List<Utrata>
        get() {
            if (!::seznamUtratField.isInitialized)
                seznamUtratField = mutableStateOf(Gson().fromJson(prefs.getString("seznamUtrat", "[]"), object : TypeToken<List<Utrata>>() {}.type) ?: emptyList())
            return seznamUtratField.value
        }
        set(value) {
            seznamUtratField.value = value
            prefs.edit { putString("seznamUtrat", Gson().toJson(seznamUtratField.value)) }
        }
    private lateinit var seznamUcastnikuField: MutableState<List<Ucastnik>>
    override var seznamUcastniku: List<Ucastnik>
        get() {
            if (!::seznamUcastnikuField.isInitialized)
                seznamUcastnikuField = mutableStateOf(Gson().fromJson(prefs.getString("seznamUcastniku", "[]"), object : TypeToken<List<Ucastnik>>() {}.type) ?: emptyList())
            println(seznamUcastnikuField.value)
            return seznamUcastnikuField.value
        }
        set(value) {
            seznamUcastnikuField.value = value
            prefs.edit { putString("seznamUcastniku", Gson().toJson(seznamUcastnikuField.value)) }
        }

    override fun <T : Activity> startActivity(activity: Class<T>) {
        val i = Intent(ctx, activity)
        ctx.startActivity(i)
    }

    private var poVybrani by mutableStateOf<(OutputStream) -> Unit>({})

    private val activityResultLauncher =
        ctx.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data!!.data!!
                poVybrani(ctx.contentResolver.openOutputStream(uri)!!)
            }
        }

    override fun ulozitSoubor(fileName: String, poVybrani: (OutputStream) -> Unit) {

        this.poVybrani = poVybrani

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = """application/pdf"""
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        activityResultLauncher.launch(intent)
    }
}
