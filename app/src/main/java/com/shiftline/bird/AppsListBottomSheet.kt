package com.shiftline.bird

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.shiftline.bird.data.AppIconFetcher

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppsListBottomSheet(
    onDismiss: () -> Unit,
    appsListViewModel: AppsListViewModel = viewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val apps by appsListViewModel.apps.collectAsState()
    val isLoading by appsListViewModel.isLoading.collectAsState()

    var showMenuForPackage by remember { mutableStateOf<String?>(null) }

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
                text = "${apps.size} apps",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->

                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (app.isPinned)
                                            colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else
                                            colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .combinedClickable(
                                        onClick = {
                                            val launchIntent = appsListViewModel.getLaunchIntent(app.packageName)
                                            launchIntent?.let {
                                                context.startActivity(it)
                                                onDismiss()
                                            }
                                        },
                                        onLongClick = {
                                            showMenuForPackage = app.packageName
                                        }
                                    )
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Load app icon efficiently with caching via remember
                                val appIcon = remember(app.packageName) {
                                    try {
                                        context.packageManager.getApplicationIcon(app.packageName)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                SubcomposeAsyncImage(
                                    model = appIcon,
                                    contentDescription = app.label,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit,
                                    loading = {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    },
                                    error = {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = app.label.firstOrNull()?.uppercase() ?: "?",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colorScheme.onSurface,
                                        maxLines = 2
                                    )
                                }

                                if (app.isPinned) {
                                    Icon(
                                        imageVector = Icons.Filled.PushPin,
                                        contentDescription = "Pinned",
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Context menu
                            if (showMenuForPackage == app.packageName) {
                                DropdownMenu(
                                    expanded = true,
                                    onDismissRequest = { showMenuForPackage = null }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (app.isPinned) "Unpin" else "Pin to top") },
                                        onClick = {
                                            appsListViewModel.togglePinApp(app.packageName)
                                            showMenuForPackage = null
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (app.isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("App settings") },
                                        onClick = {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${app.packageName}")
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
                                                data = Uri.parse("package:${app.packageName}")
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
}
