package cz.rblaha15.seznamUtrat.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalDataSourceImpl(private val dataStore: DataStore<Preferences>) : LocalDataSource {

    object Keys {
        val NAZEV_AKCE = stringPreferencesKey("nazevAkce")
        val MENA = stringPreferencesKey("mena")
        val UTRATY = stringPreferencesKey("seznamUtrat")
        val UCASTNICI = stringPreferencesKey("seznamUcastniku")
    }

    override val mena: Flow<String> = dataStore.data.map {
        it[Keys.MENA] ?: "Kč"
    }

    override suspend fun mena(mena: String) {
        dataStore.edit {
            it[Keys.MENA] = mena
        }
    }

    override val nazevAkce: Flow<String> = dataStore.data.map {
        it[Keys.NAZEV_AKCE] ?: ""
    }

    override suspend fun nazevAkce(nazevAkce: String) {
        dataStore.edit {
            it[Keys.NAZEV_AKCE] = nazevAkce
        }
    }

    override val seznamUtrat: Flow<List<Utrata>> = dataStore.data.map {
        it[Keys.UTRATY]?.fromJson() ?: emptyList()
    }

    override suspend fun upravitSeznamUtrat(edit: MutableList<Utrata>.() -> Unit) {
        dataStore.edit {
            it[Keys.UTRATY] = it[Keys.UTRATY]?.fromJson<List<Utrata>>().orEmpty().toMutableList().apply(edit).toJson()
        }
    }

    override val seznamUcastniku: Flow<List<Ucastnik>> = dataStore.data.map {
        it[Keys.UCASTNICI]?.fromJson() ?: emptyList()
    }

    override suspend fun upravitSeznamUcastniku(edit: MutableList<Ucastnik>.() -> Unit) {
        dataStore.edit {
            it[Keys.UCASTNICI] = it[Keys.UCASTNICI]?.fromJson<List<Ucastnik>>().orEmpty().toMutableList().apply(edit).toJson()
        }
    }
}
