package cz.rblaha15.seznamUtrat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.rblaha15.seznamUtrat.data.LocalDataSource
import cz.rblaha15.seznamUtrat.data.LocalDataSourceImpl
import cz.rblaha15.seznamUtrat.ui.seznam.Seznam
import cz.rblaha15.seznamUtrat.ui.theme.SeznamUtratTheme
import cz.rblaha15.seznamUtrat.ui.ucastnici.Ucastnici
import kotlin.math.pow
import kotlin.math.roundToLong

private lateinit var ds: LocalDataSource

class MainActivity : ComponentActivity() {
    init {
        if (!::ds.isInitialized)
            ds = LocalDataSourceImpl(
                dataStore = PreferenceDataStoreFactory.create(
                    migrations = listOf(
                        SharedPreferencesMigration(
                            produceSharedPreferences = {
                                getSharedPreferences("PREFS_SEZNAM_UTRAT_RBLAHA15", MODE_PRIVATE)
                            }
                        )
                    )
                ) {
                    this.preferencesDataStoreFile("PREFS_SEZNAM_UTRAT_RBLAHA15")
                }
            )
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
                        startDestination = "seznam",
                        popEnterTransition = {
                            scaleIn(
                                animationSpec = tween(
                                    durationMillis = 100,
                                    delayMillis = 35,
                                ),
                                initialScale = 1.1F,
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = 100,
                                    delayMillis = 35,
                                ),
                            )
                        },
                        popExitTransition = {
                            scaleOut(
                                targetScale = 0.9F,
                            ) + fadeOut(
                                animationSpec = tween(
                                    durationMillis = 35,
                                    easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f),
                                ),
                            )
                        },
                    ) {
                        composable(route = "seznam") {
                            Seznam(ds, navController)
                        }
                        composable(route = "ucastnici") {
                            Ucastnici(ds, navController)
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
