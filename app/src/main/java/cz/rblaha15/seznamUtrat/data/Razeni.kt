package cz.rblaha15.seznamUtrat.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.ui.graphics.vector.ImageVector
import cz.rblaha15.seznamUtrat.ui.seznam.UtrataVSeznamu

sealed class Razeni(
    val text: String,
    val icon: ImageVector,
    val jeVzestupne: Boolean,
): Comparator<Utrata> {
    data object Datum1 : Razeni(
        text = "Datum (sestupně)",
        icon = Icons.Default.CalendarViewMonth,
        jeVzestupne = false,
    ), Comparator<Utrata> by compareByDescending({ it.datum.atTime(it.cas) })

    data object Datum2 : Razeni(
        text = "Datum (vzestupně)",
        icon = Icons.Default.CalendarViewMonth,
        jeVzestupne = true,
    ), Comparator<Utrata> by compareBy({ it.datum.atTime(it.cas) })

    data object Cena1 : Razeni(
        text = "Cena (sestupně)",
        icon = Icons.Default.Money,
        jeVzestupne = false,
    ), Comparator<Utrata> by compareByDescending({ it.cena })

    data object Cena2 : Razeni(
        text = "Cena (vzestupně)",
        icon = Icons.Default.Money,
        jeVzestupne = true,
    ), Comparator<Utrata> by compareBy({ it.cena })

    val orderIcon get() = if (jeVzestupne) Icons.Default.NorthEast else Icons.Default.SouthEast

    companion object {
        val entries get() = listOf(Datum1, Datum2, Cena1, Cena2)
        fun Razeni.toUtrataVSeznamuComparator() =
            Comparator.comparing<UtrataVSeznamu, _>({ it.toUtrata() }, this)
    }
}