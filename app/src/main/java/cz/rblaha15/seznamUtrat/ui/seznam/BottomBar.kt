package cz.rblaha15.seznamUtrat.ui.seznam

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.rblaha15.seznamUtrat.data.Razeni
import cz.rblaha15.seznamUtrat.toString

const val PAGE_WIDTH = 595 // PostScripts (1/72th of an inch)
const val PAGE_HEIGHT = 842 // PostScripts (1/72th of an inch)
const val PRINTABLE_WIDTH = 482 // PostScripts (1/72th of an inch)
const val PRINTABLE_HEIGHT = 700 // PostScripts (1/72th of an inch)
const val VERTICAL_PADDING = (PAGE_HEIGHT - PRINTABLE_HEIGHT) / 2 // PostScripts (1/72th of an inch)
const val HORIZONTAL_PADDING = (PAGE_WIDTH - PRINTABLE_WIDTH) / 2 // PostScripts (1/72th of an inch)

@Composable
fun BottomBar(
    novaUtrata: () -> Unit,
    resetovatNazevAkce: () -> Unit,
    state: SeznamState,
    onEvent: (SeznamEvent) -> Unit,
) = Column {

    if (state.isOk()) Text(
        text = "Celkem utraceno: ${state.suma.toString(2)} ${state.mena}",
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
                Razeni.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(text = it.text) },
                        onClick = {
                            onEvent(AktualizovatRazeni(it))
                            menuRazeni = false
                        },
                        leadingIcon = { Icon(it.icon, null) },
                        trailingIcon = { Icon(it.orderIcon, null) }
                    )
                }
            }
            IconButton(
                onClick = {
                    menuRazeni = true
                }
            ) {
                Icon(Icons.AutoMirrored.Default.Sort, "Seřadit")
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
                    "£",
                ).forEach {
                    DropdownMenuItem(
                        text = { Text(text = it) },
                        onClick = {
                            onEvent(AktualizovatMenu(it))
                            menuMena = false
                        },
                        leadingIcon = {
                            if (state.okOrNull()?.mena == it)
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
                    onEvent(VygenerovatPDF)
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
                            resetovatNazevAkce()
                            onEvent(Resetovat)
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
                        if (state.okOrNull()?.ucastnici?.isEmpty() == true)
                            opravduNaNikoho = true
                        else
                            novaUtrata()
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
                                novaUtrata()
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