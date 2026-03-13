package io.github.xiaobaicz.compose.foundation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class Colors(
    val theme: Color,
    val surface: Color,
    val content: Color,
    val focus: Color,
    val unfocus: Color,
    val contentFocus: Color,
    val contentUnfocus: Color,
) {
    companion object {
        val default = Colors(
            theme = Color.Yellow,
            surface = Color.Black,
            content = Color.White,
            focus = Color.Yellow,
            unfocus = Color.Gray,
            contentFocus = Color.Black,
            contentUnfocus = Color.White,
        )
    }
}

val LocalColors = staticCompositionLocalOf { Colors.default }