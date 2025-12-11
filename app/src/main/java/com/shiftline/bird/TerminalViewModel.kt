package com.shiftline.bird

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shiftline.bird.data.repository.AppsRepository
import com.shiftline.bird.data.repository.LauncherPreferencesRepository
import com.shiftline.bird.domain.model.LauncherSettings
import com.shiftline.bird.domain.usecase.LauncherSettingsUseCase
import com.shiftline.bird.domain.usecase.SearchAppsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    private val appsRepository = AppsRepository(application)
    private val preferencesRepository = LauncherPreferencesRepository(application)
    private val searchAppsUseCase = SearchAppsUseCase(appsRepository)
    private val settingsUseCase = LauncherSettingsUseCase(preferencesRepository)

    private val _output = MutableStateFlow<List<String>>(emptyList())
    val output: StateFlow<List<String>> = _output

    private val _events = Channel<Event>()
    val events = _events.receiveAsFlow()

    val settings: StateFlow<LauncherSettings> = settingsUseCase.getSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherSettings())

    fun saveSettings(settings: LauncherSettings) {
        settingsUseCase.saveSettings(settings)
    }

    fun processInput(input: String) {
        viewModelScope.launch {
            val currentSettings = settings.value
            val newOutput = _output.value.toMutableList()

            val promptPrefix = buildString {
                if (currentSettings.showUsername) {
                    append(currentSettings.username)
                }
                if (currentSettings.showHostname && currentSettings.showUsername) {
                    append("@${currentSettings.hostname}")
                } else if (currentSettings.showHostname) {
                    append(currentSettings.hostname)
                }
                if (currentSettings.showArrow) {
                    append(" ${currentSettings.promptArrow} ")
                } else {
                    append(" ")
                }
            }

            newOutput.add("$promptPrefix$input")

            when {
                input.trim() == "clear" -> {
                    _output.value = emptyList()
                    return@launch
                }
                input.startsWith("web ") -> {
                    val query = input.substring(4)
                    newOutput.add("Searching \"$query\" on the web")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
                    _events.send(Event.LaunchIntent(intent))
                }
                else -> {
                    val apps = searchAppsUseCase(input)
                    if (apps.isNotEmpty()) {
                        if (apps.size == 1) {
                            val app = apps.first()
                            newOutput.add("Opening \"${app.label}\"")
                            val launchIntent = appsRepository.getLaunchIntent(app.packageName)
                            launchIntent?.let { _events.send(Event.LaunchIntent(it)) }
                        } else {
                            newOutput.add("Multiple apps found:")
                            apps.forEach { newOutput.add("- ${it.label} (${it.packageName})") }
                        }
                    } else {
                        newOutput.add("No app or data found. Aborting...")
                    }
                }
            }
            _output.value = newOutput
        }
    }

    sealed class Event {
        data class LaunchIntent(val intent: Intent): Event()
    }
}