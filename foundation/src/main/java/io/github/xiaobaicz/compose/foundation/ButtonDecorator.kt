package io.github.xiaobaicz.compose.foundation

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import kotlin.math.min

fun interface ButtonDecorator {
    @Composable
    fun Decoration(state: ButtonState, content: @Composable () -> Unit)
}

val LocalButtonDecorator = staticCompositionLocalOf<ButtonDecorator> { RoundButtonDecorator() }

@Composable
fun ButtonDecoratorProvider(decorator: ButtonDecorator, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalButtonDecorator provides decorator) {
        content()
    }
}

@Immutable
class ButtonStateColor(
    val normal: Color = Color.Unspecified,
    val focused: Color = normal,
    val pressed: Color = focused,
    val selected: Color = focused,
    val disabled: Color = Color.Unspecified,
) {
    fun current(state: ButtonState): Color {
        return when (state) {
            ButtonState.Normal -> normal
            ButtonState.Focused -> focused
            ButtonState.Pressed -> pressed
            ButtonState.Selected -> selected
            ButtonState.Disabled -> disabled
        }
    }

    companion object {
        @Stable
        val Unspecified = ButtonStateColor()
    }
}

@Stable
fun TextButtonDecorator(
    all: Dp = Dp.Unspecified,
    textColor: ButtonStateColor = ButtonStateColor.Unspecified
) = TextButtonDecorator(
    start = all,
    top = all,
    end = all,
    bottom = all,
    textColor = textColor,
)

@Stable
fun TextButtonDecorator(
    horizontal: Dp = Dp.Unspecified,
    vertical: Dp = Dp.Unspecified,
    textColor: ButtonStateColor = ButtonStateColor.Unspecified
) = TextButtonDecorator(
    start = horizontal,
    top = vertical,
    end = horizontal,
    bottom = vertical,
    textColor = textColor,
)

@Immutable
class TextButtonDecorator(
    val start: Dp = Dp.Unspecified,
    val top: Dp = Dp.Unspecified,
    val end: Dp = Dp.Unspecified,
    val bottom: Dp = Dp.Unspecified,
    val textColor: ButtonStateColor = ButtonStateColor.Unspecified
) : ButtonDecorator {
    @Composable
    override fun Decoration(state: ButtonState, content: @Composable () -> Unit) {
        val start = start.takeOrElse { 16.dp }
        val top = top.takeOrElse { 8.dp }
        val end = end.takeOrElse { 16.dp }
        val bottom = bottom.takeOrElse { 8.dp }
        Box(modifier = Modifier.padding(start, top, end, bottom)) {
            val textColor = textColor.current(state)
            ContentColorProvider(textColor.takeOrElse { LocalTextColor.current }) {
                content()
            }
        }
    }
}

@Stable
fun RoundButtonDecorator(
    all: Dp = Dp.Unspecified,
    radius: Dp = Dp.Unspecified,
    textColor: ButtonStateColor = ButtonStateColor.Unspecified,
    background: ButtonStateColor = ButtonStateColor.Unspecified
) = RoundButtonDecorator(
    start = all,
    top = all,
    end = all,
    bottom = all,
    radius = radius,
    textColor = textColor,
    background = background
)

@Stable
fun RoundButtonDecorator(
    horizontal: Dp = Dp.Unspecified,
    vertical: Dp = Dp.Unspecified,
    radius: Dp = Dp.Unspecified,
    textColor: ButtonStateColor = ButtonStateColor.Unspecified,
    background: ButtonStateColor = ButtonStateColor.Unspecified
) = RoundButtonDecorator(
    start = horizontal,
    top = vertical,
    end = horizontal,
    bottom = vertical,
    radius = radius,
    textColor = textColor,
    background = background
)

@Immutable
class RoundButtonDecorator(
    val start: Dp = Dp.Unspecified,
    val top: Dp = Dp.Unspecified,
    val end: Dp = Dp.Unspecified,
    val bottom: Dp = Dp.Unspecified,
    val radius: Dp = Dp.Unspecified,
    val textColor: ButtonStateColor = ButtonStateColor.Unspecified,
    val background: ButtonStateColor = ButtonStateColor.Unspecified
) : ButtonDecorator {
    @Composable
    override fun Decoration(state: ButtonState, content: @Composable () -> Unit) {
        val start = start.takeOrElse { 16.dp }
        val top = top.takeOrElse { 8.dp }
        val end = end.takeOrElse { 16.dp }
        val bottom = bottom.takeOrElse { 8.dp }
        Box(
            modifier = Modifier
                .drawBehind {
                    val color = background.current(state)
                    if (color.isUnspecified) return@drawBehind
                    val radius = radius.takeOrElse { (min(size.width, size.height) / 2).toDp() }
                    drawRoundRect(color = color, cornerRadius = CornerRadius(radius.toPx()))
                }
                .padding(start, top, end, bottom)
        ) {
            val textColor = textColor.current(state)
            ContentColorProvider(textColor.takeOrElse { LocalTextColor.current }) {
                content()
            }
        }
    }
}