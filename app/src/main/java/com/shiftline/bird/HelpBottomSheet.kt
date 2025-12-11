package com.shiftline.bird

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                // Header with back button if navigation is available
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onNavigateToSettings != null) {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Settings",
                                tint = colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = "My Bird",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your guide to Bird Launcher",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Commands Section
                SectionTitle(text = "Commands", colorScheme = colorScheme)
                Spacer(modifier = Modifier.height(12.dp))

                CommandItem(
                    command = "clear",
                    description = "Clears the terminal screen and removes all previous output",
                    colorScheme = colorScheme
                )

                CommandItem(
                    command = "web <query>",
                    description = "Search the web using Google. Example: web kotlin tutorials",
                    example = "web android development",
                    colorScheme = colorScheme
                )

                CommandItem(
                    command = "help",
                    description = "Opens this help screen with all available commands and tips",
                    colorScheme = colorScheme
                )

                CommandItem(
                    command = "<app name>",
                    description = "Launch any installed app by typing its name. The search is case-insensitive and supports partial matches",
                    example = "chrome, gmail, settings",
                    colorScheme = colorScheme
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tips Section
                SectionTitle(text = "Tips & Tricks", colorScheme = colorScheme)
                Spacer(modifier = Modifier.height(12.dp))

                TipItem(
                    title = "Pin Your Favorites",
                    description = "Long-press any app in the apps list to pin it to the top for instant access",
                    colorScheme = colorScheme
                )

                TipItem(
                    title = "Quick App Launch",
                    description = "Type just part of an app name to find it. For example, 'mess' will match 'Messages'",
                    colorScheme = colorScheme
                )

                TipItem(
                    title = "View All Apps",
                    description = "Swipe up or tap the apps button in the bottom navigation to see all installed applications with their icons",
                    colorScheme = colorScheme
                )

                TipItem(
                    title = "Multiple Matches",
                    description = "When multiple apps match your query, they'll all be listed. Tap any name to launch that app",
                    colorScheme = colorScheme
                )

                TipItem(
                    title = "Customize Your Experience",
                    description = "Access settings to customize the prompt, wallpaper effects, and terminal appearance to match your style",
                    colorScheme = colorScheme
                )

                Spacer(modifier = Modifier.height(24.dp))

                // About Section
                SectionTitle(text = "About", colorScheme = colorScheme)
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bird Launcher",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A minimalist command-line style launcher for Android",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, colorScheme: ColorScheme) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CommandItem(
    command: String,
    description: String,
    example: String? = null,
    colorScheme: ColorScheme
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Command
            Row(
                modifier = Modifier
                    .background(
                        color = colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = command,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface,
                lineHeight = 20.sp
            )

            // Example if provided
            example?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Example: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun TipItem(
    title: String,
    description: String,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Bullet point
        Box(
            modifier = Modifier
                .size(8.dp)
                .offset(y = 6.dp)
                .background(
                    color = colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}
