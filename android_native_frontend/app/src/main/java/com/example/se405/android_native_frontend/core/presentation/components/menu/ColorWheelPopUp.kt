package com.example.se405.android_native_frontend.core.presentation.components.menu

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.*
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme

@Composable
fun ColorPickerPopup(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }

    val selectedColor = Color.hsv(hue, saturation, value)

    Dialog(
        onDismissRequest = onDismiss,
        content = {
            Column {
                ColorWheel(
                    hue = hue,
                    saturation = saturation,
                    onHueSatChange = { h, s ->
                        hue = h
                        saturation = s
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Brightness slider (Value)
                Text("Brightness")
                Slider(
                    value = value,
                    onValueChange = { value = it }
                )

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(selectedColor)
                )
            }
        }
    )
}

@Composable
fun ColorWheel(
    hue: Float,
    saturation: Float,
    onHueSatChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sizeState by remember { mutableStateOf(Size.Zero) }

    val bitmap = remember(sizeState) {
        if (sizeState.width == 0f) null
        else createColorWheelBitmap(sizeState)
    }

    Canvas(
        modifier =  Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        update(offset, sizeState, onHueSatChange)
                    },
                    onDrag = { change, _ ->
                        update(change.position, sizeState, onHueSatChange)
                    }
                )
            }.then(modifier)
    ) {
        sizeState = size

        bitmap?.let {
            drawImage(it.asImageBitmap())
        }

        // Draw selector
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2f

        val angleRad = Math.toRadians(hue.toDouble())
        val r = saturation * radius

        val x = center.x + cos(angleRad) * r
        val y = center.y + sin(angleRad) * r

        drawCircle(
            color = Color.White,
            radius = 12f,
            center = Offset(x.toFloat(), y.toFloat()),
            style = Stroke(width = 3f)
        )
    }
}

private fun update(
    offset: Offset,
    size: Size,
    onHueSatChange: (Float, Float) -> Unit
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    val dx = offset.x - centerX
    val dy = offset.y - centerY

    val radius = sqrt(dx * dx + dy * dy)
    val maxRadius = size.minDimension / 2f

    val angle = atan2(dy, dx)
    var hue = Math.toDegrees(angle.toDouble()).toFloat()
    if (hue < 0) hue += 360f

    val saturation = (radius / maxRadius).coerceIn(0f, 1f)

    onHueSatChange(hue, saturation)
}

fun createColorWheelBitmap(size: Size): Bitmap {
    val width = size.width.toInt()
    val height = size.height.toInt()
    val bitmap = createBitmap(width, height)

    val centerX = width / 2f
    val centerY = height / 2f
    val maxRadius = min(centerX, centerY)

    for (y in 0 until height) {
        for (x in 0 until width) {

            val dx = x - centerX
            val dy = y - centerY

            val radius = sqrt(dx * dx + dy * dy)

            if (radius <= maxRadius) {
                val angle = atan2(dy, dx)
                var hue = Math.toDegrees(angle.toDouble()).toFloat()
                if (hue < 0) hue += 360f

                val saturation = radius / maxRadius

                val color = Color.hsv(hue, saturation, 1f)
                bitmap[x, y] = color.toArgb()
            } else {
                bitmap[x, y] = android.graphics.Color.TRANSPARENT
            }
        }
    }

    return bitmap
}

@Preview(showBackground = true, backgroundColor = 0xFFECF7FB)
@Composable
fun ColorWheelPreview() {
    Android_native_frontendTheme {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            ColorPickerPopup({}, {})
        }
    }
}