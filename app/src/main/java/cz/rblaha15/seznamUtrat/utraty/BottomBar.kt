package cz.rblaha15.seznamUtrat.utraty

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.rblaha15.seznamUtrat.Razeni
import cz.rblaha15.seznamUtrat.UtratyRepository
import java.util.*

@Composable
fun BottomBar(
    nastavitRazeni: (Razeni) -> Unit,
    repo: UtratyRepository,
) = Column {
    Text(
        text = "Celkem utraceno: ${repo.seznamUtrat.sumOf { it.cena.toDouble() }.toString(2)} ${repo.mena}",
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        style = MaterialTheme.typography.bodyLarge
    )

    BottomAppBar(
        icons = {
            var menuRazeni by remember { mutableStateOf(false) }
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
            var menuMena by remember { mutableStateOf(false) }
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
                            repo.mena = it
                            menuMena = false
                        },
                        leadingIcon = {
                            if (repo.mena == it)
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
                    repo.ulozitSoubor(repo.nazevAkce) { outputStream ->
                        repo.apply {

                            val text = """
                            |${nazevAkce}
                            |
                            |
                            |Celkem utraceno: ${seznamUtrat.sumOf { it.cena.toDouble() }.toString(2)} $mena
                            |
                            |Útraty za jednotlivé účastníky:
                            ${
                                seznamUcastniku
                                    .sortedByDescending { it.utraty(repo).sumOf { utrata -> utrata.cena.toDouble() } }
                                    .joinToString("\n") { ucastnik ->
                                        "|${ucastnik.jmeno} – ${ucastnik.utraty(repo).sumOf { (it.cena.toDouble() / it.ucastnici.size) }} $mena"
                                    }
                            }
                            |
                            |
                            |Seznam útrat:
                            ${
                                seznamUtrat.sortedWith(Razeni.Datum2.razeni).joinToString("\n") { utrata ->
                                    val ucastnici = if (utrata.ucastnici != seznamUcastniku.map { it.id })
                                        " – pouze ${seznamUcastniku.filter { it.id in utrata.ucastnici }.joinToString { it.jmeno }}"
                                    else ""

                                    with(utrata) {
                                        "|$datum – ${cena.toDouble().toString(2)} $mena – $nazev$ucastnici"
                                    }
                                }
                            }
                            |
                            """.trimMargin(marginPrefix = "|")

                            var pageNumber = 1

                            val pdfDocument = PdfDocument()
                            var pageInfo = PdfDocument.PageInfo.Builder(300, 600, pageNumber).create()
                            var page = pdfDocument.startPage(pageInfo)

                            val paint = Paint()
                            val x = 10F
                            var y = 25F

                            text.split("\n").forEach { line ->
                                if (line.isEmpty()) {
                                    page.canvas.drawText(line, x, y, paint)
                                    y += paint.descent() - paint.ascent()
                                    return@forEach
                                }

                                var segment = line
                                var endIndex: Int

                                while (segment.isNotEmpty()) { // dokud zbývá text

                                    endIndex = paint.breakText(segment, true, 280F, null) // kam už budeme psát

                                    if (endIndex < segment.lastIndex) { // pokud něco zbyde, tak ať to je rozdělený na mezeře
                                        endIndex = segment.substring(0, endIndex).lastIndexOf(" ")
                                    }

                                    page.canvas.drawText(segment.substring(0, endIndex), x, y, paint) // namalovat

                                    segment = segment.substring(endIndex).removePrefix(" ") // budeme malovat zbytek

                                    y += paint.descent() - paint.ascent()
                                    if (y >= 575) {

                                        pdfDocument.finishPage(page)

                                        pageInfo = PdfDocument.PageInfo.Builder(300, 600, pageNumber++).create()
                                        page = pdfDocument.startPage(pageInfo)
                                        y = 25F
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

                            outputStream.close()
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Download, "Stáhnout seznam")
            }
            var dialogOdstranit by remember { mutableStateOf(false) }
            if (dialogOdstranit) AlertDialog(
                onDismissRequest = {
                    dialogOdstranit = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            repo.seznamUtrat = emptyList()
                            repo.seznamUcastniku = emptyList()
                            repo.nazevAkce = ""
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
                var menu by remember { mutableStateOf(false) }
                var opravduNaNikoho by remember { mutableStateOf(false) }
                FloatingActionButton(
                    onClick = {
                        if (repo.seznamUcastniku.isEmpty())
                            opravduNaNikoho = true
                        else
                            menu = true
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
                                menu = true
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
                var zobrazit by remember { mutableStateOf(false) }
                var naNejakeUcastniky by remember { mutableStateOf(false) }
                var sJinymDatem by remember { mutableStateOf(false) }
                NovejDialog(
                    pocatecniUtrata = Utrata(
                        datum = Calendar.getInstance().toDatum().asString(),
                        cas = Calendar.getInstance().let { "${it[Calendar.HOUR_OF_DAY]}:${it[Calendar.MINUTE]}:${it[Calendar.SECOND]}" },
                        cena = 0F,
                        nazev = "",
                        ucastnici = repo.seznamUcastniku.filter { it.aktivovan }.map { it.id },
                    ),
                    zobrazit = zobrazit,
                    schovat = {
                        naNejakeUcastniky = false
                        sJinymDatem = false
                        zobrazit = false
                    },
                    repo = repo,
                    naNejakeUcastniky = naNejakeUcastniky,
                    sJinymDatem = sJinymDatem,
                ) { utrata ->
                    repo.seznamUtrat += utrata
                }
                DropdownMenu(
                    expanded = menu,
                    onDismissRequest = {
                        menu = false
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Běžná útrata") },
                        onClick = {
                            menu = false
                            zobrazit = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Pouze na nějaké účastníky") },
                        onClick = {
                            menu = false
                            naNejakeUcastniky = true
                            zobrazit = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "S jiným datem") },
                        onClick = {
                            menu = false
                            sJinymDatem = true
                            zobrazit = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Pouze na nějaké účastníky s jiným datem") },
                        onClick = {
                            menu = false
                            sJinymDatem = true
                            naNejakeUcastniky = true
                            zobrazit = true
                        }
                    )
                }
            }
        }
    )
}