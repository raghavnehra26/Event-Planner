package com.focusforge.app.domain.model

data class HeatmapDay(
    val dateString: String,
    val count: Int,
    val intensity: Float // 0.0f to 1.0f
)
