package cz.rblaha15.seznamUtrat

import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Serializable
data class Utrata(
    var datum: @Serializable(with = LocalDateSeralizer::class) LocalDate,
    var cas: @Serializable(with = LocalTimeSeralizer::class) LocalTime,
    var cena: Float,
    var nazev: String,
    var ucastnici: List<@Serializable(with = UUIDSerializer::class) UUID>,
    val id: @Serializable(with = UUIDSerializer::class) UUID = UUID.randomUUID(),
)

context(UtratyRepository)
fun Utrata.ucastnici() =
    seznamUcastniku.map { seznamUcastniku ->
        ucastnici.mapNotNull { uuid -> seznamUcastniku.find { it.id == uuid } }
    }

fun Utrata.Companion.nova(seznamUcastniku: List<Ucastnik>) = Utrata(
    datum = LocalDate.now(),
    cas = LocalTime.now(),
    cena = 0F,
    nazev = "",
    ucastnici = seznamUcastniku.filter { it.aktivovan }.map { it.id },
)