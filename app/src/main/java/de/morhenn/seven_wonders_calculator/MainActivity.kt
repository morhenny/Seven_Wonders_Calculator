package de.morhenn.seven_wonders_calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.morhenn.seven_wonders_calculator.ui.components.ThemedStatusBarIcons
import de.morhenn.seven_wonders_calculator.ui.home.HomeScreen
import de.morhenn.seven_wonders_calculator.ui.results.ResultsScreen
import de.morhenn.seven_wonders_calculator.ui.scoring.ScoringScreen
import de.morhenn.seven_wonders_calculator.ui.settings.SettingsScreen
import de.morhenn.seven_wonders_calculator.ui.setup.SetupScreen
import de.morhenn.seven_wonders_calculator.ui.stats.StatsScreen
import de.morhenn.seven_wonders_calculator.ui.theme.SevenWondersTheme
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable data class SetupRoute(val fromGameId: Long? = null)
@Serializable data class ScoringRoute(val gameId: Long)
@Serializable data class ResultsRoute(val gameId: Long)
@Serializable object StatsRoute
@Serializable object SettingsRoute

/** AppCompat so the in-app language choice also works on Android 12 and lower. */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SevenWondersTheme {
                AppNavHost(rememberNavController())
            }
        }
    }
}

/**
 * Navigation callbacks only fire while their screen is resumed. This drops double taps and
 * key repeats that would otherwise pop the start destination and leave a blank screen.
 */
private inline fun NavBackStackEntry.ifResumed(block: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) block()
}

@Composable
private fun AppNavHost(nav: NavHostController) {
    NavHost(
        navController = nav,
        startDestination = HomeRoute,
        enterTransition = { fadeIn(tween(240)) + slideInHorizontally(tween(280)) { it / 8 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(240)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(280)) { it / 8 } },
    ) {
        composable<HomeRoute> { entry ->
            HomeScreen(
                onNewGame = { entry.ifResumed { nav.navigate(SetupRoute()) } },
                onOpenGame = { id, finished ->
                    entry.ifResumed { nav.navigate(if (finished) ResultsRoute(id) else ScoringRoute(id)) }
                },
                onStats = { entry.ifResumed { nav.navigate(StatsRoute) } },
                onSettings = { entry.ifResumed { nav.navigate(SettingsRoute) } },
            )
        }
        composable<SetupRoute> { entry ->
            ThemedStatusBarIcons()
            SetupScreen(
                onBack = { entry.ifResumed { nav.popBackStack() } },
                onStarted = { id -> entry.ifResumed { nav.navigate(ScoringRoute(id)) { popUpTo(HomeRoute) } } },
            )
        }
        composable<ScoringRoute> { entry ->
            ThemedStatusBarIcons()
            ScoringScreen(
                onBack = { entry.ifResumed { nav.popBackStack() } },
                onShowResults = { id -> entry.ifResumed { nav.navigate(ResultsRoute(id)) { popUpTo(HomeRoute) } } },
            )
        }
        composable<ResultsRoute> { entry ->
            ResultsScreen(
                onBack = { entry.ifResumed { nav.popBackStack() } },
                onEdit = { id -> entry.ifResumed { nav.navigate(ScoringRoute(id)) { popUpTo(HomeRoute) } } },
                onRematch = { id -> entry.ifResumed { nav.navigate(SetupRoute(fromGameId = id)) } },
            )
        }
        composable<StatsRoute> { entry ->
            ThemedStatusBarIcons()
            StatsScreen(onBack = { entry.ifResumed { nav.popBackStack() } }) }
        composable<SettingsRoute> { entry ->
            ThemedStatusBarIcons()
            SettingsScreen(onBack = { entry.ifResumed { nav.popBackStack() } }) }
    }
}
