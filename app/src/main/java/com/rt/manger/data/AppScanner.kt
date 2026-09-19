package com.rt.manger.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import com.rt.manger.model.InstalledApp
import kotlin.math.max
import kotlin.math.min

class AppScanner(context: Context) {
    private val packageManager = context.packageManager

    fun scanLauncherApps(): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        return packageManager.queryIntentActivities(intent, 0)
            .asSequence()
            .map { info ->
                val appInfo = info.activityInfo.applicationInfo
                val bitmap = appInfo.loadIcon(packageManager).toBitmap()
                InstalledApp(
                    packageName = appInfo.packageName,
                    label = appInfo.loadLabel(packageManager).toString(),
                    icon = bitmap,
                    primaryColor = estimatePrimaryColor(bitmap)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun Drawable.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(max(1, intrinsicWidth), max(1, intrinsicHeight), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    private fun estimatePrimaryColor(bitmap: Bitmap): Color? {
        val stepX = max(1, bitmap.width / 24)
        val stepY = max(1, bitmap.height / 24)
        var r = 0L; var g = 0L; var b = 0L; var count = 0L
        for (y in 0 until bitmap.height step stepY) {
            for (x in 0 until bitmap.width step stepX) {
                val pixel = bitmap.getPixel(x, y)
                if (android.graphics.Color.alpha(pixel) < 48) continue
                r += android.graphics.Color.red(pixel)
                g += android.graphics.Color.green(pixel)
                b += android.graphics.Color.blue(pixel)
                count++
            }
        }
        if (count == 0L) return null
        return Color(
            red = min(255, (r / count).toInt()),
            green = min(255, (g / count).toInt()),
            blue = min(255, (b / count).toInt())
        )
    }
}
