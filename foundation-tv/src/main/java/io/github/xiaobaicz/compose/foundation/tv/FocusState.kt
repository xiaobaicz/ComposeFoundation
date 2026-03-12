package io.github.xiaobaicz.compose.foundation.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

sealed interface FocusState {
    val hasFocus: Boolean
}

private class FocusStateImpl : FocusState {
    override var hasFocus by mutableStateOf(false)
}

@Composable
fun rememberFocusState(): FocusState {
    return remember { FocusStateImpl() }
}

@Composable
fun FocusState(
    modifier: Modifier = Modifier,
    state: FocusState? = null,
    content: @Composable FocusState.() -> Unit,
) {
    val state = state ?: rememberFocusState()
    Box(
        modifier = modifier.onFocusChanged { (state as FocusStateImpl).hasFocus = it.hasFocus }
    ) {
        state.content()
    }
}