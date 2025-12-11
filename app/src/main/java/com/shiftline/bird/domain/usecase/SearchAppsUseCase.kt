package com.shiftline.bird.domain.usecase

import com.shiftline.bird.data.repository.AppsRepository
import com.shiftline.bird.domain.model.AppItem

class SearchAppsUseCase(
    private val appsRepository: AppsRepository
) {
    suspend operator fun invoke(query: String): List<AppItem> {
        return appsRepository.searchApps(query)
    }
}
