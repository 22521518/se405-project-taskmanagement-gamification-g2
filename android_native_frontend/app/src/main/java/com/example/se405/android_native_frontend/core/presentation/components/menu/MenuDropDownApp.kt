package com.example.se405.android_native_frontend.core.presentation.components.menu

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable

@Composable
fun<T> MenuDropDownApp(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<MenuSelectionItem<T>>,
    onDismiss: () -> Unit,
    onItemClick: (MenuSelectionItem<T>) -> Unit,
    itemLabel: @Composable (MenuSelectionItem<T>) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            onExpandedChange(false)
            onDismiss()
        }
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { itemLabel(item) },
                onClick = { onItemClick(item) }
            )
        }
    }
}