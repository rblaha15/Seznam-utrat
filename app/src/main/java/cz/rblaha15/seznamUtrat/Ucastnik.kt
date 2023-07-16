package cz.rblaha15.seznamUtrat

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Ucastnik(
    val jmeno: String,
    val aktivovan: Boolean = true,
    val id: @Serializable(with = UUIDSerializer::class) UUID = UUID.randomUUID(),
)

fun List<Utrata>.cloveka(id: UUID) = filter { id in it.ucastnici }