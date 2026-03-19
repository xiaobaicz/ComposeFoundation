package io.github.xiaobaicz.compose.foundation.tv

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
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
    state: FocusState = rememberFocusState(),
    content: @Composable FocusState.() -> Unit,
) {
    val state = state as FocusStateImpl
    Box(modifier = modifier.onFocusChanged { state.hasFocus = it.hasFocus }) {
        state.content()
    }
}

sealed interface FocusGroupState : FocusState {
    val groupFocus: FocusRequester
    val fallbackFocus: FocusRequester
    fun Modifier.defaultFocus(): Modifier
    fun requestFocus(focusDirection: FocusDirection = FocusDirection.Enter): Boolean
}

private class FocusGroupStateImpl(
    override val groupFocus: FocusRequester,
    override val fallbackFocus: FocusRequester,
) : FocusGroupState {
    override var hasFocus by mutableStateOf(false)

    override fun Modifier.defaultFocus(): Modifier {
        return this.focusRequester(fallbackFocus)
    }

    override fun requestFocus(focusDirection: FocusDirection): Boolean {
        if (hasFocus) return true
        return groupFocus.requestFocus(focusDirection)
    }
}

@Composable
fun rememberFocusGroupState(): FocusGroupState {
    val (group, fallback) = remember { FocusRequester.createRefs() }
    return rememberFocusGroupState(group, fallback)
}

@Composable
fun rememberFocusGroupState(
    groupFocus: FocusRequester,
    fallbackFocus: FocusRequester,
): FocusGroupState {
    return remember { FocusGroupStateImpl(groupFocus, fallbackFocus) }
}

@Composable
fun FocusGroupState(
    modifier: Modifier = Modifier,
    state: FocusGroupState = rememberFocusGroupState(),
    content: @Composable FocusGroupState.() -> Unit,
) {
    val state = state as FocusGroupStateImpl
    Box(
        modifier = modifier
            .onFocusChanged { state.hasFocus = it.hasFocus }
            .focusRequester(state.groupFocus)
            .focusRestorer(state.fallbackFocus)
            .focusGroup()
    ) {
        state.content()
    }
}