package de.morhenn.seven_wonders_calculator.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Sets light or dark status bar icons. Every screen declares its own color instead of restoring
 * the previous one, which goes wrong when two screens overlap during a transition.
 */
@Composable
fun StatusBarIcons(light: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !light
    }
}

/** Icons that match the theme, for screens with a regular top bar. */
@Composable
fun ThemedStatusBarIcons() = StatusBarIcons(light = isSystemInDarkTheme())

/**
 * For screens that start with a dark hero under the status bar: light icons while the hero is
 * visible; once it scrolls away, a surface-colored scrim keeps content from running under the
 * icons and the icons follow the theme again.
 */
@Composable
fun BoxScope.HeroStatusBar(heroVisible: Boolean) {
    StatusBarIcons(light = heroVisible || isSystemInDarkTheme())
    AnimatedVisibility(!heroVisible, Modifier.align(Alignment.TopCenter), enter = fadeIn(), exit = fadeOut()) {
        Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(MaterialTheme.colorScheme.surface))
    }
}
