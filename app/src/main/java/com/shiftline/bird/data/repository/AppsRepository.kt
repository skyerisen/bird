package com.shiftline.bird.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.shiftline.bird.domain.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppsRepository(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledApps(pinnedApps: Set<String>): List<AppItem> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        packageManager.queryIntentActivities(intent, 0)
            .map { resolveInfo ->
                val label = resolveInfo.loadLabel(packageManager).toString()
                val packageName = resolveInfo.activityInfo.packageName
                val isPinned = pinnedApps.contains(packageName)
                AppItem(label, packageName, isPinned)
            }
            .sortedWith(compareBy(
                { !it.isPinned }, // Pinned first
                { it.label.lowercase() } // Then alphabetical
            ))
    }

    suspend fun searchApps(query: String): List<AppItem> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { resolveInfo ->
                val label = resolveInfo.loadLabel(packageManager).toString()
                if (label.contains(query, ignoreCase = true)) {
                    val packageName = resolveInfo.activityInfo.packageName
                    AppItem(label, packageName)
                } else {
                    null
                }
            }
    }

    fun getLaunchIntent(packageName: String): Intent? {
        return packageManager.getLaunchIntentForPackage(packageName)
    }
}
