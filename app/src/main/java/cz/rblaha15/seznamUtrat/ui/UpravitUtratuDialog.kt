package cz.rblaha15.seznamUtrat.ui

import androidx.activity.addCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.marosseleng.compose.material3.datetimepickers.date.ui.dialog.DatePickerDialog
import com.marosseleng.compose.material3.datetimepickers.time.domain.noSeconds
import com.marosseleng.compose.material3.datetimepickers.time.ui.dialog.TimePickerDialog
import cz.rblaha15.seznamUtrat.LocalDateSaver
import cz.rblaha15.seznamUtrat.LocalTimeSaver
import cz.rblaha15.seznamUtrat.Utrata
import cz.rblaha15.seznamUtrat.UtratyRepository
import cz.rblaha15.seznamUtrat.asString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun UpravitUtratuDialog(
    pocatecniUtrata: Utrata,
    zobrazit: Boolean,
    schovat: () -> Unit,
    repo: UtratyRepository,
    poVybrani: (Utrata) -> Unit,
) {
    val mena by repo.mena.collectAsState("")
    val seznamUcastniku by repo.seznamUcastniku.collectAsState(emptyList())

    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    DisposableEffect(zobrazit) {
        val callback =
            if (zobrazit) onBackPressedDispatcherOwner?.onBackPressedDispatcher?.addCallback {
                schovat()
            }
            else null
        onDispose {
            callback?.remove()
        }
    }

    AnimatedVisibility(
        visible = zobrazit,
        enter = slideInVertically(animationSpec = tween(300), initialOffsetY = { it }),
        exit = slideOutVertically(animationSpec = tween(300), targetOffsetY = { it }),
    ) {
        var nazev by rememberSaveable { mutableStateOf(pocatecniUtrata.nazev) }
        var cena by rememberSaveable { mutableFloatStateOf(pocatecniUtrata.cena) }
        var datum by rememberSaveable(saver = LocalDateSaver) { mutableStateOf(pocatecniUtrata.datum) }
        var cas by rememberSaveable(saver = LocalTimeSaver) { mutableStateOf(pocatecniUtrata.cas) }
        var ucastnici by rememberSaveable { mutableStateOf(pocatecniUtrata.ucastnici) }

        Surface(
            Modifier.fillMaxSize()
        ) {
            val focusManager = LocalFocusManager.current

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Upravit útratu") }
                    )
                }
            ) { paddingValues ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .fillMaxSize()
                            .imePadding()
                            .imeNestedScroll()
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()
                                .weight(1F)
                        ) {
                            OutlinedTextField(
                                value = nazev,
                                onValueChange = {
                                    nazev = it
                                },
                                label = {
                                    Text(text = "Název útraty")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                keyboardActions = KeyboardActions {
                                    focusManager.moveFocus(FocusDirection.Down)
                                }
                            )
                            OutlinedTextField(
                                value = if (cena == 0F) "" else cena.toString(),
                                onValueChange = {
                                    cena = if (it == "") 0F
                                    else it.toFloatOrNull() ?: return@OutlinedTextField
                                },
                                label = {
                                    Text(text = "Cena")
                                },
                                trailingIcon = {
                                    Text(text = mena)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                            )
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                var date by remember { mutableStateOf(false) }
                                if (date) DatePickerDialog(
                                    initialDate = datum,
                                    onDateChange = {
                                        date = false
                                        datum = it
                                    },
                                    title = {
                                        Text("Vyberte datum")
                                    },
                                    onDismissRequest = {
                                        date = false
                                    },
                                )
                                OutlinedTextField(
                                    value = datum.asString(),
                                    onValueChange = {},
                                    Modifier
                                        .weight(1F)
                                        .padding(end = 8.dp)
                                        .onKeyEvent {
                                            if (it.key == Key.Enter) {
                                                date = true
                                            }
                                            return@onKeyEvent it.key == Key.Enter
                                        },
                                    label = {
                                        Text("Datum")
                                    },
                                    singleLine = true,
                                    interactionSource = remember { MutableInteractionSource() }
                                        .also { interactionSource ->
                                            LaunchedEffect(interactionSource) {
                                                interactionSource.interactions.collect {
                                                    if (it is PressInteraction.Release) {
                                                        date = true
                                                    }
                                                }
                                            }
                                        },
                                    readOnly = true,
                                )

                                var time by remember { mutableStateOf(false) }
                                if (time) TimePickerDialog(
                                    initialTime = cas,
                                    onTimeChange = {
                                        time = false
                                        cas = it
                                    },
                                    title = {
                                        Text("Vyberte čas")
                                    },
                                    onDismissRequest = {
                                        time = false
                                    },
                                )
                                OutlinedTextField(
                                    value = cas.noSeconds().asString(),
                                    onValueChange = {},
                                    Modifier
                                        .weight(1F)
                                        .onKeyEvent {
                                            if (it.key == Key.Enter) {
                                                time = true
                                            }
                                            return@onKeyEvent it.key == Key.Enter
                                        },
                                    label = {
                                        Text("Čas")
                                    },
                                    singleLine = true,
                                    interactionSource = remember { MutableInteractionSource() }
                                        .also { interactionSource ->
                                            LaunchedEffect(interactionSource) {
                                                interactionSource.interactions.collect {
                                                    if (it is PressInteraction.Release) {
                                                        time = true
                                                    }
                                                }
                                            }
                                        },
                                    readOnly = true,
                                )
                            }

                            var expanded by rememberSaveable { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                Modifier
                                    .fillMaxWidth(),
                            ) {
                                CompositionLocalProvider(
                                    LocalTextInputService provides null
                                ) {
                                    OutlinedTextField(
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        readOnly = true,
                                        value = remember(ucastnici) {
                                            seznamUcastniku.filter { it.id in ucastnici }.joinToString { it.jmeno }
                                        },
                                        onValueChange = {},
                                        label = { Text("Účastníci") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    )
                                }
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                ) {
                                    seznamUcastniku.forEach { ucastnik ->
                                        DropdownMenuItem(
                                            text = { Text(ucastnik.jmeno) },
                                            onClick = {
                                                ucastnici =
                                                    if ((ucastnik.id) in ucastnici) ucastnici - ucastnik.id else ucastnici + ucastnik.id
                                            },
                                            leadingIcon = {
                                                if (remember(ucastnici) { ucastnik.id in ucastnici }) {
                                                    Icon(Icons.Default.Check, "Vybráno")
                                                }
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 16.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    schovat()
                                }
                            ) {
                                Text(text = "Zrušit")
                            }
                            val scope = rememberCoroutineScope()
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        schovat()
                                        delay(300L)
                                        poVybrani(
                                            Utrata(
                                                datum = datum,
                                                cas = cas,
                                                cena = cena,
                                                nazev = nazev,
                                                ucastnici = ucastnici,
                                            )
                                        )
                                    }
                                }
                            ) {
                                Text(text = "Uložit")
                            }
                        }
                    }
                }
            }
        }
    }
}