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

fun Double.toString(decimalPlaces: Int) = toString()
    .split(".")
    .mapIndexed { i, s -> if (i == 1) s.take(decimalPlaces) else s }
    .joinToString(",")

fun <T> List<T>.mutate(transform: MutableList<T>.() -> Unit) = buildList {
    addAll(this@mutate)
    transform()
}