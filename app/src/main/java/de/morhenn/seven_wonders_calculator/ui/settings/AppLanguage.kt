package de.morhenn.seven_wonders_calculator.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** In-app language. [SYSTEM] follows the device language. */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null), GERMAN("de"), ENGLISH("en");

    companion object {
        fun current(): AppLanguage {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.isEmpty) return SYSTEM
            return entries.firstOrNull { it.tag != null && locales[0]?.language == it.tag } ?: SYSTEM
        }

        /** Applies the language; the activity is recreated by AppCompat. */
        fun apply(language: AppLanguage) {
            val locales = language.tag?.let { LocaleListCompat.forLanguageTags(it) } ?: LocaleListCompat.getEmptyLocaleList()
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}
