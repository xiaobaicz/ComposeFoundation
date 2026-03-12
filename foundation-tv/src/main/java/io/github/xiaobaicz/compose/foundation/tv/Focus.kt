package io.github.xiaobaicz.compose.foundation.tv

import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusDirection.Companion.Enter
import androidx.compose.ui.focus.FocusRequester

fun FocusRequester.safeRequestFocus(focusDirection: FocusDirection = Enter): Boolean {
    if (saveFocusedChild()) return true
    return try {
        requestFocus(focusDirection)
    } catch (_: Throwable) {
        false
    }
}