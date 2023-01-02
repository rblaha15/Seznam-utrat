package cz.rblaha15.seznamUtrat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import cz.rblaha15.seznamUtrat.FakeUtratyRepositoryImpl
import cz.rblaha15.seznamUtrat.MainActivity.Companion.mutate
import cz.rblaha15.seznamUtrat.MainActivity.Companion.toString
import cz.rblaha15.seznamUtrat.Razeni
import cz.rblaha15.seznamUtrat.UtratyRepository
import cz.rblaha15.seznamUtrat.ui.theme.SeznamUtratTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeznamScreen(
    repo: UtratyRepository,
    navigate: (route: String) -> Unit
) {
    var razeni by remember { mutableStateOf(Razeni.Datum1) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Seznam útrat") },
                actions = {
                    IconButton(
                        onClick = {
                            navigate("ucastnici")
                        }
                    ) {
                        Icon(Icons.Default.PeopleAlt, "Účastníci")
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(nastavitRazeni = {
                razeni = it
            }, repo)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = repo.nazevAkce,
                    onValueChange = {
                        repo.nazevAkce = it
                    },
                    label = { Text(text = "Název akce") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                )
            }
            LazyColumn {
                items(repo.seznamUtrat/*.reversed()*/.sortedWith(razeni.razeni)) { utrata ->

                    var zobrazitUpravitDialog by remember { mutableStateOf(false) }
                    NovejDialog(
                        pocatecniUtrata = utrata,
                        zobrazit = zobrazitUpravitDialog,
                        schovat = {
                            zobrazitUpravitDialog = false
                        },
                        repo = repo,
                        sNazvem = true,
                        sCenou = true,
                        naNejakeUcastniky = true,
                        sJinymDatem = true,
                    ) { novaUtrata ->
                        repo.seznamUtrat = repo.seznamUtrat.mutate {
                            this[indexOf(utrata)] = novaUtrata
                        }
                    }
                    ListItem(
                        headlineText = {
                            Text(
                                text = utrata.nazev + " – " + utrata.cena.toDouble()
                                    .toString(2) + " " + repo.mena
                            )
                        },
                        supportingText = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = utrata.datum)
                                Text(text = when {
                                    utrata.ucastnici.toSet() == repo.seznamUcastniku.map { it.id }
                                        .toSet() -> ""

                                    utrata.ucastnici.isEmpty() -> "Žádný účastník"
                                    else -> repo.seznamUcastniku.filter { it.id in utrata.ucastnici }
                                        .joinToString { it.jmeno }
                                })
                            }
                        },
                        trailingContent = {
                            var menuMoznosti by remember { mutableStateOf(false) }
                            DropdownMenu(
                                expanded = menuMoznosti,
                                onDismissRequest = {
                                    menuMoznosti = false
                                }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = "Upravit") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, null)
                                    },
                                    onClick = {
                                        menuMoznosti = false
                                        zobrazitUpravitDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Odstranit") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, null)
                                    },
                                    onClick = {
                                        menuMoznosti = false
                                        repo.seznamUtrat = repo.seznamUtrat.mutate {
                                            remove(utrata)
                                        }
                                    }
                                )
                            }
                            IconButton(
                                onClick = {
                                    menuMoznosti = true
                                }
                            ) {
                                Icon(Icons.Default.MoreVert, "Možnosti")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SeznamPreview() {
    SeznamUtratTheme {
        SeznamScreen(FakeUtratyRepositoryImpl()) {}
    }
}