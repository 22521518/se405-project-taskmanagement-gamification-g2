# Popup Stack Guide

This folder contains the app-level popup mechanism used by multiple features:

- [PopupController.kt](PopupController.kt): stack state + popup operations
- [PopupHost.kt](PopupHost.kt): renderer for popup stack

## Why this mechanism exists

Normal composables are scoped by navigation destination. This popup stack is mounted at app root, so a popup can stay visible above screens and be managed from any feature.

## Core concepts

1. `PopupController` stores a `List<PopupConfig>` as Compose state.
2. `LocalPopupController` exposes controller through `CompositionLocal`.
3. `PopupHost` observes stack and renders each popup in a `Dialog`.
4. Dismiss is instance-based (`remove(config)`) to avoid wrong popup removal when stacked.

## Integration at app root

```kotlin
@Composable
fun AppRoot() {
    val popupController = remember { PopupController() }

    CompositionLocalProvider(LocalPopupController provides popupController) {
        MainNavHost(/* ... */)
        PopupHost(controller = popupController)
    }
}
```

## Usage from a Screen

```kotlin
@Composable
fun TaskScreen() {
    val popup = LocalPopupController.current

    Button(onClick = {
        popup.push { onDismiss ->
            TaskDetailPopUp(
                task = task,
                onEditClick = { /* ask Screen to open edit popup */ },
                onClose = onDismiss,
                onDone = { /* dispatch to ViewModel */ },
                onWontDo = { /* dispatch to ViewModel */ },
                onDelete = { /* dispatch to ViewModel */ },
            )
        }
    }) {
        Text("Open detail")
    }
}
```

## MVVM responsibility split

1. `ViewModel`: owns domain state, validation, and business actions.
2. `Screen`: decides which popup to open/close and wires popup callbacks.
3. `Popup composable`: pure UI, receives state and emits intents via callbacks.

Do not put popup navigation logic inside ViewModel.

## Useful operations

- `push(...)`: show new popup
- `remove(config)`: dismiss a specific popup instance
- `pop()`: dismiss top popup
- `clear()`: dismiss all popups

## Notes

- Keep popup callbacks side-effect free in UI layer; dispatch intent to Screen/ViewModel.
- Use `dismissOnBackPress = false` only for mandatory confirmation dialogs.
