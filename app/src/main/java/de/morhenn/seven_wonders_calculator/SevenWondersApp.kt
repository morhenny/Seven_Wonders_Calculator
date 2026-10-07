package de.morhenn.seven_wonders_calculator

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import de.morhenn.seven_wonders_calculator.data.AppDatabase
import de.morhenn.seven_wonders_calculator.data.GameRepository
import de.morhenn.seven_wonders_calculator.data.PlayerRepository
import de.morhenn.seven_wonders_calculator.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

class SevenWondersApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Manual dependency injection; the app is small enough not to need a framework. */
class AppContainer(app: Application) {
    private val database = AppDatabase.create(app)
    val players = PlayerRepository(database.players())
    val games = GameRepository(database.games())
    val settings = SettingsRepository(app)

    /** For work that must finish even when the screen that started it is gone. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Deletes every game, player and preference. The language choice is kept. */
    suspend fun resetAll() {
        withContext(Dispatchers.IO) { database.clearAllTables() }
        settings.clear()
    }
}

val CreationExtras.container: AppContainer
    get() = (this[APPLICATION_KEY] as SevenWondersApp).container
