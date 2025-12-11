package com.shiftline.bird.domain.model

data class AppItem(
    val label: String,
    val packageName: String,
    val isPinned: Boolean = false
)
