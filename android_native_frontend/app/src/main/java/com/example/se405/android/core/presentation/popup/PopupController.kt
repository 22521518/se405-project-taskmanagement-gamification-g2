package com.example.se405.android.core.presentation.popup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

/**
 * Configuration for a single popup entry in the popup stack.
 *
 * Each [PopupConfig] represents one modal popup that can be displayed
 * in a [PopupHost]. The popup is rendered using the [content] composable
 * lambda, which receives an `onDismiss` callback to allow the popup
 * content to dismiss itself (e.g., via a close button or after completing
 * an action).
 *
 * @property id Unique identifier for this popup instance. Defaults to a
 *   random UUID. Used for stable identity during recomposition and for
 *   targeted removal via [PopupController.remove].
 * @property dismissOnBackPress Whether pressing the system back button
 *   should dismiss this popup. Defaults to `true`. Set to `false` for
 *   popups that require explicit user action (e.g., mandatory confirmations).
 * @property content The composable content to render inside the popup's
 *   [Dialog]. Receives an `onDismiss: () -> Unit` callback that removes
 *   this specific popup from the stack when invoked.
 *
 * ### Usage
 * ```kotlin
 * val config = PopupConfig(
 *     dismissOnBackPress = true
 * ) { onDismiss ->
 *     TaskDetailCard(
 *         task = task,
 *         onClose = onDismiss
 *     )
 * }
 * ```
 *
 * @see PopupController
 * @see PopupHost
 */
data class PopupConfig(
    val id: String = UUID.randomUUID().toString(),
    val dismissOnBackPress: Boolean = true,
    val content: @Composable (onDismiss: () -> Unit) -> Unit
)

/**
 * Central controller for managing a global stack of modal popups.
 *
 * [PopupController] maintains a [mutableStateListOf] of [PopupConfig]
 * entries. Because the list is backed by Compose's snapshot state system,
 * any mutation (push, remove, clear) automatically triggers recomposition
 * of the [PopupHost] that observes the [stack].
 *
 * ### Architectural notes
 * - **Root-level ownership**: Create the controller at the app root
 *   (e.g., inside `MainActivity`'s `setContent`) and provide it via
 *   [LocalPopupController]. This ensures popups survive navigation
 *   transitions because they sit above the `NavHost`.
 * - **Instance-based removal**: [remove] targets the specific
 *   [PopupConfig] instance rather than blindly popping the top, which
 *   prevents incorrect dismissal when multiple popups are stacked.
 * - **Memory safety**: When a popup is removed its composable is torn
 *   down by Compose, so any `remember`-ed state inside the popup content
 *   is automatically cleaned up.
 *
 * ### Thread safety
 * All mutations must happen on the main thread (standard Compose rule).
 *
 * @see PopupConfig
 * @see PopupHost
 * @see LocalPopupController
 */
class PopupController {

    /**
     * Internal mutable state list backing the popup stack.
     * Compose observes this list; mutations trigger recomposition.
     */
    private val _stack = mutableStateListOf<PopupConfig>()

    private var lastPushTime = 0L
    private val DEBOUNCE_TIME_MS = 300L

    /**
     * Read-only snapshot of the current popup stack.
     *
     * Iterate this list in [PopupHost] to render each popup.
     * The list order corresponds to the push order: index 0 is the
     * bottom-most popup, last index is the top-most (most recently pushed).
     */
    val stack: List<PopupConfig> get() = _stack

    /**
     * Push a new popup onto the top of the stack.
     *
     * The popup will be rendered inside a [Dialog] by [PopupHost].
     * The [content] lambda receives an `onDismiss` callback bound to
     * the specific [PopupConfig] instance, so calling it always removes
     * the correct popup regardless of stack order.
     *
     * @param dismissOnBackPress Whether the system back button should
     *   dismiss this popup. Defaults to `true`.
     * @param content Composable content to display. Receives `onDismiss`
     *   for self-dismissal.
     * @return The [PopupConfig] that was created and pushed, in case the
     *   caller needs to remove it programmatically later.
     *
     * ### Example
     * ```kotlin
     * val popup = LocalPopupController.current
     * popup.push { onDismiss ->
     *     ConfirmDialog(
     *         message = "Are you sure?",
     *         onConfirm = { /* ... */ onDismiss() },
     *         onCancel = onDismiss
     *     )
     * }
     * ```
     */
    fun push(
        dismissOnBackPress: Boolean = true,
        content: @Composable (onDismiss: () -> Unit) -> Unit
    ): PopupConfig? {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPushTime < DEBOUNCE_TIME_MS) {
            return null // Prevent double-click from pushing multiple popups
        }
        lastPushTime = currentTime

        val config = PopupConfig(
            dismissOnBackPress = dismissOnBackPress,
            content = content
        )
        _stack.add(config)
        return config
    }

    /**
     * Remove a specific popup instance from the stack.
     *
     * This is the **preferred** way to dismiss a popup because it
     * targets the exact [PopupConfig] regardless of its position in the
     * stack. If the config is not found (already removed), this is a no-op.
     *
     * @param config The [PopupConfig] instance to remove.
     */
    fun remove(config: PopupConfig) {
        _stack.remove(config)
    }

    /**
     * Pop (remove) the topmost popup from the stack.
     *
     * Convenience method equivalent to removing the last element.
     * No-op if the stack is empty.
     */
    fun pop() {
        if (_stack.isNotEmpty()) {
            _stack.removeAt(_stack.lastIndex)
        }
    }

    /**
     * Remove all popups from the stack.
     *
     * Use this when navigating away from a flow that opened multiple
     * popups, or during cleanup in lifecycle-aware scenarios.
     */
    fun clear() {
        _stack.clear()
    }
}

/**
 * [CompositionLocal] providing access to the nearest [PopupController].
 *
 * This is how any composable in the tree can push or dismiss popups
 * without prop-drilling. The controller must be provided at the app root
 * via [CompositionLocalProvider]:
 *
 * ```kotlin
 * val popupController = remember { PopupController() }
 * CompositionLocalProvider(LocalPopupController provides popupController) {
 *     // entire app tree
 * }
 * ```
 *
 * To use from any composable:
 * ```kotlin
 * val popup = LocalPopupController.current
 * popup.push { onDismiss ->
 *     MyPopupContent(onClose = onDismiss)
 * }
 * ```
 *
 * @throws IllegalStateException if accessed without a provider in the
 *   composition tree.
 *
 * @see PopupController
 */
val LocalPopupController = compositionLocalOf<PopupController> {
    error(
        "No PopupController provided. " +
        "Wrap your root composable with CompositionLocalProvider(LocalPopupController provides ...)"
    )
}
