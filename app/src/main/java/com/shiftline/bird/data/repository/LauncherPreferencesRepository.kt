package com.shiftline.bird.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.shiftline.bird.domain.model.LauncherSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: Flow<LauncherSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): LauncherSettings {
        return LauncherSettings(
            username = prefs.getString("username", "user") ?: "user",
            showDate = prefs.getBoolean("show_date", true),
            showUsername = prefs.getBoolean("show_username", true),
            showHostname = prefs.getBoolean("show_hostname", true),
            hostname = prefs.getString("hostname", "local") ?: "local",
            promptArrow = prefs.getString("prompt_arrow", ">>>") ?: ">>>",
            showArrow = prefs.getBoolean("show_arrow", true),
            blurRadius = prefs.getFloat("blur_radius", 12f),
            overlayAlpha = prefs.getFloat("overlay_alpha", 0.7f),
            showTerminalLabel = prefs.getBoolean("show_terminal_label", true)
        )
    }

    fun saveSettings(settings: LauncherSettings) {
        prefs.edit()
            .putString("username", settings.username)
            .putBoolean("show_date", settings.showDate)
            .putBoolean("show_username", settings.showUsername)
            .putBoolean("show_hostname", settings.showHostname)
            .putString("hostname", settings.hostname)
            .putString("prompt_arrow", settings.promptArrow)
            .putBoolean("show_arrow", settings.showArrow)
            .putFloat("blur_radius", settings.blurRadius)
            .putFloat("overlay_alpha", settings.overlayAlpha)
            .putBoolean("show_terminal_label", settings.showTerminalLabel)
            .apply()

        _settingsFlow.value = settings
    }

    fun getPinnedApps(): Set<String> {
        return prefs.getStringSet("pinned_apps", emptySet()) ?: emptySet()
    }

    fun savePinnedApps(pinnedApps: Set<String>) {
        prefs.edit().putStringSet("pinned_apps", pinnedApps).apply()
    }
}
