package com.example.se405.android_native_frontend.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.se405.android_native_frontend.R
import com.example.se405.android_native_frontend.core.presentation.theme.AppText
import com.example.se405.android_native_frontend.features.tasks_management.presentation.components.LabelTextInput

fun verifyTextInputLen(text: String, maxLen: Int): Boolean = text.length <= maxLen

@Composable
fun TextFieldApp(
    labelTitle: String,
    value: String,
    placeholder:  @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
    maxTextLen: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = AppText.Body2Regular.copy(color = MaterialTheme.colorScheme.secondaryContainer),
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    TextField(
        value = value,
        onValueChange = { newText: String ->
            if (verifyTextInputLen(newText, maxTextLen)) {
                onValueChange(newText)
            }
        },
        placeholder = placeholder,
        modifier = Modifier.fillMaxWidth().padding(0.dp).then(modifier),
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = style,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,

            focusedIndicatorColor = MaterialTheme.colorScheme.outline,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            cursorColor = MaterialTheme.colorScheme.onPrimary,
        ),
        label = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_edit_pencil),
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
                LabelTextInput("$labelTitle (${value.length} / $maxTextLen)")
            }
        }
    )
}