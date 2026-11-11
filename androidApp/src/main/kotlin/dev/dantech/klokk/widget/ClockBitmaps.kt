package dev.dantech.klokk.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import dev.dantech.klokk.model.ClockData
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val FACE = 0xFF020202.toInt()
private const val INK = Color.WHITE

fun analogBitmap(hourDeg: Float, minuteDeg: Float, size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(FACE)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = INK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = size * 0.03f
    }
    val cx = size / 2f
    val cy = size / 2f
    val radius = size * 0.38f
    canvas.drawCircle(cx, cy, radius, paint)
    hand(canvas, cx, cy, hourDeg, radius * 0.55f, paint)
    hand(canvas, cx, cy, minuteDeg, radius * 0.82f, paint)
    paint.style = Paint.Style.FILL
    canvas.drawCircle(cx, cy, size * 0.03f, paint)
    return bitmap
}

fun kineticBitmap(matrix: List<List<ClockData>>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(FACE)
    val rows = matrix.size
    val cols = matrix.first().size
    val cell = min(width.toFloat() / cols, height.toFloat() / rows)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = INK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = cell * 0.08f
    }
    for (row in matrix.indices) {
        for (col in matrix[row].indices) {
            val clock = matrix[row][col]
            val cx = col * cell + cell / 2f
            val cy = row * cell + cell / 2f
            val radius = cell * 0.36f
            canvas.drawCircle(cx, cy, radius, paint)
            hand(canvas, cx, cy, clock.degreeOne, radius * 0.7f, paint)
            hand(canvas, cx, cy, clock.degreeTwo, radius * 0.9f, paint)
        }
    }
    return bitmap
}

fun textBitmap(lines: List<String>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(FACE)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = INK
        textAlign = Paint.Align.CENTER
        textSize = height / (lines.size + 2f)
    }
    val step = height / (lines.size + 1f)
    lines.forEachIndexed { index, line ->
        canvas.drawText(line, width / 2f, step * (index + 1), paint)
    }
    return bitmap
}

fun alarmCardBitmap(date: String, alarmLabel: String, alarmTime: String, hourDeg: Float, minuteDeg: Float): Bitmap {
    val width = 640
    val height = 480
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(FACE)
    val dial = analogBitmap(hourDeg, minuteDeg, 280)
    canvas.drawBitmap(dial, (width - dial.width) / 2f, 70f, null)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = INK
        textAlign = Paint.Align.CENTER
        textSize = 36f
    }
    canvas.drawText(date, width / 2f, 52f, paint)
    paint.textSize = 32f
    canvas.drawText("$alarmLabel  $alarmTime", width / 2f, height - 48f, paint)
    dial.recycle()
    return bitmap
}

private fun hand(canvas: Canvas, cx: Float, cy: Float, degrees: Float, length: Float, paint: Paint) {
    val rad = Math.toRadians(degrees.toDouble())
    canvas.drawLine(
        cx,
        cy,
        cx + (length * sin(rad)).toFloat(),
        cy - (length * cos(rad)).toFloat(),
        paint,
    )
}
