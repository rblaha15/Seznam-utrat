package cz.rblaha15.seznamUtrat.ui.ucastnici

import java.util.UUID

sealed interface UcastniciEvent

data object PridatUcastnika : UcastniciEvent
data class AktualizovatJmeno(val uuid: UUID, val jmeno: String) : UcastniciEvent
data class AktualizovatAktivovanost(val uuid: UUID, val aktivovan: Boolean) : UcastniciEvent
data class OdstranitUcastnika(val uuid: UUID) : UcastniciEvent

sealed interface UcastniciState {
    data object Loading : UcastniciState

    data class OK(
        val ucastnici: List<UcastnikVSeznamu>,
        val mena: String,
    ) : UcastniciState
}

data class UcastnikVSeznamu(
    val jmeno: String,
    val aktivovan: Boolean,
    val uuid: UUID,
    val utrata: Double,
    val muzemeOdstranit: Boolean,
)
