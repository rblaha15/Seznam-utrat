package cz.rblaha15.seznamUtrat.ui.seznam

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cz.rblaha15.seznamUtrat.data.LocalDataSource
import cz.rblaha15.seznamUtrat.data.Razeni
import cz.rblaha15.seznamUtrat.data.Ucastnik
import cz.rblaha15.seznamUtrat.data.Utrata
import cz.rblaha15.seznamUtrat.data.serializers.asString
import cz.rblaha15.seznamUtrat.rememberResultLauncher
import cz.rblaha15.seznamUtrat.toString
import cz.rblaha15.seznamUtrat.ui.savers.StateOfNulableUtrataSaver
import cz.rblaha15.seznamUtrat.ui.theme.SeznamUtratTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

var showUUIDs by mutableStateOf(false)

@Composable
fun Seznam(
    dataSource: LocalDataSource,
    navController: NavController
) {
    val launcher =
        rememberResultLauncher(ActivityResultContracts.CreateDocument("application/pdf"))

    val ctx = LocalContext.current

    val viewModel = viewModel {
        SeznamViewModel(dataSource, launcher) {
            ctx.contentResolver.openOutputStream(this)?.use(it)
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    SeznamScreen(
        state = state,
        onEvent = viewModel::onEvent,
        navigate = navController::navigate,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SeznamScreen(
    state: SeznamState,
    onEvent: (SeznamEvent) -> Unit,
    navigate: (route: String) -> Unit
) {
    var localNazevAkce by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state::class) {
        if (state is SeznamState.OK) {
            localNazevAkce = state.nazevAkce
        }
    }

    val scope = rememberCoroutineScope()
    var upravitUtratu by rememberSaveable(saver = StateOfNulableUtrataSaver) { mutableStateOf(null as Utrata?) }
    val animatableNova = remember { Animatable(1F) }
    val animatableUpravit = remember { Animatable(1F) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Seznam útrat", Modifier.combinedClickable(onClick = {}, onLongClick = { showUUIDs = !showUUIDs }))
                },
                actions = {
                    IconButton(
                        onClick = {
                            navigate("ucastnici")
                        },
                    ) {
                        Icon(Icons.Default.PeopleAlt, "Účastníci")
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(
                novaUtrata = {
//                    novaUtrataSheet = true
                    scope.launch {
                        animatableNova.animateTo(0F, tween(500))
                    }
                },
                resetovatNazevAkce = {
                    localNazevAkce = ""
                },
                state = state,
                onEvent = onEvent,
            )
        }
    ) { paddingValues ->
        if (state.isLoading()) CircularProgressIndicator(Modifier.padding(paddingValues).padding(16.dp))
        else Column(
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
                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField(
                    value = localNazevAkce,
                    onValueChange = {
                        localNazevAkce = it
                        onEvent(AktualizovatNazevAkce(it))
                    },
                    Modifier,
                    label = { Text(text = "Název akce") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions {
                        keyboardController?.hide()
                    },
                    singleLine = true,
                )
            }
            LazyColumn {
                items(
                    state.utraty.sortedWith(Comparator.comparing({ it.toUtrata() }, state.razeni)),
                    key = { it.id },
                ) { utrata ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = utrata.nazev + " – " + utrata.cena.toDouble()
                                    .toString(2) + " " + state.mena
                            )
                        },
                        Modifier.animateItemPlacement(),
                        supportingContent = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = utrata.datum.asString())
                                Text(text = /*when {
                                    utrata.ucastnici.toSet() == seznamUcastniku.map { it.id }.toSet() -> ""

                                    utrata.ucastnici.isEmpty() -> "Žádný účastník"
                                    else -> seznamUcastniku.filter { it.id in utrata.ucastnici }.joinToString { it.jmeno }
                                }*/utrata.ucastnici)
                            }
                        },
                        overlineContent = if (showUUIDs) {{
                            Text(text = "UUID: ${utrata.id}")
                        }} else null,
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
                                        scope.launch {
                                            upravitUtratu = utrata.toUtrata()
                                            animatableUpravit.animateTo(0F, tween(500))
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Odstranit") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, null)
                                    },
                                    onClick = {
                                        menuMoznosti = false
                                        onEvent(OdstranitUtratu(utrata.id))
//                                        repo.seznamUtrat(seznamUtrat.mutate {
//                                            remove(utrata)
//                                        })
                                    }
                                )
                            }
                            IconButton(
                                onClick = {
                                    menuMoznosti = true
                                }
                            ) {
                                Icon(Icons.Default.MoreVert, "Možnosti k útratě ${utrata.nazev}")
                            }
                        }
                    )
                }
            }
        }
    }

    if (state is SeznamState.OK) UpravitUtratuSheet(
        pocatecniUtrata = upravitUtratu ?: Utrata.empty(state.aktivovaniUcastnici),
//        zobrazit = upravitUtratuSheet != null,
//        schovat = {
//            upravitUtratuSheet = null
//        },
        animatable = animatableUpravit,
        seznamUcastniku = state.ucastnici,
        mena = state.mena,
    ) { novaUtrata ->
        onEvent(AktualizovatUtratu(novaUtrata))
        //        repo.seznamUtrat(seznamUtrat.mutate {
        //            this[indexOfFirst { it.id == novaUtrata.id }] = novaUtrata
        //        })
    }
    if (state is SeznamState.OK) UpravitUtratuSheet(
        pocatecniUtrata = Utrata.empty(state.aktivovaniUcastnici),
//        zobrazit = novaUtrataSheet,
//        schovat = {
//            novaUtrataSheet = false
//        },
        seznamUcastniku = state.ucastnici,
        mena = state.mena,
        animatable = animatableNova,
    ) { utrata ->
        onEvent(PridatUtratu(utrata))
//            repo.seznamUtrat(seznamUtrat + utrata)
    }
}

fun Utrata.Companion.empty(aktivovaniUcastnici: List<UUID>) = Utrata(
    datum = LocalDate.now(),
    cas = LocalTime.now(),
    cena = 0F,
    nazev = "",
    ucastnici = aktivovaniUcastnici,
)

@Preview
@Composable
fun SeznamPreviewL() {
    SeznamUtratTheme {
        SeznamScreen(SeznamState.Loading, {}) {}
    }
}
@Preview
@Composable
fun SeznamPreview() {
    SeznamUtratTheme {
        SeznamScreen(SeznamState.OK(
            ucastnici = listOf(
                Ucastnik(
                    jmeno = "Tomáš",
                    aktivovan = true,
                    id = UUID.randomUUID(),
                ),
                Ucastnik(
                    jmeno = "Taťulda",
                    aktivovan = false,
                    id = UUID.randomUUID(),
                ),
            ),
            mena = "Kč",
            nazevAkce = "Naše akce",
            utraty = listOf(
                UtrataVSeznamu(
                    datum = LocalDate.now(),
                    cas = LocalTime.now(),
                    cena = 38F,
                    nazev = "Naše akce",
                    ucastnici = "Tomáš, Taťulda",
                    ucastniciIds = listOf(UUID.randomUUID()),
                    id = UUID.randomUUID(),
                ),
                UtrataVSeznamu(
                    datum = LocalDate.now(),
                    cas = LocalTime.now(),
                    cena = 308.99F,
                    nazev = "Vaše akce",
                    ucastnici = "",
                    ucastniciIds = listOf(UUID.randomUUID()),
                    id = UUID.randomUUID(),
                ),
            ),
            aktivovaniUcastnici = listOf(UUID.randomUUID()),
            suma = 38.0,
            razeni = Razeni.Datum1,
        ), {}) {}
    }
}