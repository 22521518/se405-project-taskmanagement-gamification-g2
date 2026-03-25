package com.example.se405.android_native_frontend.core.presentation.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Composable host that renders all popups currently on the [PopupController] stack.
 *
 * Place this composable at the **root level** of your app (e.g., inside
 * `MainActivity`'s `setContent` block, **after** the `Scaffold`/`NavHost`).
 * Because it sits outside the navigation graph, popups will persist across
 * navigation transitions until explicitly dismissed.
 *
 * ### How it works
 * 1. Iterates [PopupController.stack] (a Compose-observed state list).
 * 2. For each [PopupConfig], wraps the content in a [Dialog].
 * 3. Passes an `onDismiss` callback bound to [PopupController.remove]
 *    for that specific config, so dismissing always removes the correct
 *    popup regardless of stack order.
 * 4. Respects [PopupConfig.dismissOnBackPress] via [DialogProperties].
 *
 * ### Rendering order
 * Popups are rendered in push order. The last pushed popup appears on top
 * visually because Compose lays out later `Dialog` calls above earlier ones.
 *
 * ### Example integration
 * ```kotlin
 * @Composable
 * fun App() {
 *     val popupController = remember { PopupController() }
 *     CompositionLocalProvider(LocalPopupController provides popupController) {
 *         Scaffold { padding ->
 *             Box(modifier = Modifier.padding(padding)) {
 *                 MainNavHost(navController)
 *             }
 *         }
 *         // Render popups above everything
 *         PopupHost(controller = popupController)
 *     }
 * }
 * ```
 *
 * @param controller The [PopupController] whose stack should be rendered.
 *
 * @see PopupController
 * @see PopupConfig
 * @see LocalPopupController
 */
@Composable
fun PopupHost(controller: PopupController) {
    controller.stack.forEach { config ->
        Dialog(
            onDismissRequest = { controller.remove(config) },
            properties = DialogProperties(
                dismissOnBackPress = config.dismissOnBackPress
            )
        ) {
            config.content { controller.remove(config) }
        }
    }
}
