package io.github.xiaobaicz.compose.foundation

import androidx.compose.foundation.Indication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.Role
import io.github.xiaobaicz.compose.foundation.theme.ButtonDecorator
import io.github.xiaobaicz.compose.foundation.theme.LocalButtonDecorator

@Composable
fun rememberButtonState(): ButtonState {
    return remember { ButtonState() }
}

class ButtonState {
    var hasFocus by mutableStateOf(false)
        internal set
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = rememberButtonState(),
    decorator: ButtonDecorator = LocalButtonDecorator.current,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    hapticFeedbackEnabled: Boolean = true,
    content: @Composable ButtonState.() -> Unit,
) {
    Box(
        modifier = modifier
            .onFocusChanged { state.hasFocus = it.hasFocus }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = indication,
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = role,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                onClick = onClick
            )
    ) {
        with(decorator) {
            state.Decoration(content)
        }
    }
}