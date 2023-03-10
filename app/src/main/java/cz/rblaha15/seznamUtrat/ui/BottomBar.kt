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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.rblaha15.seznamUtrat.MainActivity.Companion.asString
import cz.rblaha15.seznamUtrat.MainActivity.Companion.toDatum
import cz.rblaha15.seznamUtrat.MainActivity.Companion.toString
import cz.rblaha15.seznamUtrat.Razeni
import cz.rblaha15.seznamUtrat.Utrata
import cz.rblaha15.seznamUtrat.UtratyRepository
import java.util.Calendar

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
        actions = {
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
                Icon(Icons.Default.Sort, "Se??adit")
            }
            var menuMena by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = menuMena,
                onDismissRequest = {
                    menuMena = false
                }
            ) {
                listOf(
                    "K??",
                    "???",
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
                Icon(Icons.Default.AttachMoney, "Zm??nit m??nu")
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
                            |??traty za jednotliv?? ????astn??ky:
                            ${
                                seznamUcastniku
                                    .sortedByDescending { it.utraty(repo).sumOf { utrata -> utrata.cena.toDouble() } }
                                    .joinToString("\n") { ucastnik ->
                                        "|${ucastnik.jmeno} ??? ${ucastnik.utraty(repo).sumOf { (it.cena.toDouble() / it.ucastnici.size) }} $mena"
                                    }
                            }
                            |
                            |
                            |Seznam ??trat:
                            ${
                                seznamUtrat.sortedWith(Razeni.Datum2.razeni).joinToString("\n") { utrata ->
                                    val ucastnici = if (utrata.ucastnici != seznamUcastniku.map { it.id })
                                        " ??? pouze ${seznamUcastniku.filter { it.id in utrata.ucastnici }.joinToString { it.jmeno }}"
                                    else ""

                                    with(utrata) {
                                        "|$datum ??? ${cena.toDouble().toString(2)} $mena ??? $nazev$ucastnici"
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

                                while (segment.isNotEmpty()) { // dokud zb??v?? text

                                    endIndex = paint.breakText(segment, true, 280F, null) // kam u?? budeme ps??t

                                    if (endIndex < segment.lastIndex) { // pokud n??co zbyde, tak a?? to je rozd??len?? na meze??e
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
                Icon(Icons.Default.Download, "St??hnout seznam")
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
                        Text(text = "Ano, odstranit v??e!")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            dialogOdstranit = false
                        }
                    ) {
                        Text(text = "Zru??it")
                    }
                },
                title = {
                    Text("Odstranit v??e?")
                },
                text = {
                    Text("Opravdu chcete odstranit v??echna data? Tato akce je nevratn??!")
                },
            )
            IconButton(
                onClick = {
                    dialogOdstranit = true
                }
            ) {
                Icon(Icons.Default.DeleteForever, "Odstranit v??e")
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
                    Icon(Icons.Default.AddCircle, "P??idat ??tratu")
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
                            Text(text = "Ano, pokra??ovat")
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
                        Text("????dn?? ????astn??ci")
                    },
                    text = {
                        Text("Opravdu chcete p??idat ??tratu bez ????astn??k???")
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
                        text = { Text(text = "B????n?? ??trata") },
                        onClick = {
                            menu = false
                            zobrazit = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Pouze na n??jak?? ????astn??ky") },
                        onClick = {
                            menu = false
                            naNejakeUcastniky = true
                            zobrazit = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "S jin??m datem") },
                        onClick = {
                            menu = false
                            sJinymDatem = true
                            zobrazit = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Pouze na n??jak?? ????astn??ky s jin??m datem") },
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