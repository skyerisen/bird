package com.example.cliauncher

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.graphics.drawable.toBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.cliauncher.ui.theme.CLIauncherTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {
    private val viewModel: TerminalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CLIauncherTheme {
                TerminalScreenWithPermission(viewModel)
            }
        }
    }
}

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
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:${context.packageName}")
                    manageStorageLauncher.launch(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
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

@Composable
fun TerminalScreen(viewModel: TerminalViewModel, wallpaperBitmap: ImageBitmap?, hasPermission: Boolean) {
    val output by viewModel.output.collectAsState()
    var inputValue by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    var showSettings by remember { mutableStateOf(false) }
    var showAppsList by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)

    var username by remember { mutableStateOf(prefs.getString("username", "user") ?: "user") }
    var showDate by remember { mutableStateOf(prefs.getBoolean("show_date", true)) }
    var showUsername by remember { mutableStateOf(prefs.getBoolean("show_username", true)) }
    var showHostname by remember { mutableStateOf(prefs.getBoolean("show_hostname", true)) }
    var hostname by remember { mutableStateOf(prefs.getString("hostname", "local") ?: "local") }
    var promptArrow by remember { mutableStateOf(prefs.getString("prompt_arrow", ">>>") ?: ">>>") }
    var showArrow by remember { mutableStateOf(prefs.getBoolean("show_arrow", true)) }

    val colorScheme = MaterialTheme.colorScheme
    val promptColor = colorScheme.primary
    val outputColor = colorScheme.secondary
    val overlayColor = colorScheme.surface.copy(alpha = 0.7f)
    val navBarColor = colorScheme.surfaceVariant.copy(alpha = 0.8f)

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
        SettingsDialog(
            currentUsername = username,
            currentShowDate = showDate,
            currentShowUsername = showUsername,
            currentShowHostname = showHostname,
            currentHostname = hostname,
            currentPromptArrow = promptArrow,
            currentShowArrow = showArrow,
            onDismiss = { showSettings = false },
            onSave = { newUsername, newShowDate, newShowUsername, newShowHostname, newHostname, newPromptArrow, newShowArrow ->
                username = newUsername
                showDate = newShowDate
                showUsername = newShowUsername
                showHostname = newShowHostname
                hostname = newHostname
                promptArrow = newPromptArrow
                showArrow = newShowArrow

                prefs.edit()
                    .putString("username", newUsername)
                    .putBoolean("show_date", newShowDate)
                    .putBoolean("show_username", newShowUsername)
                    .putBoolean("show_hostname", newShowHostname)
                    .putString("hostname", newHostname)
                    .putString("prompt_arrow", newPromptArrow)
                    .putBoolean("show_arrow", newShowArrow)
                    .apply()
                showSettings = false
            },
            context = context
        )
    }

    if (showAppsList) {
        AppsListDialog(
            viewModel = viewModel,
            onDismiss = { showAppsList = false },
            context = context
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
                        .blur(radius = 12.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (wallpaperBitmap != null && hasPermission) {
                overlayColor
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
                    Text(
                        "Terminal",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(output) {
                            val textColor = if (it.startsWith("$username@local")) {
                                promptColor
                            } else {
                                outputColor
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

                        Text(
                            text = promptText,
                            fontFamily = FontFamily.Monospace,
                            color = promptColor,
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
                            .background(navBarColor)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { /* Действие поиска */ },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentUsername: String,
    currentShowDate: Boolean,
    currentShowUsername: Boolean,
    currentShowHostname: Boolean,
    currentHostname: String,
    currentPromptArrow: String,
    currentShowArrow: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Boolean, Boolean, Boolean, String, String, Boolean) -> Unit,
    context: Context
) {
    var username by remember { mutableStateOf(currentUsername) }
    var showDate by remember { mutableStateOf(currentShowDate) }
    var showUsername by remember { mutableStateOf(currentShowUsername) }
    var showHostname by remember { mutableStateOf(currentShowHostname) }
    var hostname by remember { mutableStateOf(currentHostname) }
    var promptArrow by remember { mutableStateOf(currentPromptArrow) }
    var showArrow by remember { mutableStateOf(currentShowArrow) }

    val colorScheme = MaterialTheme.colorScheme

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorScheme.onSurface
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = colorScheme.onSurface
                            )
                        }
                    }

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
                                onSave(username, showDate, showUsername, showHostname, hostname, promptArrow, showArrow)
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppsListDialog(
    viewModel: TerminalViewModel,
    onDismiss: () -> Unit,
    context: Context
) {
    val colorScheme = MaterialTheme.colorScheme
    val packageManager = context.packageManager

    // Get all apps
    val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
    val allApps = remember {
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
            .sortedBy { it.first.lowercase() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Apps",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${allApps.size} apps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(allApps) { (label, packageName, icon) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable {
                                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                                    launchIntent?.let {
                                        context.startActivity(it)
                                        onDismiss()
                                    }
                                }
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

                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurface,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}