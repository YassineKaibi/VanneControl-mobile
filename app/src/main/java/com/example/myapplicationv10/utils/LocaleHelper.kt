package com.example.myapplicationv10.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {

    private const val SELECTED_LANGUAGE = "selected_language"
    private const val LANGUAGE_ENGLISH = "en"
    private const val LANGUAGE_FRENCH = "fr"

    /**
     * Set and persist the app language
     */
    fun setLocale(context: Context, languageCode: String): Context {
        persist(context, languageCode)
        return updateResources(context, languageCode)
    }

    /**
     * Get the currently selected language
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }

    /**
     * Persist language preference
     */
    private fun persist(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
    }

    /**
     * Update app resources with selected language
     */
    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }

    /**
     * Get language display name
     */
    fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_FRENCH -> "Français"
            else -> "English"
        }
    }

    /**
     * Get all available languages
     */
    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language(LANGUAGE_ENGLISH, "English"),
            Language(LANGUAGE_FRENCH, "Français")
        )
    }

    data class Language(val code: String, val name: String)
}