package com.example.cliauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.cliauncher.ui.theme.CLIauncherTheme

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
