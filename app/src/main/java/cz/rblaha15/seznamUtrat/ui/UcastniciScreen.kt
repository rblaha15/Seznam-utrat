package cz.rblaha15.seznamUtrat.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.rblaha15.seznamUtrat.*
import cz.rblaha15.seznamUtrat.MainActivity.Companion.mutate
import cz.rblaha15.seznamUtrat.MainActivity.Companion.toString
import cz.rblaha15.seznamUtrat.ui.theme.SeznamUtratTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UcastniciScreen(
    repo: UtratyRepository,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Účastníci") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    repo.seznamUcastniku += Ucastnik(
                        jmeno = "",
                    )
                },
            ) {
                Icon(Icons.Default.PersonAdd, "Přidat účastníka")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            items(repo.seznamUcastniku, key = { it.id }) { ucastnik ->
                ListItem(
                    headlineText = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextField(
                                value = ucastnik.jmeno,
                                onValueChange = {
                                    repo.seznamUcastniku = repo.seznamUcastniku.mutate {
                                        val i = indexOf(ucastnik)
                                        this[i] = this[i].copy(jmeno = it)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1F)
                                        .padding(start = 8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                singleLine = true,
                            )
                            Text(
                                text = ucastnik.utraty(repo)
                                    .sumOf { (it.cena.toDouble() / it.ucastnici.size) }
                                    .toString(2) + " " + repo.mena,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                            )
                            Checkbox(
                                checked = ucastnik.aktivovan,
                                onCheckedChange = {
                                    repo.seznamUcastniku = repo.seznamUcastniku.mutate {
                                        val i = indexOf(ucastnik)
                                        this[i] = this[i].copy(aktivovan = it)
                                    }
                                },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                            )
                            IconButton(
                                onClick = {
                                    repo.seznamUcastniku = repo.seznamUcastniku.mutate {
                                        remove(ucastnik)
                                    }
                                },
                                enabled = ucastnik.utraty(repo).isEmpty(),
                                modifier = Modifier
                                    .padding(start = 8.dp)
                            ) {
                                Icon(Icons.Default.Clear, "odebrat")
                            }
                        }
                    }
                )
            }
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