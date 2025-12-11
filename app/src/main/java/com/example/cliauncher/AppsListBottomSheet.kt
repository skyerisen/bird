package com.example.cliauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppsListBottomSheet(
    viewModel: TerminalViewModel,
    onDismiss: () -> Unit,
    context: Context
) {
    val colorScheme = MaterialTheme.colorScheme
    val packageManager = context.packageManager
    val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pinnedApps by remember {
        mutableStateOf(prefs.getStringSet("pinned_apps", emptySet()) ?: emptySet())
    }
    var showMenuForPackage by remember { mutableStateOf<String?>(null) }

    // Get all apps
    val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
    val allApps = remember(pinnedApps) {
        packageManager.queryIntentActivities(intent, 0)
            .map { resolveInfo ->
                val label = resolveInfo.loadLabel(packageManager).toString()
                val packageName = resolveInfo.activityInfo.packageName
                val icon = try {
                    resolveInfo.loadIcon(packageManager).toBitmap().asImageBitmap()
                } catch (e: Exception) {
                    null
                }
                Triple(label, packageName, icon)
            }
            .sortedWith(compareBy(
                { !pinnedApps.contains(it.second) }, // Pinned first
                { it.first.lowercase() } // Then alphabetical
            ))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "All Apps",
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${allApps.size} apps",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(allApps) { (label, packageName, icon) ->
                    val isPinned = pinnedApps.contains(packageName)

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isPinned)
                                        colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else
                                        colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .combinedClickable(
                                    onClick = {
                                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                                        launchIntent?.let {
                                            context.startActivity(it)
                                            onDismiss()
                                        }
                                    },
                                    onLongClick = {
                                        showMenuForPackage = packageName
                                    }
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            icon?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = label,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            } ?: Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label.firstOrNull()?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurface,
                                    maxLines = 2
                                )
                            }

                            if (isPinned) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = "Pinned",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Context menu
                        if (showMenuForPackage == packageName) {
                            DropdownMenu(
                                expanded = true,
                                onDismissRequest = { showMenuForPackage = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (isPinned) "Unpin" else "Pin to top") },
                                    onClick = {
                                        val newPinnedApps = pinnedApps.toMutableSet()
                                        if (isPinned) {
                                            newPinnedApps.remove(packageName)
                                        } else {
                                            newPinnedApps.add(packageName)
                                        }
                                        pinnedApps = newPinnedApps
                                        prefs.edit().putStringSet("pinned_apps", newPinnedApps).apply()
                                        showMenuForPackage = null
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("App settings") },
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.parse("package:$packageName")
                                        }
                                        context.startActivity(intent)
                                        showMenuForPackage = null
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Uninstall") },
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DELETE).apply {
                                            data = Uri.parse("package:$packageName")
                                        }
                                        context.startActivity(intent)
                                        showMenuForPackage = null
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
