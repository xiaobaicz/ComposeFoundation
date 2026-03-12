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

interface ButtonScope {
    val isFocus: Boolean
}

private class ButtonScopeImpl : ButtonScope {
    override var isFocus by mutableStateOf(false)
}

fun interface ButtonDecorator {
    @Composable
    fun ButtonScope.Decoration(content: @Composable () -> Unit)

    companion object : ButtonDecorator {
        @Composable
        override fun ButtonScope.Decoration(content: @Composable (() -> Unit)) {
            content()
        }
    }
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    decorator: ButtonDecorator = ButtonDecorator,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    hapticFeedbackEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val buttonScope = remember { ButtonScopeImpl() }
    Box(
        modifier = modifier
            .onFocusChanged {
                buttonScope.isFocus = it.isFocused
            }
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
            buttonScope.Decoration(content)
        }
    }
}