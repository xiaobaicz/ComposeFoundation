package io.github.xiaobaicz.compose.foundation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.xiaobaicz.compose.foundation.RoundButtonDecorator
import io.github.xiaobaicz.compose.foundation.TextButtonDecorator

@Immutable
data class ButtonDecorators(
    val textButton: TextButtonDecorator,
    val roundButton: RoundButtonDecorator,
) {
    companion object {
        val default = ButtonDecorators(
            textButton = TextButtonDecorator(),
            roundButton = RoundButtonDecorator(),
        )
    }
}

val LocalButtonDecorators = staticCompositionLocalOf { ButtonDecorators.default }