@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android_native_frontend.core.presentation.components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.se405.android_native_frontend.R
import com.example.se405.android_native_frontend.core.presentation.theme.Android_native_frontendTheme
import com.example.se405.android_native_frontend.core.presentation.theme.AppText
import com.example.se405.android_native_frontend.features.tasks_management.__test_data__.preview.PreviewDomainEntityData
import com.example.se405.android_native_frontend.features.tasks_management.domain.entity.Tag
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun <T> MenuSelectionPopUp(
    items: List<MenuSelectionItem<T>>,
    onDismiss: () -> Unit,
    onItemClick: (MenuSelectionItem<T>) -> Unit,
    itemLabel: @Composable (MenuSelectionItem<T>) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 300.dp, max = 360.dp)
                .heightIn(max = 360.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Select items",
                style = AppText.BodySemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (item.selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            else Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                        )
                        .clickable { onItemClick(item) }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemLabel(item)

                }
            }
        }
    }
}

fun Tag.toMenuDropDownItem() = MenuSelectionItem(data = this, selected = false)

@Preview(showBackground = true)
@Composable
fun MenuSelectionPopUpPreview() {
    Android_native_frontendTheme {
        val itemsState = remember {
            mutableStateOf(
                PreviewDomainEntityData.tags.map { it.toMenuDropDownItem() }
            )
        }

        MenuSelectionPopUp(
            items = itemsState.value,
            onDismiss = {},
            onItemClick = { clickedItem ->
                itemsState.value = itemsState.value.map {
                    if (it.id == clickedItem.id) {
                        it.copy(selected = !it.selected)
                    } else it
                }
            }
        ) { menuItem ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(menuItem.data.label.icon),
                    contentDescription = menuItem.data.name,
                    tint = Color(menuItem.data.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(menuItem.data.label.name, color = MaterialTheme.colorScheme.onPrimary)
                if (menuItem.selected)
                    Icon(
                        painter = painterResource(R.drawable.icon_done),
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        contentDescription = "Selected",
                        modifier = Modifier.padding(start = 8.dp).size(16.dp)
                            .background(MaterialTheme.colorScheme.onPrimary)
                    )
            }
        }
    }
}