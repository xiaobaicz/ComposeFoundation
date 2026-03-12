package io.github.xiaobaicz.compose.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.xiaobaicz.compose.foundation.ButtonColor.Companion.Unspecified

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
    val unFocus: Color = Color.Unspecified,
    val contentFocus: Color = Color.Unspecified,
    val contentUnFocus: Color = Color.Unspecified,
) {
    companion object {
        val Unspecified = ButtonColor()
    }
}

val ButtonColor.isUnspecified get() = this == Unspecified
val ButtonColor.isSpecified get() = !isUnspecified

@Composable
fun ButtonColor.takeOrElse(color: @Composable () -> ButtonColor): ButtonColor {
    return if (isSpecified) this else color()
}

val LocalButtonColor = compositionLocalOf { Unspecified }

@Composable
fun ButtonColorProvider(
    color: ButtonColor = Unspecified,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalButtonColor provides color) {
        content()
    }
}