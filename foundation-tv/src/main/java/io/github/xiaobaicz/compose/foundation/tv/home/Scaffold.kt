package io.github.xiaobaicz.compose.foundation.tv.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import io.github.xiaobaicz.compose.foundation.tv.ScrollColumn
import io.github.xiaobaicz.compose.foundation.tv.rememberScrollState

object Scaffold {
    val SIDEBAR_WIDTH = 270.dp
    val CONTENT_PADDING = 35.dp
}

@Composable
fun Scaffold(
    routers: List<Router>,
    modifier: Modifier = Modifier,
    padding: Map<Int, Dp> = emptyMap(),
    controller: Controller = rememberController(),
) {
    if (routers.isEmpty()) return
    val keyset = routers.map { it.key }.toSet()
    require(keyset.size == routers.size) { "Router key is not unique." }

    val padding by rememberUpdatedState(padding)

    val progress by remember { derivedStateOf { if (controller.showSidebar) 0f else 1f } }
    val progressAnim by animateFloatAsState(progress)
    Layout(
        modifier = modifier.graphicsLayer {
            val offsetDp = padding.getOrDefault(controller.select, Scaffold.CONTENT_PADDING)
                .coerceIn(0.dp, Scaffold.SIDEBAR_WIDTH)
            translationX = progressAnim * -(Scaffold.SIDEBAR_WIDTH - offsetDp).toPx()
        },
        content = {
            val (sidebarFocus, contentFocus) = remember { FocusRequester.createRefs() }
            var sidebarHasFocus by rememberSaveable { mutableStateOf(false) }
            var contentHasFocus by rememberSaveable { mutableStateOf(false) }

            val defaultSelect = remember(controller) { controller.select }
            val state = rememberScrollState(defaultSelect)

            val selectFlow = remember(controller) { snapshotFlow { controller.select } }
            LaunchedEffect(selectFlow) {
                selectFlow.collect { state.select(it) }
            }

            ScrollColumn(
                state = state,
                modifier = Modifier
                    .width(Scaffold.SIDEBAR_WIDTH)
                    .fillMaxHeight()
                    .background(Color.Gray)
                    .onFocusChanged {
                        sidebarHasFocus = it.hasFocus
                        if (it.hasFocus) controller.showSidebar()
                    }
                    .focusRequester(sidebarFocus)
            ) {
                for (router in routers) {
                    with(router) {
                        key(key) {
                            Title(controller)
                        }
                    }
                }
            }

            val holder = rememberSaveableStateHolder()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .onFocusChanged {
                        contentHasFocus = it.hasFocus
                        if (it.hasFocus) controller.hideSidebar()
                    }
                    .focusRequester(contentFocus)
                    .focusRestorer()
                    .focusGroup()
            ) {
                val router = routers[controller.select]
                holder.SaveableStateProvider("content@${router.key}") {
                    router.Content(controller)
                }
            }

            LaunchedEffect(controller.showSidebar) {
                if (controller.showSidebar && !sidebarHasFocus) {
                    sidebarFocus.requestFocus()
                }
                if (!controller.showSidebar && !contentHasFocus) {
                    contentFocus.requestFocus()
                }
            }
        },
        measurePolicy = ScaffoldMeasurePolicy
    )
}