package com.shiftline.bird

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shiftline.bird.data.repository.AppsRepository
import com.shiftline.bird.data.repository.LauncherPreferencesRepository
import com.shiftline.bird.domain.model.AppItem
import com.shiftline.bird.domain.usecase.GetInstalledAppsUseCase
import com.shiftline.bird.domain.usecase.ManagePinnedAppsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppsListViewModel(application: Application) : AndroidViewModel(application) {

    private val appsRepository = AppsRepository(application)
    private val preferencesRepository = LauncherPreferencesRepository(application)
    private val getInstalledAppsUseCase = GetInstalledAppsUseCase(appsRepository, preferencesRepository)
    private val managePinnedAppsUseCase = ManagePinnedAppsUseCase(preferencesRepository)

    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps: StateFlow<List<AppItem>> = _apps

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _apps.value = getInstalledAppsUseCase()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun togglePinApp(packageName: String) {
        managePinnedAppsUseCase.togglePinApp(packageName)
        loadApps()
    }

    fun getLaunchIntent(packageName: String) = appsRepository.getLaunchIntent(packageName)
}
