package io.github.xiaobaicz.compose.foundation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ButtonDecorators(
    val textButton: TextButtonDecorator,
    val roundButton: RoundButtonDecorator,
) {
    companion object {
        val default = ButtonDecorators(
            textButton = TextButtonDecorator(
                buttonColor = ButtonColor(
                    contentFocus = Color.Yellow,
                    contentUnfocus = Color.White
                )
            ),
            roundButton = RoundButtonDecorator(
                buttonColor = ButtonColor(
                    focus = Color.Yellow,
                    unfocus = Color.Gray,
                    contentFocus = Color.Black,
                    contentUnfocus = Color.White,
                )
            ),
        )
    }
}

val LocalButtonDecorators = staticCompositionLocalOf { ButtonDecorators.default }