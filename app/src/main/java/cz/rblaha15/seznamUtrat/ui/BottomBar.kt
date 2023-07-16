package cz.rblaha15.seznamUtrat.ui

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.rblaha15.seznamUtrat.Razeni
import cz.rblaha15.seznamUtrat.UtratyRepository
import cz.rblaha15.seznamUtrat.asString
import cz.rblaha15.seznamUtrat.cloveka
import cz.rblaha15.seznamUtrat.toString

const val PAGE_WIDTH = 595 // PostScripts (1/72th of an inch)
const val PAGE_HEIGHT = 842 // PostScripts (1/72th of an inch)
const val PRINTABLE_WIDTH = 482 // PostScripts (1/72th of an inch)
const val PRINTABLE_HEIGHT = 700 // PostScripts (1/72th of an inch)
const val VERTICAL_PADDING = (PAGE_HEIGHT - PRINTABLE_HEIGHT) / 2 // PostScripts (1/72th of an inch)
const val HORIZONTAL_PADDING = (PAGE_WIDTH - PRINTABLE_WIDTH) / 2 // PostScripts (1/72th of an inch)

@Composable
fun BottomBar(
    nastavitRazeni: (Razeni) -> Unit,
    otevritDialog: () -> Unit,
    repo: UtratyRepository,
) = Column {
    val seznamUtrat by repo.seznamUtrat.collectAsState(emptyList())
    val seznamUcastniku by repo.seznamUcastniku.collectAsState(emptyList())
    val mena by repo.mena.collectAsState("")
    val nazevAkce by repo.nazevAkce.collectAsState("")

    Text(
        text = "Celkem utraceno: ${seznamUtrat.sumOf { it.cena.toDouble() }.toString(2)} $mena",
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        style = MaterialTheme.typography.bodyLarge
    )

    BottomAppBar(
        actions = {
            var menuRazeni by rememberSaveable { mutableStateOf(false) }
            DropdownMenu(
                expanded = menuRazeni,
                onDismissRequest = {
                    menuRazeni = false
                }
            ) {
                Razeni.values().forEach {
                    DropdownMenuItem(
                        text = { Text(text = it.text) },
                        onClick = {
                            nastavitRazeni(it)
                            menuRazeni = false
                        },
                        leadingIcon = { Icon(it.leadingIcon, null) },
                        trailingIcon = { Icon(it.trailingIcon, null) }
                    )
                }
            }
            IconButton(
                onClick = {
                    menuRazeni = true
                }
            ) {
                Icon(Icons.Default.Sort, "Seřadit")
            }
            var menuMena by rememberSaveable { mutableStateOf(false) }
            DropdownMenu(
                expanded = menuMena,
                onDismissRequest = {
                    menuMena = false
                }
            ) {
                listOf(
                    "Kč",
                    "€",
                    "$",
                ).forEach {
                    DropdownMenuItem(
                        text = { Text(text = it) },
                        onClick = {
                            repo.mena(it)
                            menuMena = false
                        },
                        leadingIcon = {
                            if (mena == it)
                                Icon(Icons.Default.Check, null)
                        }
                    )
                }
            }
            IconButton(
                onClick = {
                    menuMena = true
                }
            ) {
                Icon(Icons.Default.AttachMoney, "Změnit měnu")
            }
            IconButton(
                onClick = {
                    repo.ulozitSoubor(nazevAkce) { outputStream ->
                        val text = """
                            |${nazevAkce}
                            |
                            |Celkem utraceno: ${
                            seznamUtrat.sumOf { it.cena.toDouble() }.toString(2)
                        } $mena
                            |
                            |Útraty za jednotlivé účastníky:
                            ${
                            seznamUcastniku
                                .sortedByDescending { 
                                    seznamUtrat.cloveka(it.id).sumOf { utrata -> utrata.cena.toDouble() }
                                }
                                .joinToString("\n") { ucastnik ->
                                    "|    ${ucastnik.jmeno} – ${
                                        seznamUtrat.cloveka(ucastnik.id).sumOf { (it.cena.toDouble() / it.ucastnici.size) }
                                    } $mena"
                                }
                            }
                            |
                            |Seznam útrat:
                            ${
                            seznamUtrat.sortedWith(Razeni.Datum2.razeni)
                                .joinToString("\n") { utrata ->
                                    val ucastnici =
                                        if (utrata.ucastnici != seznamUcastniku.map { it.id })
                                            " – pouze ${
                                                seznamUcastniku.filter { it.id in utrata.ucastnici }
                                                    .joinToString { it.jmeno }
                                            }"
                                        else ""

                                    with(utrata) {
                                        "|    ${datum.asString()} – ${
                                            cena.toDouble().toString(2)
                                        } $mena – $nazev$ucastnici"
                                    }
                                }
                        }
                            |
                            """.trimMargin(marginPrefix = "|")

                        var pageNumber = 1

                        val pdfDocument = PdfDocument()
                        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        var page = pdfDocument.startPage(pageInfo)

                        val paint = Paint()
                        val capitalPaint = Paint().apply {
                            textSize = 20F

                        }
                        val x = HORIZONTAL_PADDING.toFloat()
                        var y = VERTICAL_PADDING.toFloat()

                        text.split("\n").forEachIndexed { index, line ->
                            val localPaint = if (index == 0 || index == 2) capitalPaint else paint

                            if (line.isEmpty()) {
                                page.canvas.drawText(line, x, y, localPaint)
                                y += localPaint.descent() - localPaint.ascent()
                                return@forEachIndexed
                            }

                            var segment = line

                            while (segment.isNotEmpty()) { // dokud zbývá text

                                var endIndex =
                                    localPaint.breakText(segment, true, PRINTABLE_WIDTH.toFloat(), null) // kam už budeme psát

                                if (endIndex < segment.lastIndex) { // pokud něco zbyde, tak ať to je rozdělený na mezeře
                                    segment.substring(0, endIndex).lastIndexOf(" ").let {
                                        if (it != -1) endIndex = it
                                    }
                                }

                                page.canvas.drawText(
                                    segment.substring(0, endIndex),
                                    x,
                                    y,
                                    localPaint
                                ) // namalovat

                                segment = segment.substring(endIndex)
                                    .removePrefix(" ") // budeme malovat zbytek

                                y += localPaint.descent() - localPaint.ascent()
                                if (y >= PAGE_WIDTH - HORIZONTAL_PADDING) {

                                    pdfDocument.finishPage(page)

                                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber++).create()
                                    page = pdfDocument.startPage(pageInfo)
                                    y = VERTICAL_PADDING.toFloat()
                                }
                            }
                        }

                        pdfDocument.finishPage(page)

                        try {
                            pdfDocument.writeTo(outputStream)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        pdfDocument.close()
                    }
                }
            ) {
                Icon(Icons.Default.Download, "Stáhnout seznam")
            }
            var dialogOdstranit by rememberSaveable { mutableStateOf(false) }
            if (dialogOdstranit) AlertDialog(
                onDismissRequest = {
                    dialogOdstranit = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            repo.seznamUtrat(emptyList())
                            repo.seznamUcastniku(emptyList())
                            repo.nazevAkce("")
                            dialogOdstranit = false
                        }
                    ) {
                        Text(text = "Ano, odstranit vše!")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            dialogOdstranit = false
                        }
                    ) {
                        Text(text = "Zrušit")
                    }
                },
                title = {
                    Text("Odstranit vše?")
                },
                text = {
                    Text("Opravdu chcete odstranit všechna data? Tato akce je nevratná!")
                },
            )
            IconButton(
                onClick = {
                    dialogOdstranit = true
                }
            ) {
                Icon(Icons.Default.DeleteForever, "Odstranit vše")
            }
        },
        floatingActionButton = {
            Box {
                var opravduNaNikoho by rememberSaveable { mutableStateOf(false) }
                FloatingActionButton(
                    onClick = {
                        if (seznamUcastniku.isEmpty())
                            opravduNaNikoho = true
                        else
                            otevritDialog()
                    },
                ) {
                    Icon(Icons.Default.AddCircle, "Přidat útratu")
                }
                if (opravduNaNikoho) AlertDialog(
                    onDismissRequest = {
                        opravduNaNikoho = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                opravduNaNikoho = false
                                otevritDialog()
                            }
                        ) {
                            Text(text = "Ano, pokračovat")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                opravduNaNikoho = false
                            }
                        ) {
                            Text(text = "Ne")
                        }
                    },
                    title = {
                        Text("Žádní účastníci")
                    },
                    text = {
                        Text("Opravdu chcete přidat útratu bez účastníků?")
                    },
                )
            }
        }
    )
}