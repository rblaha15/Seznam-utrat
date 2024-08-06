package cz.rblaha15.seznamUtrat.data

import cz.rblaha15.seznamUtrat.data.serializers.LocalDateSeralizer
import cz.rblaha15.seznamUtrat.data.serializers.LocalTimeSeralizer
import cz.rblaha15.seznamUtrat.data.serializers.UUIDSerializer
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