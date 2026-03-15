package io.github.xiaobaicz.compose.foundation.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import io.github.xiaobaicz.compose.foundation.ButtonState
import kotlin.math.min

fun interface ButtonDecorator {
    @Composable
    fun ButtonState.Decoration(content: @Composable ButtonState.() -> Unit)
}

val LocalButtonDecorator = staticCompositionLocalOf<ButtonDecorator> { RoundButtonDecorator() }

@Composable
fun ButtonDecoratorProvider(decorator: ButtonDecorator, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalButtonDecorator provides decorator) {
        content()
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
    override fun ButtonState.Decoration(content: @Composable (ButtonState.() -> Unit)) {
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
    override fun ButtonState.Decoration(content: @Composable (ButtonState.() -> Unit)) {
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