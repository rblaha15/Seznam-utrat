package cz.rblaha15.seznamUtrat

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH


class MainActivity : AppCompatActivity() {

    companion object {

        fun Calendar.toDatum() = Datum(get(DAY_OF_MONTH), get(MONTH) + 1)
        fun Datum.asString() = "$first. $second."
        fun Cas.asString() = "$first:$second:$third"
        fun Double.toString(decimalPlaces: Int) = toString()
            .split(".")
            .mapIndexed { i, s -> if (i == 1) s.take(decimalPlaces) else s }
            .joinToString(",")
        fun <T> List<T>.mutate(transform: MutableList<T>.() -> Unit) = buildList {
            addAll(this@mutate)
            transform()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = UtratyRepositoryImpl(this)
        setContent {
            SeznamUtratTheme {
                // A surface container using the 'background' color from the theme
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

typealias Datum = Pair<Int, Int>
typealias Cas = Triple<Int, Int, Int>