package cz.rblaha15.seznamUtrat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.ui.graphics.vector.ImageVector
import cz.rblaha15.seznamUtrat.utraty.Utrata
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
        razeni = compareByDescending {
            val c = Calendar.getInstance()
            c.set(Calendar.MONTH, it.datum.split(".")[1].replace(" ", "").replace(".", "").toInt())
            c.set(Calendar.DAY_OF_MONTH, it.datum.split(".")[0].replace(" ", "").replace(".", "").toInt())
            c.set(Calendar.HOUR_OF_DAY, it.cas.split(":")[0].replace(" ", "").replace(".", "").toInt())
            c.set(Calendar.MINUTE, it.cas.split(":")[1].replace(" ", "").replace(".", "").toInt())
            c.set(Calendar.SECOND, it.cas.split(":")[2].replace(" ", "").replace(".", "").toInt())
            c
        }
    ),
    Datum2(
        text = "Datum (vzestupně)",
        leadingIcon = Icons.Default.CalendarViewMonth,
        trailingIcon = Icons.Default.NorthEast,
        razeni = compareBy<Utrata> {
            val c = Calendar.getInstance()
            c.set(Calendar.MONTH, it.datum.split(".")[1].replace(" ", "").replace(".", "").toInt())
            c.set(Calendar.DAY_OF_MONTH, it.datum.split(".")[0].replace(" ", "").replace(".", "").toInt())
            c.set(Calendar.HOUR_OF_DAY, it.cas.split(":")[0].replace(" ", "").replace(".", "").toInt())
            c.set(Calendar.MINUTE, it.cas.split(":")[1].replace(" ", "").replace(".", "").toInt())
            c.set(Calendar.SECOND, it.cas.split(":")[2].replace(" ", "").replace(".", "").toInt())
            c
        }
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