package com.example.se405.android.features.workspaces_management.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.se405.android.core.presentation.theme.Android_Theme
import com.example.se405.android.core.presentation.theme.AppText

@Composable
fun SubScreenHeader(content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 20.dp)) {
        content()
        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Preview(backgroundColor = 0xfff, showBackground = true, name = "SubScreenHeader")
@Composable
fun SubScreenHeaderPreview() {

    Android_Theme {
        Column(modifier = Modifier.fillMaxSize()) {
            SubScreenHeader {
                Text("Header Name", style = AppText.BodyBold)
                Text("Header Name", style = AppText.HeadBold)
                Text("Header Name", style = AppText.CaptionBold)
            }
            Column(modifier = Modifier.fillMaxSize()) { }
        }
    }
}
