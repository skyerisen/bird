package com.example.cliauncher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cliauncher.ui.theme.CLIauncherTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {
    private val viewModel: TerminalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CLIauncherTheme {
                TerminalScreen(viewModel)
            }
        }
    }
}

@Composable
fun TerminalScreen(viewModel: TerminalViewModel) {
    val output by viewModel.output.collectAsState()
    var inputValue by remember { mutableStateOf("") }
    val context = LocalContext.current

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Terminal", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(output) {
                        val textColor = if (it.startsWith("user@local")) Color.White else Color(0xFF82B1FF)
                        if (it.startsWith("- ")) { // App suggestion
                            val parts = it.removePrefix("- ").split(" (")
                            val appName = parts[0]
                            val packageName = parts[1].removeSuffix(")")
                            Text(
                                text = appName,
                                fontFamily = FontFamily.Monospace,
                                color = textColor,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable { 
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                                    context.startActivity(launchIntent)
                                 }.fillMaxWidth()
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
                    Text(
                        text = "user@local >>> ",
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    BasicTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        textStyle = TextStyle(
                            color = Color.White,
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
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(painter = painterResource(id = android.R.drawable.ic_input_get), contentDescription = "Keyboard")
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size), contentDescription = "List")
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}