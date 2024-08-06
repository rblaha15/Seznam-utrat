package cz.rblaha15.seznamUtrat.ui.seznam

import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marosseleng.compose.material3.datetimepickers.date.ui.dialog.DatePickerDialog
import com.marosseleng.compose.material3.datetimepickers.time.domain.noSeconds
import com.marosseleng.compose.material3.datetimepickers.time.ui.dialog.TimePickerDialog
import cz.rblaha15.seznamUtrat.data.Ucastnik
import cz.rblaha15.seznamUtrat.data.Utrata
import cz.rblaha15.seznamUtrat.data.serializers.asString
import cz.rblaha15.seznamUtrat.toString
import cz.rblaha15.seznamUtrat.ui.savers.StateOfListOfUUIDSaver
import cz.rblaha15.seznamUtrat.ui.savers.StateOfLocalDateSaver
import cz.rblaha15.seznamUtrat.ui.savers.StateOfLocalTimeSaver
import kotlinx.coroutines.launch

@Composable
fun BackHandler(
    enabled: Boolean = true,
    handleOnBackCancelled: () -> Unit = {},
    handleOnBackProgressed: (BackEventCompat) -> Unit = {},
    handleOnBackStarted: (BackEventCompat) -> Unit = {},
    handleOnBackPressed: () -> Unit,
) {
    val currentOnBackCancelled by rememberUpdatedState(handleOnBackCancelled)
    val currentOnBackProgressed by rememberUpdatedState(handleOnBackProgressed)
    val currentOnBackStarted by rememberUpdatedState(handleOnBackStarted)
    val currentOnBackPressed by rememberUpdatedState(handleOnBackPressed)

    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() = currentOnBackPressed()
            override fun handleOnBackCancelled() = currentOnBackCancelled()
            override fun handleOnBackProgressed(backEvent: BackEventCompat) = currentOnBackProgressed(backEvent)
            override fun handleOnBackStarted(backEvent: BackEventCompat) = currentOnBackStarted(backEvent)
        }
    }
    SideEffect {
        backCallback.isEnabled = enabled
    }

    val backDispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.onBackPressedDispatcher
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, backDispatcher) {
        backDispatcher.addCallback(lifecycleOwner, backCallback)
        onDispose {
            backCallback.remove()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun UpravitUtratuSheet(
    pocatecniUtrata: Utrata,
    seznamUcastniku: List<Ucastnik>,
    mena: String,
    animatable: Animatable<Float, AnimationVector1D>,
    poVybrani: (Utrata) -> Unit,
) {
    val scope = rememberCoroutineScope()
    BackHandler(
        enabled = animatable.value < 1F,
        handleOnBackPressed = {
            scope.launch {
                animatable.animateTo(1F, tween(300))
            }
        },
        handleOnBackCancelled = {
            scope.launch {
                animatable.animateTo(0F, tween(100))
            }
        },
        handleOnBackProgressed = {
            scope.launch {
                animatable.snapTo(it.progress.let {
                    if (it > .1) it / 20F + .1F
                    else it
                })
            }
        },
    )

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(animatable.velocity) {
        when {
            animatable.velocity < 0 -> focusRequester.requestFocus()
            animatable.velocity > 0 -> focusRequester.freeFocus()
        }
    }

    if (animatable.value < 1F) Surface(
        Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = size.height * animatable.value
                    scaleX = 1F - animatable.value
                    scaleY = 1F - animatable.value
                }
                .fillMaxSize(),
        ) {
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
                        Content(
                            focusRequester,
                            mena,
                            seznamUcastniku,
                            animatable,
                            poVybrani,
                            pocatecniUtrata
                        )
                    }
                }
            }
        }
    }
}

context(ColumnScope)
@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
private fun Content(
    focusRequester: FocusRequester,
    mena: String,
    seznamUcastniku: List<Ucastnik>,
    animatable: Animatable<Float, AnimationVector1D>,
    poVybrani: (Utrata) -> Unit,
    pocatecniUtrata: Utrata,
) {
    val scope = rememberCoroutineScope()
    var nazev by rememberSaveable { mutableStateOf(pocatecniUtrata.nazev) }
    var cena by rememberSaveable { mutableFloatStateOf(pocatecniUtrata.cena) }
    var datum by rememberSaveable(saver = StateOfLocalDateSaver) { mutableStateOf(pocatecniUtrata.datum) }
    var cas by rememberSaveable(saver = StateOfLocalTimeSaver) { mutableStateOf(pocatecniUtrata.cas) }
    var ucastnici by rememberSaveable(saver = StateOfListOfUUIDSaver) { mutableStateOf(pocatecniUtrata.ucastnici) }
    val id by rememberSaveable { mutableStateOf(pocatecniUtrata.id.toString()) }
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .weight(1F)
    ) {
        if (showUUIDs) Text(text = "UUID: ${pocatecniUtrata.id}")
        if (showUUIDs) Text(text = "UUID: $id")
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
                .focusRequester(focusRequester)
                .padding(top = 8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Down)
            },
        )
        var localCena by remember { mutableStateOf(if (cena == 0F) "" else cena.toString(2)) }
        OutlinedTextField(
            value = localCena,
            onValueChange = {
                localCena = it
                cena = if (it == "") 0F
                else it.replace(",", ".").toFloatOrNull() ?: return@OutlinedTextField
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
            supportingText = {
                Text("Uloženo: ${cena.toString(2)}")
            }
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

        var expanded by remember { mutableStateOf(false) }
        if (seznamUcastniku.isNotEmpty()) ExposedDropdownMenuBox(
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
                    value = remember(ucastnici, seznamUcastniku) {
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
                        trailingIcon = if (showUUIDs) {{
                            Text(text = "UUID: ${ucastnik.id.toString().split("-").let {
                                "${it[0]}\n-${it[1]}-${it[2]}-${it[3]}\n-${it[4]}"
                            }}", fontSize = 10.sp)
                        }} else null,
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
                scope.launch {
                    animatable.animateTo(1F, tween(500))
                }
            }
        ) {
            Text(text = "Zrušit")
        }
        rememberCoroutineScope()
        TextButton(
            onClick = {
                poVybrani(
                    Utrata(
                        datum = datum,
                        cas = cas,
                        cena = cena,
                        nazev = nazev,
                        ucastnici = ucastnici,
                        id = pocatecniUtrata.id
                    )
                )
                scope.launch {
                    animatable.animateTo(1F, tween(500))
                }
            }
        ) {
            Text(text = "Uložit")
        }
    }
}