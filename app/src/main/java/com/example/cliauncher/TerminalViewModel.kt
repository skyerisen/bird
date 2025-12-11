package com.example.cliauncher

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    private val _output = MutableStateFlow<List<String>>(emptyList())
    val output: StateFlow<List<String>> = _output

    private val _events = Channel<Event>()
    val events = _events.receiveAsFlow()

    private val packageManager: PackageManager = application.packageManager

    fun processInput(input: String) {
        viewModelScope.launch {
            val newOutput = _output.value.toMutableList()
            newOutput.add("user@local >>> $input")

            when {
                input.startsWith("web ") -> {
                    val query = input.substring(4)
                    newOutput.add("Searching \"$query\" on the web")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
                    _events.send(Event.LaunchIntent(intent))
                }
                else -> {
                    val apps = findApps(input)
                    if (apps.isNotEmpty()) {
                        if (apps.size == 1) {
                            val app = apps.first()
                            newOutput.add("Opening \"${app.label}\"")
                            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
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

    fun findApps(query: String): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        return resolveInfoList
            .mapNotNull { resolveInfo ->
                val label = resolveInfo.loadLabel(packageManager).toString()
                if (label.contains(query, ignoreCase = true)) {
                    AppInfo(label, resolveInfo.activityInfo.packageName)
                } else {
                    null
                }
            }
    }
    
    sealed class Event {
        data class LaunchIntent(val intent: Intent): Event()
    }
}

data class AppInfo(val label: String, val packageName: String)