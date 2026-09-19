package com.rt.manger.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Bitmap?,
    val primaryColor: Color?,
    val isEdited: Boolean = false
)

enum class LayerType { RASTER, VECTOR, SHAPE, LINE, TEXT, MASK, EFFECT }

data class IconLayer(
    val id: String,
    val name: String,
    val type: LayerType,
    val visible: Boolean = true,
    val opacity: Float = 1f
)

data class IconDocument(
    val packageName: String,
    val canvasSize: Int = 432,
    val layers: List<IconLayer> = emptyList()
)
