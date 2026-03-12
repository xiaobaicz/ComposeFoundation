package io.github.xiaobaicz.compose.foundation

import androidx.compose.foundation.Indication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface ButtonScope {
    val isFocus: Boolean
}

private class ButtonScopeImpl : ButtonScope {
    override var isFocus by mutableStateOf(false)
}

fun interface ButtonDecorator {
    @Composable
    fun ButtonScope.Decoration(content: @Composable ButtonScope.() -> Unit)

    companion object : ButtonDecorator {
        @Composable
        override fun ButtonScope.Decoration(content: @Composable (ButtonScope.() -> Unit)) {
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
    content: @Composable ButtonScope.() -> Unit,
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

fun TextButtonDecorator(
    all: Dp = 0.dp,
    buttonColor: ButtonColor = ButtonColor.Unspecified
) = TextButtonDecorator(
    start = all,
    top = all,
    end = all,
    bottom = all,
    buttonColor = buttonColor,
)

fun TextButtonDecorator(
    horizontal: Dp = 0.dp,
    vertices: Dp = 0.dp,
    buttonColor: ButtonColor = ButtonColor.Unspecified
) = TextButtonDecorator(
    start = horizontal,
    top = vertices,
    end = horizontal,
    bottom = vertices,
    buttonColor = buttonColor,
)

@Immutable
class TextButtonDecorator(
    @Stable val start: Dp = 0.dp,
    @Stable val top: Dp = 0.dp,
    @Stable val end: Dp = 0.dp,
    @Stable val bottom: Dp = 0.dp,
    @Stable val buttonColor: ButtonColor = ButtonColor.Unspecified,
) : ButtonDecorator {
    @Composable
    override fun ButtonScope.Decoration(content: @Composable (ButtonScope.() -> Unit)) {
        val buttonColor = buttonColor.takeOrElse { LocalButtonColor.current }
        Box(modifier = Modifier.padding(start, top, end, bottom)) {
            val contentColor = if (isFocus) buttonColor.contentFocus else buttonColor.contentUnFocus
            ContentColorProvider(contentColor) {
                content()
            }
        }
    }
}

fun RoundButtonDecorator(
    all: Dp = 0.dp,
    radius: Dp = 0.dp,
    buttonColor: ButtonColor = ButtonColor.Unspecified
) = RoundButtonDecorator(
    start = all,
    top = all,
    end = all,
    bottom = all,
    radius = radius,
    buttonColor = buttonColor,
)

fun RoundButtonDecorator(
    horizontal: Dp = 0.dp, vertices: Dp = 0.dp,
    radius: Dp = 0.dp,
    buttonColor: ButtonColor = ButtonColor.Unspecified
) = RoundButtonDecorator(
    start = horizontal,
    top = vertices,
    end = horizontal,
    bottom = vertices,
    radius = radius,
    buttonColor = buttonColor,
)

@Immutable
class RoundButtonDecorator(
    @Stable val start: Dp = 0.dp,
    @Stable val top: Dp = 0.dp,
    @Stable val end: Dp = 0.dp,
    @Stable val bottom: Dp = 0.dp,
    @Stable val radius: Dp = 0.dp,
    @Stable val buttonColor: ButtonColor = ButtonColor.Unspecified,
) : ButtonDecorator {
    @Composable
    override fun ButtonScope.Decoration(content: @Composable (ButtonScope.() -> Unit)) {
        val buttonColor = buttonColor.takeOrElse { LocalButtonColor.current }
        Box(
            modifier = Modifier
                .drawBehind {
                    val color = if (isFocus) buttonColor.focus else buttonColor.unFocus
                    drawRoundRect(color = color, cornerRadius = CornerRadius(radius.toPx()))
                }
                .padding(start, top, end, bottom)
        ) {
            val contentColor = if (isFocus) buttonColor.contentFocus else buttonColor.contentUnFocus
            ContentColorProvider(contentColor) {
                content()
            }
        }
    }
}