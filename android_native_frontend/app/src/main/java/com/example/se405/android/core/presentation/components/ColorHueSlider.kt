package com.example.se405.android.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.theme.Android_Theme

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ColorHueSlider(
//    onColorSelected: (Color) -> Unit,
//    initialColor: Color = Color.Blue,
//    actionSection: @Composable () -> Unit = {},
//    children: @Composable () -> Unit = {},
//) {
//    val initialHue = remember(initialColor) {
//        val hsv = FloatArray(3)
//        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
//        hsv[0] / 360f
//    }
//
//    var sliderVal by remember { mutableFloatStateOf(initialHue) }
//
//    val hue by remember { derivedStateOf { sliderVal * 360f } }
//    val currentColor by remember { derivedStateOf { Color.hsv(hue, 1f, 1f) } }
//    val hex by remember { derivedStateOf { String.format("#%08X", currentColor.toArgb()) } }
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.SpaceBetween
//    ) {
//        children()
//        Text(text = "Color: $hex")
//        Slider(
//            value = sliderVal,
//            onValueChange = { value ->
//                sliderVal = value
//                onColorSelected(Color.hsv(value * 360f, 1f, 1f))
//            },
//            valueRange = 0f..1f,
//            colors = SliderDefaults.colors(
//                thumbColor = currentColor,
//                activeTrackColor = Color.Transparent,
//                inactiveTrackColor = Color.Transparent
//            ),
//            track = {
//                Box(
//                    Modifier
//                        .fillMaxWidth()
//                        .height(8.dp)
//                        .background(
//                            brush = Brush.horizontalGradient(
//                                listOf(
//                                    Color.Red,
//                                    Color.Yellow,
//                                    Color.Green,
//                                    Color.Cyan,
//                                    Color.Blue,
//                                    Color.Magenta,
//                                    Color.Red
//                                )
//                            ),
//                            shape = RoundedCornerShape(4.dp)
//                        )
//                )
//            }
//        )
//        actionSection()
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorHueSlider(
    onColorSelected: (Color) -> Unit,
    initialColor: Color = Color.Blue,
    actionSection: @Composable () -> Unit = {},
    children: @Composable () -> Unit = {},
) {
    // Chỉ lấy giá trị khởi tạo một lần duy nhất, không reset khi initialColor từ ngoài thay đổi
    val initialHue = remember {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
        hsv[0] / 360f
    }

    var sliderVal by remember { mutableFloatStateOf(initialHue) }

    val hue by remember { derivedStateOf { sliderVal * 360f } }
    val currentColor by remember { derivedStateOf { Color.hsv(hue, 1f, 1f) } }
    val hex by remember { derivedStateOf { String.format("#%08X", currentColor.toArgb()) } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        children()
//        Text(text = "Color: $hex")
        Slider(
            value = sliderVal,
            onValueChange = { value ->
                sliderVal = value
                onColorSelected(Color.hsv(value * 360f, 1f, 1f))
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = currentColor,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            track = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Red, Color.Yellow, Color.Green,
                                    Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                )
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        )
        actionSection()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECF7FB)
@Composable
fun ColorHueSliderPreview() {
    Android_Theme {
        Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            var tintColor by remember { mutableStateOf(Color.Blue) }
            ColorHueSlider(onColorSelected = { tintColor = it}, initialColor = tintColor) {
                BuiltinLabelIcon(label = BuiltinLabels[0], tint = tintColor, modifier = Modifier.size(32.dp))
            }
        }
    }
}