package com.habitnudge.app.reminder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

object NotificationIconGenerator {

    /**
     * Creates a high-resolution circular badge containing the habit emoji and branded accent styling.
     */
    fun createHabitLargeIcon(
        context: Context,
        emoji: String,
        accentColorHex: String
    ): Bitmap {
        val density = try {
            context.resources?.displayMetrics?.density ?: 2f
        } catch (e: Exception) {
            2f
        }
        val size = (64 * density).toInt().coerceAtLeast(128)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val accentColor = try {
            Color.parseColor(accentColorHex)
        } catch (e: Exception) {
            0xFFFF8A3D.toInt()
        }

        val radius = size / 2f

        // Draw solid circular background with accent color
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(radius, radius, radius, bgPaint)

        // Draw subtle inner white stroke for premium badge depth
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 60
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * density
        }
        canvas.drawCircle(radius, radius, radius - strokePaint.strokeWidth / 2, strokePaint)

        // Draw Emoji in center
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size * 0.48f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
        val fontMetrics = textPaint.fontMetrics
        val yOffset = (fontMetrics.descent + fontMetrics.ascent) / 2
        canvas.drawText(emoji, radius, radius - yOffset, textPaint)

        return bitmap
    }

    /**
     * Safe wrapper that will not throw in headless JVM unit test environments.
     */
    fun createHabitLargeIconSafe(
        context: Context,
        emoji: String,
        accentColorHex: String
    ): Bitmap? {
        return try {
            createHabitLargeIcon(context, emoji, accentColorHex)
        } catch (e: Throwable) {
            null
        }
    }
}
