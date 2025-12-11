package com.shiftline.bird.domain.usecase

import com.shiftline.bird.data.repository.AppsRepository
import com.shiftline.bird.data.repository.LauncherPreferencesRepository
import com.shiftline.bird.domain.model.AppItem

class GetInstalledAppsUseCase(
    private val appsRepository: AppsRepository,
    private val preferencesRepository: LauncherPreferencesRepository
) {
    suspend operator fun invoke(): List<AppItem> {
        val pinnedApps = preferencesRepository.getPinnedApps()
        return appsRepository.getInstalledApps(pinnedApps)
    }
}
