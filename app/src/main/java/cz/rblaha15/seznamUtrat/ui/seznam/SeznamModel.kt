package cz.rblaha15.seznamUtrat.ui.seznam

import cz.rblaha15.seznamUtrat.data.Razeni
import cz.rblaha15.seznamUtrat.data.Ucastnik
import cz.rblaha15.seznamUtrat.data.Utrata
import cz.rblaha15.seznamUtrat.data.serializers.LocalDateSeralizer
import cz.rblaha15.seznamUtrat.data.serializers.LocalTimeSeralizer
import cz.rblaha15.seznamUtrat.data.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed interface SeznamEvent

data class AktualizovatNazevAkce(val nazev: String) : SeznamEvent
data class AktualizovatMenu(val mena: String) : SeznamEvent
data class AktualizovatRazeni(val razeni: Razeni) : SeznamEvent
data class OdstranitUtratu(val id: UUID) : SeznamEvent
data class PridatUtratu(val utrata: Utrata) : SeznamEvent
data class AktualizovatUtratu(val utrata: Utrata) : SeznamEvent
data object VygenerovatPDF : SeznamEvent
data object Resetovat : SeznamEvent

@OptIn(ExperimentalContracts::class)
fun SeznamState.okOrNull(): SeznamState.OK? {
    contract {
        returnsNotNull() implies (this@okOrNull is SeznamState.OK)
        returnsNotNull() implies (this@okOrNull !is SeznamState.Loading)
        returns(null) implies (this@okOrNull !is SeznamState.OK)
        returns(null) implies (this@okOrNull is SeznamState.Loading)
    }
    return this as? SeznamState.OK
}

@OptIn(ExperimentalContracts::class)
fun SeznamState.isOk(): Boolean {
    contract {
        returns(true) implies (this@isOk is SeznamState.OK)
        returns(true) implies (this@isOk !is SeznamState.Loading)
        returns(false) implies (this@isOk !is SeznamState.OK)
        returns(false) implies (this@isOk is SeznamState.Loading)
    }
    return this is SeznamState.OK
}

@OptIn(ExperimentalContracts::class)
fun SeznamState.isLoading(): Boolean {
    contract {
        returns(true) implies (this@isLoading !is SeznamState.OK)
        returns(true) implies (this@isLoading is SeznamState.Loading)
        returns(false) implies (this@isLoading is SeznamState.OK)
        returns(false) implies (this@isLoading !is SeznamState.Loading)
    }
    return this is SeznamState.Loading
}

sealed interface SeznamState {

    data object Loading : SeznamState

    data class OK(
        val mena: String,
        val nazevAkce: String,
        val utraty: List<UtrataVSeznamu>,
        val razeni: Razeni,
        val aktivovaniUcastnici: List<UUID>,
        val suma: Double,
        val ucastnici: List<Ucastnik>,
    ) : SeznamState
}

@Serializable
data class UtrataVSeznamu(
    val datum: @Serializable(with = LocalDateSeralizer::class) LocalDate,
    val cas: @Serializable(with = LocalTimeSeralizer::class) LocalTime,
    val cena: Float,
    val nazev: String,
    val ucastnici: String,
    val ucastniciIds: List<@Serializable(with = UUIDSerializer::class) UUID>,
    val id: @Serializable(with = UUIDSerializer::class) UUID,
) {
    fun toUtrata() = Utrata(
        datum = datum,
        cas = cas,
        cena = cena,
        nazev = nazev,
        ucastnici = ucastniciIds,
        id = id,
    )
}