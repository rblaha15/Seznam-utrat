package cz.rblaha15.seznamUtrat.ui.ucastnici

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cz.rblaha15.seznamUtrat.data.LocalDataSource
import cz.rblaha15.seznamUtrat.toString
import cz.rblaha15.seznamUtrat.ui.seznam.showUUIDs
import cz.rblaha15.seznamUtrat.ui.theme.SeznamUtratTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun Ucastnici(
    dataSource: LocalDataSource,
    navController: NavController,
) {
    val viewModel = viewModel {
        UcastniciViewModel(dataSource)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    UcastniciScreen(
        state = state,
        onEvent = viewModel::onEvent,
        navigateUp = {
            navController.navigateUp()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UcastniciScreen(
    state: UcastniciState,
    onEvent: (UcastniciEvent) -> Unit,
    navigateUp: () -> Unit,
) {
    val snackbarState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        Modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Účastníci") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigateUp()
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Zpět")
                    }
                }
            )
        },
        floatingActionButton = {
            val scope = rememberCoroutineScope()
            FloatingActionButton(
                onClick = {
                    onEvent(PridatUcastnika)
                    scope.launch {
                        delay(100)
                        focusRequester.requestFocus()
                    }
//                    repo.seznamUcastniku(
//                        seznamUcastniku + Ucastnik(
//                            jmeno = "",
//                        )
//                    )
                },
            ) {
                Icon(Icons.Default.PersonAdd, "Přidat účastníka")
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarState)
        }
    ) { paddingValues ->
        if (state !is UcastniciState.OK) CircularProgressIndicator(
            Modifier
                .padding(paddingValues)
                .padding(16.dp))
        else Row(
            Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .height(IntrinsicSize.Min),
        ) {
            Sloupecek(
                values = state.ucastnici,
                label = "Jméno účastníka",
                Modifier.weight(1F),
                alignment = Alignment.CenterStart,
            ) { i, ucastnik ->
                var localValue by remember { mutableStateOf(ucastnik.jmeno) }
                TextField(
                    value = localValue,
                    onValueChange = { newValue ->
                        localValue = newValue
                        onEvent(AktualizovatJmeno(ucastnik.uuid, newValue))
//                        repo.seznamUcastniku(seznamUcastniku.mutate {
//                            val i = indexOfFirst { it.id == ucastnik.id }
//                            this[i] = this[i].copy(jmeno = newValue)
//                        })
                    },
                    if (i == state.ucastnici.lastIndex) Modifier.focusRequester(focusRequester) else Modifier,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                )
            }

            Sloupecek(
                values = state.ucastnici,
                label = "Útrata",
                alignment = Alignment.CenterStart,
            ) { ucastnik ->
                Text(
                    text = ucastnik.utrata /*seznamUtrat.cloveka(ucastnik.id)
                        .sumOf { (it.cena.toDouble() / it.ucastnici.size) }*/
                        .toString(2) + " " + state.mena,
                )
            }

            if (showUUIDs) Sloupecek(
                values = state.ucastnici,
                label = "UUID"
            ) { ucastnik ->
                Text(
                    text = ucastnik.uuid.toString().split("-").let {
                        "${it[0]}-${it[1]}\n-${it[2]}-${it[3]}\n-${it[4]}"
                    },
                    fontSize = 10.sp,
                )
            }

            Sloupecek(
                values = state.ucastnici,
                label = "Aktivován"
            ) { ucastnik ->
                Checkbox(
                    checked = ucastnik.aktivovan,
                    onCheckedChange = {
                        onEvent(AktualizovatAktivovanost(ucastnik.uuid, it))
//                        repo.seznamUcastniku(seznamUcastniku.mutate {
//                            val i = indexOf(ucastnik)
//                            this[i] = this[i].copy(aktivovan = it)
//                        })
                    },
                )
            }

            val scope = rememberCoroutineScope()
            Sloupecek(
                values = state.ucastnici,
                label = "Odstranit",
                Modifier.padding(end = 8.dp)
            ) { ucastnik ->
//                val muzemeOdstranit = remember { seznamUtrat.cloveka(ucastnik.id).isEmpty() }
                IconButton(
                    onClick = {
                        if (!ucastnik.muzemeOdstranit) scope.launch {
                            val result = snackbarState.showSnackbar(
                                message = "Nemůžete odstranit účastníka, který má nějaké útraty!",
                                actionLabel = "Deaktivovat".takeIf { ucastnik.aktivovan },
                                withDismissAction = true,
                                duration = SnackbarDuration.Long,
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onEvent(AktualizovatAktivovanost(ucastnik.uuid, false))
//                                repo.seznamUcastniku(seznamUcastniku.mutate {
//                                    val i = indexOf(ucastnik)
//                                    this[i] = this[i].copy(aktivovan = false)
//                                })
                            }
                        }
                        else /*repo.seznamUcastniku(seznamUcastniku.mutate {
                            remove(ucastnik)
                        })*/ onEvent(OdstranitUcastnika(ucastnik.uuid))
                    },
                    modifier = Modifier,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (ucastnik.muzemeOdstranit) IconButtonDefaults.iconButtonColors().contentColor
                        else IconButtonDefaults.iconButtonColors().disabledContentColor,
                        containerColor = if (ucastnik.muzemeOdstranit) IconButtonDefaults.iconButtonColors().containerColor
                        else IconButtonDefaults.iconButtonColors().disabledContainerColor,
                    )
                ) {
                    Icon(Icons.Default.Clear, "Odebrat")
                }
            }
        }
    }
}

context(RowScope)
@Composable
inline fun <T> Sloupecek(
    values: List<T>,
    label: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    content: @Composable (T) -> Unit,
) = Sloupecek(
    values = values,
    label = label,
    modifier = modifier,
    alignment = alignment,
    content = { _, value ->
        content(value)
    }
)

context(RowScope)
@Composable
inline fun <T> Sloupecek(
    values: List<T>,
    label: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    content: @Composable (Int, T) -> Unit,
) = Column(
    modifier
        .fillMaxHeight()
        .width(IntrinsicSize.Max)
        .padding(bottom = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        contentAlignment = alignment,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
        )
    }
    values.forEachIndexed { i, value ->
        Box(
            Modifier
                .weight(1F)
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp),
            contentAlignment = alignment,
        ) {
            content(i, value)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UcastniciPreviewLoading() {
    SeznamUtratTheme {
        UcastniciScreen(
            UcastniciState.Loading, {}
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun UcastniciPreview() {
    SeznamUtratTheme {
        UcastniciScreen(
            UcastniciState.OK(
                ucastnici = listOf(
                    UcastnikVSeznamu(
                        jmeno = "Tomáš",
                        aktivovan = true,
                        uuid = UUID.randomUUID(),
                        utrata = 38.0,
                        muzemeOdstranit = false,
                    ),
                    UcastnikVSeznamu(
                        jmeno = "Taťulda",
                        aktivovan = false,
                        uuid = UUID.randomUUID(),
                        utrata = 308.99,
                        muzemeOdstranit = true,
                    ),
                ),
                mena = "Kč"
            ), {}
        ) {}
    }
}