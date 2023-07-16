package cz.rblaha15.seznamUtrat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.rblaha15.seznamUtrat.FakeUtratyRepositoryImpl
import cz.rblaha15.seznamUtrat.Razeni
import cz.rblaha15.seznamUtrat.StateOfNulableUtrataSaver
import cz.rblaha15.seznamUtrat.Utrata
import cz.rblaha15.seznamUtrat.UtratyRepository
import cz.rblaha15.seznamUtrat.asString
import cz.rblaha15.seznamUtrat.mutate
import cz.rblaha15.seznamUtrat.nova
import cz.rblaha15.seznamUtrat.toString
import cz.rblaha15.seznamUtrat.ui.theme.SeznamUtratTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeznamScreen(
    repo: UtratyRepository,
    navigate: (route: String) -> Unit
) {
    var razeni by rememberSaveable { mutableStateOf(Razeni.Datum1) }
    val seznamUtrat by repo.seznamUtrat.collectAsState(emptyList())
    val seznamUcastniku by repo.seznamUcastniku.collectAsState(emptyList())
    val nazevAkce by repo.nazevAkce.collectAsState("")
    var localNazevAkce by rememberSaveable { mutableStateOf(nazevAkce) }
    val mena by repo.mena.collectAsState("")

    var upravitUtratuSheet by rememberSaveable(saver = StateOfNulableUtrataSaver) { mutableStateOf(null as Utrata?) }
    var novaUtrataSheet by rememberSaveable { mutableStateOf(false) }

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
            BottomBar(
                nastavitRazeni = {
                    razeni = it
                },
                novaUtrata = {
                    novaUtrataSheet = true
                },
                resetovatNazevAkce = {
                    localNazevAkce = ""
                },
                repo = repo,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val focusRequester = remember { FocusRequester() }
                OutlinedTextField(
                    value = localNazevAkce,
                    onValueChange = {
                        localNazevAkce = it
                        repo.nazevAkce(it)
                    },
                    label = { Text(text = "Název akce") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions {
                        focusRequester.freeFocus()
                    },
                    singleLine = true,
                )
            }
            LazyColumn {
                items(
                    seznamUtrat.sortedWith(razeni.razeni),
                    key = { it.id }
                ) { utrata ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = utrata.nazev + " – " + utrata.cena.toDouble()
                                    .toString(2) + " " + mena
                            )
                        },
                        supportingContent = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = utrata.datum.asString())
                                Text(text = when {
                                    utrata.ucastnici.toSet() == seznamUcastniku.map { it.id }.toSet() -> ""

                                    utrata.ucastnici.isEmpty() -> "Žádný účastník"
                                    else -> seznamUcastniku.filter { it.id in utrata.ucastnici }.joinToString { it.jmeno }
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
                                        upravitUtratuSheet = utrata
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Odstranit") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, null)
                                    },
                                    onClick = {
                                        menuMoznosti = false
                                        repo.seznamUtrat(seznamUtrat.mutate {
                                            remove(utrata)
                                        })
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

    println(upravitUtratuSheet?.id)

    UpravitUtratuSheet(
        pocatecniUtrata = upravitUtratuSheet ?: Utrata.nova(seznamUcastniku),
        zobrazit = upravitUtratuSheet != null,
        schovat = {
            upravitUtratuSheet = null
        },
        repo = repo,
    ) { novaUtrata ->
        repo.seznamUtrat(seznamUtrat.mutate {
            println(map { it.id })
            println(novaUtrata.id)
            this[indexOfFirst { it.id == novaUtrata.id }] = novaUtrata
        })
    }
    UpravitUtratuSheet(
        pocatecniUtrata = Utrata.nova(seznamUcastniku),
        zobrazit = novaUtrataSheet,
        schovat = {
            novaUtrataSheet = false
        },
        repo = repo,
    ) { utrata ->
        repo.seznamUtrat(seznamUtrat + utrata)
    }
}

@Preview
@Composable
fun SeznamPreview() {
    SeznamUtratTheme {
        SeznamScreen(FakeUtratyRepositoryImpl()) {}
    }
}