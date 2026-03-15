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
        CompositionLocalProvider(
            LocalTextStyle provides Theme.textStyles.normal,
            LocalContentColor provides Theme.colors.content,
            LocalButtonDecorator provides Theme.buttonDecorators.roundButton
        ) {
            content()
        }
    }
}

object Theme {
    val colors @Composable get() = LocalColors.current
    val textStyles @Composable get() = LocalTextStyles.current
    val buttonDecorators @Composable get() = LocalButtonDecorators.current
}