package io.github.xiaobaicz.compose.foundation

import androidx.compose.foundation.Indication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

enum class ButtonState {
    Normal, Focused, Pressed, Selected, Disabled
}

@Composable
private fun InteractionSource.collectButtonStateAsState(
    enabled: Boolean,
    selected: Boolean
): State<ButtonState> {
    val isFocused by collectIsFocusedAsState()
    val isPressed by collectIsPressedAsState()
    val enabled by rememberUpdatedState(enabled)
    val selected by rememberUpdatedState(selected)
    return remember {
        derivedStateOf {
            when {
                !enabled -> ButtonState.Disabled
                isPressed -> ButtonState.Pressed
                isFocused -> ButtonState.Focused
                selected -> ButtonState.Selected
                else -> ButtonState.Normal
            }
        }
    }
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    decorator: ButtonDecorator = LocalButtonDecorator.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    hapticFeedbackEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.combinedClickable(
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
        val state by interactionSource.collectButtonStateAsState(enabled, selected)
        decorator.Decoration(state, content)
    }
}