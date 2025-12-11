package com.shiftline.bird.domain.usecase

import com.shiftline.bird.data.repository.LauncherPreferencesRepository
import com.shiftline.bird.domain.model.LauncherSettings
import kotlinx.coroutines.flow.Flow

class LauncherSettingsUseCase(
    private val preferencesRepository: LauncherPreferencesRepository
) {
    fun getSettings(): Flow<LauncherSettings> {
        return preferencesRepository.settingsFlow
    }

    fun saveSettings(settings: LauncherSettings) {
        preferencesRepository.saveSettings(settings)
    }
}
