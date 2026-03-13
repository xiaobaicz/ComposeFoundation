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
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import io.github.xiaobaicz.compose.foundation.theme.ButtonColor
import io.github.xiaobaicz.compose.foundation.theme.ContentColorProvider
import io.github.xiaobaicz.compose.foundation.theme.LocalButtonColor
import io.github.xiaobaicz.compose.foundation.theme.LocalContentColor
import io.github.xiaobaicz.compose.foundation.theme.takeOrElse
import kotlin.math.min

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

@Stable
fun TextButtonDecorator(
    all: Dp = Dp.Unspecified,
    buttonColor: ButtonColor = ButtonColor.Unspecified
) = TextButtonDecorator(
    start = all,
    top = all,
    end = all,
    bottom = all,
    buttonColor = buttonColor,
)

@Stable
fun TextButtonDecorator(
    horizontal: Dp = Dp.Unspecified,
    vertices: Dp = Dp.Unspecified,
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
    @Stable val start: Dp = Dp.Unspecified,
    @Stable val top: Dp = Dp.Unspecified,
    @Stable val end: Dp = Dp.Unspecified,
    @Stable val bottom: Dp = Dp.Unspecified,
    @Stable val buttonColor: ButtonColor = ButtonColor.Unspecified,
) : ButtonDecorator {
    @Composable
    override fun ButtonScope.Decoration(content: @Composable (ButtonScope.() -> Unit)) {
        val buttonColor = buttonColor.takeOrElse { LocalButtonColor.current }
        val start = start.takeOrElse { 16.dp }
        val top = top.takeOrElse { 8.dp }
        val end = end.takeOrElse { 16.dp }
        val bottom = bottom.takeOrElse { 8.dp }
        Box(modifier = Modifier.padding(start, top, end, bottom)) {
            val contentColor = if (isFocus) buttonColor.contentFocus else buttonColor.contentUnfocus
            ContentColorProvider(contentColor.takeOrElse { LocalContentColor.current }) {
                content()
            }
        }
    }
}

@Stable
fun RoundButtonDecorator(
    all: Dp = Dp.Unspecified,
    radius: Dp = Dp.Unspecified,
    buttonColor: ButtonColor = ButtonColor.Unspecified
) = RoundButtonDecorator(
    start = all,
    top = all,
    end = all,
    bottom = all,
    radius = radius,
    buttonColor = buttonColor,
)

@Stable
fun RoundButtonDecorator(
    horizontal: Dp = Dp.Unspecified,
    vertices: Dp = Dp.Unspecified,
    radius: Dp = Dp.Unspecified,
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
    @Stable val start: Dp = Dp.Unspecified,
    @Stable val top: Dp = Dp.Unspecified,
    @Stable val end: Dp = Dp.Unspecified,
    @Stable val bottom: Dp = Dp.Unspecified,
    @Stable val radius: Dp = Dp.Unspecified,
    @Stable val buttonColor: ButtonColor = ButtonColor.Unspecified,
) : ButtonDecorator {
    @Composable
    override fun ButtonScope.Decoration(content: @Composable (ButtonScope.() -> Unit)) {
        val buttonColor = buttonColor.takeOrElse { LocalButtonColor.current }
        val start = start.takeOrElse { 16.dp }
        val top = top.takeOrElse { 8.dp }
        val end = end.takeOrElse { 16.dp }
        val bottom = bottom.takeOrElse { 8.dp }
        Box(
            modifier = Modifier
                .drawBehind {
                    val color = if (isFocus) buttonColor.focus else buttonColor.unfocus
                    val radius = radius.takeOrElse { (min(size.width, size.height) / 2).toDp() }
                    drawRoundRect(color = color, cornerRadius = CornerRadius(radius.toPx()))
                }
                .padding(start, top, end, bottom)
        ) {
            val contentColor = if (isFocus) buttonColor.contentFocus else buttonColor.contentUnfocus
            ContentColorProvider(contentColor.takeOrElse { LocalContentColor.current }) {
                content()
            }
        }
    }
}