package io.github.xiaobaicz.compose.foundation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun Theme(
    colors: Colors = Colors.default,
    textStyles: TextStyles = TextStyles.default,
    buttonDecorators: ButtonDecorators = ButtonDecorators.default,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTextStyles provides textStyles,
        LocalButtonDecorators provides buttonDecorators,
    ) {
        TextStyleProvider(Theme.textStyles.normal) {
            ContentColorProvider(Theme.colors.content) {
                val buttonColor = Theme.colors.let {
                    ButtonColor(it.focus, it.unfocus, it.contentFocus, it.contentUnfocus)
                }
                ButtonColorProvider(buttonColor) {
                    content()
                }
            }
        }
    }
}

object Theme {
    val colors @Composable get() = LocalColors.current
    val textStyles @Composable get() = LocalTextStyles.current
    val buttonDecorators @Composable get() = LocalButtonDecorators.current
}