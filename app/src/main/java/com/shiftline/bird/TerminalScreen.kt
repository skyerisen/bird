package com.shiftline.bird

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.shiftline.bird.ui.theme.getLauncherColors
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TerminalScreenWithPermission(viewModel: TerminalViewModel) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var wallpaperBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasPermission = isGranted }
    )

    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = android.content.Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:${context.packageName}")
                    manageStorageLauncher.launch(intent)
                } catch (e: Exception) {
                    val intent = android.content.Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageStorageLauncher.launch(intent)
                }
            } else {
                legacyPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            try {
                val wallpaperManager = WallpaperManager.getInstance(context)
                val wallpaperDrawable = wallpaperManager.peekDrawable()
                wallpaperBitmap = wallpaperDrawable?.let {
                    val bitmap = Bitmap.createBitmap(
                        it.intrinsicWidth.coerceAtLeast(1),
                        it.intrinsicHeight.coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    it.setBounds(0, 0, canvas.width, canvas.height)
                    it.draw(canvas)
                    bitmap.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    TerminalScreen(viewModel, wallpaperBitmap, hasPermission)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(viewModel: TerminalViewModel, wallpaperBitmap: ImageBitmap?, hasPermission: Boolean) {
    val output by viewModel.output.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var inputValue by remember { mutableStateOf("") }
    val context = LocalContext.current

    var showSettings by remember { mutableStateOf(false) }
    var showAppsList by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val launcherColors = getLauncherColors(colorScheme, settings.overlayAlpha)

    LaunchedEffect(key1 = viewModel) {
        viewModel.events.onEach {
            when(it) {
                is TerminalViewModel.Event.LaunchIntent -> {
                    context.startActivity(it.intent)
                }
            }
        }.launchIn(this)
    }

    val onCommand: (String) -> Unit = {
        viewModel.processInput(it)
        inputValue = ""
    }

    if (showSettings) {
        SettingsBottomSheet(
            currentSettings = settings,
            onDismiss = { showSettings = false },
            onSave = { newSettings ->
                viewModel.saveSettings(newSettings)
                showSettings = false
            }
        )
    }

    if (showAppsList) {
        AppsListBottomSheet(
            onDismiss = { showAppsList = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            wallpaperBitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = settings.blurRadius.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (wallpaperBitmap != null && hasPermission) {
                launcherColors.overlayColor
            } else {
                colorScheme.background
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(32.dp))

                    if (settings.showTerminalLabel) {
                        Text(
                            "Terminal",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(output) {
                            val textColor = if (it.startsWith("${settings.username}@")) {
                                launcherColors.promptColor
                            } else {
                                launcherColors.outputColor
                            }

                            if (it.startsWith("- ")) {
                                val parts = it.removePrefix("- ").split(" (")
                                val appName = parts[0]
                                val packageName = parts.getOrNull(1)?.removeSuffix(")")

                                Text(
                                    text = appName,
                                    fontFamily = FontFamily.Monospace,
                                    color = textColor,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .clickable {
                                            packageName?.let { pkg ->
                                                val launchIntent = context.packageManager
                                                    .getLaunchIntentForPackage(pkg)
                                                launchIntent?.let { intent ->
                                                    context.startActivity(intent)
                                                }
                                            }
                                        }
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                            } else {
                                Text(
                                    text = it,
                                    fontFamily = FontFamily.Monospace,
                                    color = textColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val promptText = buildString {
                            if (settings.showDate) {
                                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                append("[${dateFormat.format(Date())}] ")
                            }
                            if (settings.showUsername) {
                                append(settings.username)
                            }
                            if (settings.showHostname && settings.showUsername) {
                                append("@${settings.hostname}")
                            } else if (settings.showHostname) {
                                append(settings.hostname)
                            }
                            if (settings.showArrow) {
                                append(" ${settings.promptArrow} ")
                            } else {
                                append(" ")
                            }
                        }

                        Text(
                            text = promptText,
                            fontFamily = FontFamily.Monospace,
                            color = launcherColors.promptColor,
                            fontSize = 14.sp
                        )
                        BasicTextField(
                            value = inputValue,
                            onValueChange = { inputValue = it },
                            textStyle = TextStyle(
                                color = colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (inputValue.isNotBlank()) {
                                    onCommand(inputValue.trim())
                                }
                            }),
                            singleLine = true,
                            cursorBrush = SolidColor(colorScheme.onSurface),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(28.dp))
                            .background(launcherColors.navBarColor)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { /* Search action */ },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { showAppsList = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Apps",
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Settings",
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
