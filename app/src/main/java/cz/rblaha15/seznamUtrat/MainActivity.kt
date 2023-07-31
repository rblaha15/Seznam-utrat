package cz.rblaha15.seznamUtrat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.rblaha15.seznamUtrat.ui.SeznamScreen
import cz.rblaha15.seznamUtrat.ui.UcastniciScreen
import cz.rblaha15.seznamUtrat.ui.theme.SeznamUtratTheme
import kotlin.math.pow
import kotlin.math.roundToLong

private lateinit var repo: UtratyRepository

class MainActivity : ComponentActivity() {
    init {
        if (!::repo.isInitialized)
            repo = UtratyRepositoryImpl(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeznamUtratTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "seznam"
                    ) {
                        composable(route = "seznam") {
                            SeznamScreen(repo) {
                                navController.navigate(it)
                            }
                        }
                        composable(route = "ucastnici") {
                            UcastniciScreen(repo)
                        }
                    }
                }
            }
        }
    }
}

fun Double.toString(decimalPlaces: Int) = this
    .times(10F.pow(decimalPlaces))
    .roundToLong()
    .toDouble()
    .div(10F.pow(decimalPlaces))
    .toString()
    .split(".")
    .let {
        if (it[1] == "0") it.dropLast(1)
        else if (it[1].length == 1) listOf(it[0], "${it[1]}0")
        else it
    }
    .joinToString(",")

fun Float.toString(decimalPlaces: Int) = toDouble().toString(decimalPlaces)

fun <T> List<T>.mutate(transform: MutableList<T>.() -> Unit) = buildList {
    addAll(this@mutate)
    transform()
}