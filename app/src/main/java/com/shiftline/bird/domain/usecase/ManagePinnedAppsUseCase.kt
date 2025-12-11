package com.shiftline.bird.domain.usecase

import com.shiftline.bird.data.repository.LauncherPreferencesRepository

class ManagePinnedAppsUseCase(
    private val preferencesRepository: LauncherPreferencesRepository
) {
    fun getPinnedApps(): Set<String> {
        return preferencesRepository.getPinnedApps()
    }

    fun togglePinApp(packageName: String) {
        val pinnedApps = preferencesRepository.getPinnedApps().toMutableSet()
        if (pinnedApps.contains(packageName)) {
            pinnedApps.remove(packageName)
        } else {
            pinnedApps.add(packageName)
        }
        preferencesRepository.savePinnedApps(pinnedApps)
    }
}
