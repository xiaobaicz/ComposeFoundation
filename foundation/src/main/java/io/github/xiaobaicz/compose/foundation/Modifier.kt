package io.github.xiaobaicz.compose.foundation

import androidx.compose.ui.Modifier

inline fun Modifier.conditional(
    value: Boolean,
    ifTrue: (Modifier) -> Modifier,
    ifFalse: (Modifier) -> Modifier,
): Modifier {
    return this then if (value) ifTrue(Modifier) else ifFalse(Modifier)
}

inline fun Modifier.thenIf(
    value: Boolean,
    ifTrue: (Modifier) -> Modifier,
): Modifier {
    return if (value) this then ifTrue(Modifier) else this
}