package cz.rblaha15.seznamUtrat.utraty

import cz.rblaha15.seznamUtrat.UtratyRepository
import java.util.*

data class Utrata(
    var datum: String,
    var cas: String,
    var cena: Float,
    var nazev: String,
    var ucastnici: List<UUID>,
    val id: UUID = UUID.randomUUID(),
) {
    override fun toString(): String {
        return "\nUtrata(datum=\"$datum\", cena=$cena, nazev=\"$nazev\", ucastnici=$ucastnici)"
    }

    fun ucastnici(repo: UtratyRepository) =
        ucastnici.map { uuid -> repo.seznamUcastniku.first { it.id == uuid } }
}