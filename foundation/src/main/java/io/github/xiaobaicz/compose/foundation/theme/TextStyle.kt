package io.github.xiaobaicz.compose.foundation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle

val LocalTextStyle = compositionLocalOf { TextStyle.Default }

@Composable
fun TextStyleProvider(style: TextStyle = TextStyle.Default, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTextStyle provides style) {
        content()
    }
}