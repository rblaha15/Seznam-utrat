package cz.rblaha15.seznamUtrat

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream

class UtratyRepositoryImpl(private val ctx: ComponentActivity) : UtratyRepository {

    private val prefs = PreferenceDataStoreFactory.create(
        migrations = listOf(
            SharedPreferencesMigration(
                produceSharedPreferences = {
                    ctx.getSharedPreferences("PREFS_SEZNAM_UTRAT_RBLAHA15", Context.MODE_PRIVATE)
                }
            )
        )
    ) {
        ctx.preferencesDataStoreFile("PREFS_SEZNAM_UTRAT_RBLAHA15")
    }

    object Keys {
        val NAZEV_AKCE = stringPreferencesKey("nazevAkce")
        val MENA = stringPreferencesKey("mena")
        val UTRATY = stringPreferencesKey("seznamUtrat")
        val UCASTNICI = stringPreferencesKey("seznamUcastniku")
    }

    override val mena: Flow<String> = prefs.data.map {
        it[Keys.MENA] ?: "Kč"
    }

    override fun mena(mena: String) {
        ctx.lifecycleScope.launch {
            prefs.edit {
                it[Keys.MENA] = mena
            }
        }
    }

    override val nazevAkce: Flow<String> = prefs.data.map {
        it[Keys.NAZEV_AKCE] ?: ""
    }

    override fun nazevAkce(nazevAkce: String) {
        ctx.lifecycleScope.launch {
            prefs.edit {
                it[Keys.NAZEV_AKCE] = nazevAkce
            }
        }
    }

    override val seznamUtrat: Flow<List<Utrata>> = prefs.data.map {
        it[Keys.UTRATY]?.fromJson() ?: emptyList()
    }

    override fun seznamUtrat(seznamUtrat: List<Utrata>) {
        ctx.lifecycleScope.launch {
            prefs.edit {
                it[Keys.UTRATY] = seznamUtrat.toJson()
            }
        }
    }

    override val seznamUcastniku: Flow<List<Ucastnik>> = prefs.data.map {
        it[Keys.UCASTNICI]?.fromJson() ?: emptyList()
    }

    override fun seznamUcastniku(seznamUcastniku: List<Ucastnik>) {
        ctx.lifecycleScope.launch {
            prefs.edit {
                it[Keys.UCASTNICI] = seznamUcastniku.toJson()
            }
        }
    }

    private var poVybrani: (OutputStream) -> Unit = {}

    private val activityResultLauncher =
        ctx.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data!!.data!!
                ctx.contentResolver.openOutputStream(uri)!!.use(poVybrani)
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

inline fun <reified T> String.fromJson(): T = Json.decodeFromString(this)
inline fun <reified T> T.toJson(): String = Json.encodeToString(this)
