package io.github.xiaobaicz.compose.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import io.github.xiaobaicz.compose.foundation.theme.Theme

@Composable
fun Surface(
    modifier: Modifier = Modifier,
    color: Color = Theme.colors.surface,
    shape: Shape = RectangleShape,
    maxSize: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .thenIf(maxSize) { it.fillMaxSize() }
            .background(color, shape)) {
        content()
    }
}

@Composable
fun Surface(
    brush: Brush,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    maxSize: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .thenIf(maxSize) { it.fillMaxSize() }
            .background(brush, shape)
    ) {
        content()
    }
}