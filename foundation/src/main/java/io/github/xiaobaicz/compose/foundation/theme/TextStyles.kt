package io.github.xiaobaicz.compose.foundation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

@Immutable
data class TextStyles(
    val normal: TextStyle,
) {
    companion object {
        val default = TextStyles(
            normal = TextStyle.Default
        )
    }
}

val LocalTextStyles = staticCompositionLocalOf { TextStyles.default }