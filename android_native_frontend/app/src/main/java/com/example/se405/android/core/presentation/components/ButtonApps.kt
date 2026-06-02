package com.example.se405.android.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.Blue40
import com.example.se405.android.core.presentation.theme.White

enum class ButtonType {  FILLED, OUTLINED, ROUNDED, TEXT }
@Composable
fun ButtonApp(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(vertical = 0.dp, horizontal = 16.dp),
    border: BorderStroke = BorderStroke(2.dp,MaterialTheme.colorScheme.onPrimary),
    type: ButtonType = ButtonType.FILLED,
    content: @Composable () -> Unit
) {
    when (type) {
        ButtonType.FILLED -> ButtonCTA(onClick = onClick, enabled = enabled, contentPadding = contentPadding, modifier = modifier, content = content)
        ButtonType.OUTLINED -> ButtonCTAOutline(onClick = onClick, enabled = enabled,  contentPadding = contentPadding, modifier = modifier, border = border, content = content)
        ButtonType.ROUNDED -> ButtonCTA(onClick = onClick, enabled = enabled,  contentPadding = contentPadding, modifier = modifier, shape = RoundedCornerShape(999.dp), content =  content)
        ButtonType.TEXT -> ButtonCTAText(onClick = onClick, enabled = enabled,  contentPadding = contentPadding, modifier = modifier, content = content)
    }
}

@Composable
fun ButtonCTA(
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    content: @Composable () -> Unit = {},
) {
    Button(onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
//            containerColor = MaterialTheme.colorScheme.onPrimary,
//            contentColor = MaterialTheme.colorScheme.primary
            containerColor = Blue40,
            contentColor = White
        ),
        contentPadding = contentPadding,
        modifier = Modifier.then(modifier)
        ){
        content()
    }
}

@Composable
fun ButtonCTAOutline(
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    border: BorderStroke,
    content: @Composable () -> Unit = {},
) {
    OutlinedButton(onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = border,
        contentPadding = contentPadding,
        modifier = Modifier.then(modifier)
    ){
        content()
    }
}

@Composable
fun ButtonCTAText(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit = {},
) {
    TextButton(onClick = onClick,
        enabled = enabled,
        modifier = Modifier.padding(0.dp).then(modifier),
        contentPadding = contentPadding,
    ){
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun ButtonCTAPreview() {
    Android_Theme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ButtonType.entries.forEach { type ->  ButtonApp(onClick = {}, type = type) {
                val textColor = when (type) {
                    ButtonType.FILLED -> MaterialTheme.colorScheme.primary
                    ButtonType.OUTLINED -> MaterialTheme.colorScheme.onPrimary
                    ButtonType.ROUNDED -> MaterialTheme.colorScheme.primary
                    ButtonType.TEXT -> MaterialTheme.colorScheme.onPrimary
                }

                Text("Button $type", color = textColor)
            } }
        }
    }
}
