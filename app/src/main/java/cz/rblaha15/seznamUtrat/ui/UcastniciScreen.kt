package cz.rblaha15.seznamUtrat.ui

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.rblaha15.seznamUtrat.*
import cz.rblaha15.seznamUtrat.ui.theme.SeznamUtratTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UcastniciScreen(
    repo: UtratyRepository,
) {
    val seznamUcastniku by repo.seznamUcastniku.collectAsState(emptyList())
    val seznamUtrat by repo.seznamUtrat.collectAsState(emptyList())
    val mena by repo.mena.collectAsState("")
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        Modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Účastníci") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    repo.seznamUcastniku(
                        seznamUcastniku + Ucastnik(
                            jmeno = "",
                        )
                    )
                },
            ) {
                Icon(Icons.Default.PersonAdd, "Přidat účastníka")
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarState)
        }
    ) { paddingValues ->
        Row(
            Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .height(IntrinsicSize.Min),
        ) {
            Sloupecek(
                values = seznamUcastniku,
                label = "Jméno účastníka",
                Modifier.weight(1F)
            ) { ucastnik ->
                var localValue by remember { mutableStateOf(ucastnik.jmeno) }
                TextField(
                    value = localValue,
                    onValueChange = { newValue ->
                        localValue = newValue
                        repo.seznamUcastniku(seznamUcastniku.mutate {
                            val i = indexOfFirst { it.id == ucastnik.id }
                            this[i] = this[i].copy(jmeno = newValue)
                        })
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    singleLine = true,
                )
            }

            Sloupecek(
                values = seznamUcastniku,
                label = "Útrata"
            ) { ucastnik ->
                Text(
                    text = seznamUtrat.cloveka(ucastnik.id)
                        .sumOf { (it.cena.toDouble() / it.ucastnici.size) }
                        .toString(2) + " " + mena,
                    maxLines = 1
                )
            }

            Sloupecek(
                values = seznamUcastniku,
                label = "Aktivován"
            ) { ucastnik ->
                Checkbox(
                    checked = ucastnik.aktivovan,
                    onCheckedChange = {
                        repo.seznamUcastniku(seznamUcastniku.mutate {
                            val i = indexOf(ucastnik)
                            this[i] = this[i].copy(aktivovan = it)
                        })
                    },
                )
            }

            val scope = rememberCoroutineScope()
            Sloupecek(
                values = seznamUcastniku,
                label = "Odstranit",
                Modifier.padding(end = 8.dp)
            ) { ucastnik ->
                val muzemeOdstranit = remember { seznamUtrat.cloveka(ucastnik.id).isEmpty() }
                IconButton(
                    onClick = {
                        if (!muzemeOdstranit) scope.launch {
                            val result = snackbarState.showSnackbar(
                                message = "Nemůžete odstranit účastníka, který má nějaké útraty!",
                                actionLabel = "Deaktivovat".takeIf { ucastnik.aktivovan },
                                withDismissAction = true,
                                duration = SnackbarDuration.Long,
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                repo.seznamUcastniku(seznamUcastniku.mutate {
                                    val i = indexOf(ucastnik)
                                    this[i] = this[i].copy(aktivovan = false)
                                })
                            }
                        }
                        else repo.seznamUcastniku(seznamUcastniku.mutate {
                            remove(ucastnik)
                        })
                    },
                    modifier = Modifier,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (muzemeOdstranit) IconButtonDefaults.iconButtonColors().contentColor
                        else IconButtonDefaults.iconButtonColors().disabledContentColor,
                        containerColor = if (muzemeOdstranit) IconButtonDefaults.iconButtonColors().containerColor
                        else IconButtonDefaults.iconButtonColors().disabledContainerColor,
                    )
                ) {
                    Icon(Icons.Default.Clear, "odebrat")
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
    content: @Composable (T) -> Unit,
) = Column(
    modifier
        .fillMaxHeight()
        .width(IntrinsicSize.Max)
        .padding(bottom = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = label,
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        style = MaterialTheme.typography.labelMedium
    )
    values.forEach { value ->
        Box(
            Modifier
                .weight(1F)
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            content(value)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UcastniciPreview() {
    SeznamUtratTheme {
        UcastniciScreen(FakeUtratyRepositoryImpl())
    }
}