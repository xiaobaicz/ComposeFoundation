package io.github.xiaobaicz.compose.foundation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class Colors(
    val theme: Color,
    val surface: Color,
    val content: Color,
) {
    companion object {
        val default = Colors(
            theme = Color.Yellow,
            surface = Color.Black,
            content = Color.White,
        )
    }
}

val LocalColors = staticCompositionLocalOf { Colors.default }