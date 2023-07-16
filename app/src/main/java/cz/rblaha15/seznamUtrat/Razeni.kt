package cz.rblaha15.seznamUtrat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDateTime
import java.util.*
import kotlin.Comparator

enum class Razeni(
    val text: String,
    val leadingIcon: ImageVector,
    val trailingIcon: ImageVector,
    val razeni: Comparator<Utrata>,
) {
    Datum1(
        text = "Datum (sestupně)",
        leadingIcon = Icons.Default.CalendarViewMonth,
        trailingIcon = Icons.Default.SouthEast,
        razeni = compareByDescending { it.datum.atTime(it.cas) }
    ),
    Datum2(
        text = "Datum (vzestupně)",
        leadingIcon = Icons.Default.CalendarViewMonth,
        trailingIcon = Icons.Default.NorthEast,
        razeni = compareBy { it.datum.atTime(it.cas) }
    ),
    Cena1(
        text = "Cena (sestupně)",
        leadingIcon = Icons.Default.Money,
        trailingIcon = Icons.Default.SouthEast,
        razeni = compareByDescending { it.cena }
    ),
    Cena2(
        text = "Cena (vzestupně)",
        leadingIcon = Icons.Default.Money,
        trailingIcon = Icons.Default.NorthEast,
        razeni = compareBy { it.cena }
    ),
}