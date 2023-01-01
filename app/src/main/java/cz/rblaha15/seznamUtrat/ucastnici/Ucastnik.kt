package cz.rblaha15.seznamUtrat.ucastnici

import cz.rblaha15.seznamUtrat.UtratyRepository
import java.util.*

data class Ucastnik(
    val jmeno: String,
    val aktivovan: Boolean = true,
    val id: UUID = UUID.randomUUID(),
) {

    override fun toString(): String {
        return "\nUcastnik(jmeno=$jmeno)"
    }

    fun utraty(repo: UtratyRepository) =
        repo.seznamUtrat.filter { id in it.ucastnici }
}