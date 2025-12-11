package com.shiftline.bird.domain.model

data class LauncherSettings(
    val username: String = "user",
    val showDate: Boolean = true,
    val showUsername: Boolean = true,
    val showHostname: Boolean = true,
    val hostname: String = "local",
    val promptArrow: String = ">>>",
    val showArrow: Boolean = true,
    val blurRadius: Float = 12f,
    val overlayAlpha: Float = 0.7f,
    val showTerminalLabel: Boolean = true
)
