package cz.rblaha15.seznamUtrat.utraty

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cz.rblaha15.seznamUtrat.R
import cz.rblaha15.seznamUtrat.UtratyRepository
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NovejDialog(
    pocatecniUtrata: Utrata,
    zobrazit: Boolean,
    schovat: () -> Unit,
    repo: UtratyRepository,
    sNazvem: Boolean = true,
    sCenou: Boolean = true,
    naNejakeUcastniky: Boolean = false,
    sJinymDatem: Boolean = false,
    poVybrani: (Utrata) -> Unit,
) {
    if (zobrazit) {
        var nazev by remember { mutableStateOf(pocatecniUtrata.nazev) }
        var cena by remember { mutableStateOf(if (pocatecniUtrata.cena == 0F) "" else pocatecniUtrata.cena.toString()) }
        var datum by remember {
            mutableStateOf(pocatecniUtrata.datum
                .removeSuffix(".")
                .split(". ")
                .let { Datum(it[0].toInt(), it[1].toInt()) })
        }
        var cas by remember {
            mutableStateOf(pocatecniUtrata.cas
                .split(":")
                .let { Cas(it[0].toInt(), it[1].toInt(), it[2].toInt()) })
        }

        var ucastnici by remember { mutableStateOf(pocatecniUtrata.ucastnici) }
        Dialog(
            onDismissRequest = {
                schovat()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {

            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = { Text("Upravit útatu") }
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
                            .verticalScroll(rememberScrollState())
                            .padding(all = 8.dp)
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            singleLine = true,
                            keyboardActions = KeyboardActions {

                            }
                        )
                        OutlinedTextField(
                            value = cena,
                            onValueChange = {
                                cena = it
                            },
                            label = {
                                Text(text = "Cena")
                            },
                            trailingIcon = {
                                Text(text = repo.mena)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            singleLine = true,
                        )
                        if (sJinymDatem) Column(
                            modifier = Modifier
                                .padding(all = 8.dp)
                                .fillMaxWidth()
                        ) {


                            Text(
                                text = "Vyberte datum a čas, na které chcete útratu zadat:",
                                //style = MaterialTheme.typography.labelLarge,
                            )

                            AndroidView(
                                factory = { context ->
                                    LayoutInflater.from(context).inflate(R.layout.calendar, null).rootView
                                        .let { it as DatePicker }
                                        .apply {
                                            init(Calendar.getInstance()[Calendar.YEAR], datum.second - 1, datum.first) { _, _, m, d ->
                                                datum = Datum(d, m + 1)
                                            }

                                            firstDayOfWeek = Calendar.MONDAY
                                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                        }
                                },
                                update = { view ->
                                    view.updateDate(Calendar.getInstance()[Calendar.YEAR], datum.second - 1, datum.first)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            AndroidView(
                                factory = { context ->
                                    LayoutInflater.from(context).inflate(R.layout.time, null).rootView
                                        .let { it as TimePicker }
                                        .apply {

                                            hour = cas.first
                                            minute = cas.second
                                            setIs24HourView(true)
                                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                            setOnTimeChangedListener { _, h, m ->
                                                cas = Cas(h, m, 0)
                                            }
                                        }
                                },
                                update = { view ->
                                    view.hour = cas.first
                                    view.minute = cas.second
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                        if (naNejakeUcastniky) Column(
                            modifier = Modifier
                                .padding(all = 8.dp)
                        ) {
                            Text(text = "Vyberte účastníky, kterým chcete útratu přiřadit:")
                            repo.seznamUcastniku.forEach { ucastnik ->
                                ListItem(
                                    headlineText = { Text(text = ucastnik.jmeno) },
                                    leadingContent = {
                                        Checkbox(
                                            checked = ucastnik.id in ucastnici,
                                            onCheckedChange = {
                                                ucastnici =
                                                    if (ucastnik.id in ucastnici) ucastnici - ucastnik.id else ucastnici + ucastnik.id
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .clickable {
                                            ucastnici =
                                                if (ucastnik.id in ucastnici) ucastnici - ucastnik.id else ucastnici + ucastnik.id
                                        }
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    schovat()
                                }
                            ) {
                                Text(text = "Zrušit")
                            }
                            TextButton(
                                onClick = {
                                    poVybrani(Utrata(
                                        datum = if (sJinymDatem) datum.asString() else pocatecniUtrata.datum,
                                        cas = if (sJinymDatem) cas.asString() else pocatecniUtrata.cas,
                                        cena = if (sCenou) cena.toFloat() else pocatecniUtrata.cena,
                                        nazev = if (sNazvem) nazev else pocatecniUtrata.nazev,
                                        ucastnici = if (naNejakeUcastniky) ucastnici else pocatecniUtrata.ucastnici,
                                    ))
                                    schovat()
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

fun date(year: Int, month: Int, day: Int): Date = Calendar.getInstance().apply { set(year, month, day) }.time