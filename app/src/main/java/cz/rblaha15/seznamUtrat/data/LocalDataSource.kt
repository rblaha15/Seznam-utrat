package cz.rblaha15.seznamUtrat.data

import kotlinx.coroutines.flow.Flow

interface LocalDataSource {

    val mena: Flow<String>
    suspend fun mena(mena: String)

    val nazevAkce: Flow<String>
    suspend fun nazevAkce(nazevAkce: String)

    val seznamUtrat: Flow<List<Utrata>>
    suspend fun upravitSeznamUtrat(edit: MutableList<Utrata>.() -> Unit)

    val seznamUcastniku: Flow<List<Ucastnik>>
    suspend fun upravitSeznamUcastniku(edit: MutableList<Ucastnik>.() -> Unit)
}
