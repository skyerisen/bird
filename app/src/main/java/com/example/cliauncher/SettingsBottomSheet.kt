package com.example.cliauncher

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    currentUsername: String,
    currentShowDate: Boolean,
    currentShowUsername: Boolean,
    currentShowHostname: Boolean,
    currentHostname: String,
    currentPromptArrow: String,
    currentShowArrow: Boolean,
    currentBlurRadius: Float,
    currentOverlayAlpha: Float,
    currentShowTerminalLabel: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Boolean, Boolean, Boolean, String, String, Boolean, Float, Float, Boolean) -> Unit,
    context: Context
) {
    var username by remember { mutableStateOf(currentUsername) }
    var showDate by remember { mutableStateOf(currentShowDate) }
    var showUsername by remember { mutableStateOf(currentShowUsername) }
    var showHostname by remember { mutableStateOf(currentShowHostname) }
    var hostname by remember { mutableStateOf(currentHostname) }
    var promptArrow by remember { mutableStateOf(currentPromptArrow) }
    var showArrow by remember { mutableStateOf(currentShowArrow) }
    var blurRadius by remember { mutableStateOf(currentBlurRadius) }
    var overlayAlpha by remember { mutableStateOf(currentOverlayAlpha) }
    var showTerminalLabel by remember { mutableStateOf(currentShowTerminalLabel) }

    val colorScheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // System settings button
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "System Settings",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("System Settings")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Show Terminal Label
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Terminal Label",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Display \"Terminal\" text at the top",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showTerminalLabel,
                            onCheckedChange = { showTerminalLabel = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Prompt Customization",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Show Date
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Date",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Display current date and time",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showDate,
                            onCheckedChange = { showDate = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Show Username
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Username",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Display username in prompt",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showUsername,
                            onCheckedChange = { showUsername = it }
                        )
                    }

                    if (showUsername) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Username") },
                            placeholder = { Text("user") },
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Show Hostname
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Hostname",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Display hostname in prompt",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showHostname,
                            onCheckedChange = { showHostname = it }
                        )
                    }

                    if (showHostname) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = hostname,
                            onValueChange = { hostname = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Hostname") },
                            placeholder = { Text("local") },
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Show Arrow
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Prompt Arrow",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Display arrow symbol in prompt",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showArrow,
                            onCheckedChange = { showArrow = it }
                        )
                    }

                    if (showArrow) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = promptArrow,
                            onValueChange = { promptArrow = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Arrow Symbol") },
                            placeholder = { Text(">>>") },
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Prompt Preview
                Text(
                    text = "Prompt Preview:",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                val previewText = buildString {
                    if (showDate) {
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        append("[${dateFormat.format(Date())}] ")
                    }
                    if (showUsername) {
                        append(username)
                    }
                    if (showHostname && showUsername) {
                        append("@$hostname")
                    } else if (showHostname) {
                        append(hostname)
                    }
                    if (showArrow) {
                        append(" $promptArrow ")
                    } else {
                        append(" ")
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = previewText,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.primary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Wallpaper settings
                Text(
                    text = "Wallpaper Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Blur Radius
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Blur Radius",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "${blurRadius.toInt()}dp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        value = blurRadius,
                        onValueChange = { blurRadius = it },
                        valueRange = 0f..25f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Control wallpaper blur intensity",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Overlay Alpha
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overlay Opacity",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "${(overlayAlpha * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        value = overlayAlpha,
                        onValueChange = { overlayAlpha = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Control overlay darkness/lightness",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(username, showDate, showUsername, showHostname, hostname, promptArrow, showArrow, blurRadius, overlayAlpha, showTerminalLabel)
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
