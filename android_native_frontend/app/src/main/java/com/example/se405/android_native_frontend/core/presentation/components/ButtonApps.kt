package com.example.se405.android_native_frontend.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme

enum class ButtonType {  FILLED, OUTLINED, ROUNDED, TEXT }

@Preview(
    wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE,
    showBackground = true,
    backgroundColor = 0xFFccccc1
)
@Composable
fun ButtonCTAPreview() {
    Android_native_frontendTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ButtonType.entries.forEach { type ->  ButtonApp(onClick = {}, type = type) {
                Text("Button $type")
            } }
        }
    }
}

@Composable
fun ButtonApp(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.FILLED,
    content: @Composable () -> Unit
) {
    when (type) {
        ButtonType.FILLED -> ButtonCTA(onClick, modifier, content = content)
        ButtonType.OUTLINED -> ButtonCTAOutline(onClick, modifier, content = content)
        ButtonType.ROUNDED -> ButtonCTA(onClick, modifier, RoundedCornerShape(999.dp), content)
        ButtonType.TEXT -> ButtonCTAText(onClick, modifier, content)
    }
}

@Composable
fun ButtonCTA(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    content: @Composable () -> Unit = {},
) {
    Button(onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.then(modifier)
        ){
        content()
    }
}

@Composable
fun ButtonCTAOutline(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    content: @Composable () -> Unit = {},
) {
    OutlinedButton(onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.then(modifier)
    ){
        content()
    }
}

@Composable
fun ButtonCTAText(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    TextButton(onClick = onClick,
        modifier = Modifier.then(modifier)
    ){
        content()
    }
}