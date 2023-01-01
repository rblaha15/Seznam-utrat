package cz.rblaha15.seznamUtrat.utraty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import cz.rblaha15.seznamUtrat.FakeUtratyRepositoryImpl
import cz.rblaha15.seznamUtrat.Razeni
import cz.rblaha15.seznamUtrat.UtratyRepository
import cz.rblaha15.seznamUtrat.UtratyRepositoryImpl
import cz.rblaha15.seznamUtrat.ucastnici.UcastniciActivity
import cz.rblaha15.seznamUtrat.ui.theme.SeznamUtratTheme
import java.util.*
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH

lateinit var repoSingleton: UtratyRepository

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repoSingleton = UtratyRepositoryImpl(this)
        setContent {
            SeznamUtratTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainContent(repoSingleton)
                }
            }
        }
    }
}

typealias Datum = Pair<Int, Int>

fun Calendar.toDatum() = Datum(get(DAY_OF_MONTH), get(MONTH) + 1)
fun Datum.asString() = "$first. $second."

typealias Cas = Triple<Int, Int, Int>

fun Cas.asString() = "$first:$second:$third"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    repo: UtratyRepository,
) {
    var razeni by remember { mutableStateOf(Razeni.Datum1) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Seznam útrat") },
                actions = {
                    IconButton(
                        onClick = {
                            cz.rblaha15.seznamUtrat.utraty.repoSingleton.startActivity(UcastniciActivity::class.java)
                        }
                    ) {
                        Icon(Icons.Default.PeopleAlt, "Účastníci")
                    }
                }
            )
        },
        bottomBar = {
            BottomBar({
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
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
                            Text(text = utrata.nazev + " – " + utrata.cena.toDouble().toString(2) + " " + repo.mena)
                        },
                        supportingText = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = utrata.datum)
                                Text(text = when {
                                    utrata.ucastnici.toSet() == repo.seznamUcastniku.map { it.id }.toSet() -> ""
                                    utrata.ucastnici.isEmpty() -> "Žádný účastník"
                                    else -> repo.seznamUcastniku.filter { it.id in utrata.ucastnici }.joinToString { it.jmeno }
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

internal fun Double.toString(decimalPlaces: Int) = toString()
    .split(".")
    .mapIndexed { i, s -> if (i == 1) s.take(decimalPlaces) else s }
    .joinToString(",")

internal fun <T> List<T>.mutate(transform: MutableList<T>.() -> Unit) = buildList {
    addAll(this@mutate)
    transform()
}

@Preview
@Composable
fun MainPreview() {
    SeznamUtratTheme {
        MainContent(FakeUtratyRepositoryImpl())
    }
}