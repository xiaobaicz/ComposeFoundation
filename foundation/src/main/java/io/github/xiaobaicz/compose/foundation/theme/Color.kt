package io.github.xiaobaicz.compose.foundation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalContentColor = compositionLocalOf { Color.Unspecified }

@Composable
fun ContentColorProvider(color: Color = Color.Unspecified, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalContentColor provides color) {
        content()
    }
}

@Immutable
data class ButtonColor(
    val focus: Color = Color.Unspecified,
    val unfocus: Color = Color.Unspecified,
    val contentFocus: Color = Color.Unspecified,
    val contentUnfocus: Color = Color.Unspecified,
) {
    companion object {
        val Unspecified = ButtonColor()
    }
}

val ButtonColor.isUnspecified get() = this == ButtonColor.Unspecified
val ButtonColor.isSpecified get() = !isUnspecified

@Composable
fun ButtonColor.takeOrElse(color: @Composable () -> ButtonColor): ButtonColor {
    return if (isSpecified) this else color()
}

val LocalButtonColor = compositionLocalOf { ButtonColor.Unspecified }

@Composable
fun ButtonColorProvider(
    color: ButtonColor = ButtonColor.Unspecified,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalButtonColor provides color) {
        content()
    }
}