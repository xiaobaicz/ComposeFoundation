package io.github.xiaobaicz.compose.foundation.tv

import android.os.Bundle
import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusRequesterModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import io.github.xiaobaicz.compose.foundation.thenIf

interface ScrollListScope {
    fun select(index: Int)

    fun Modifier.registerFocusState(): Modifier {
        return this then ScrollItemFocusStateElement
    }
}

private object ScrollItemFocusStateElement : ModifierNodeElement<ScrollItemFocusStateNode>() {
    override fun create(): ScrollItemFocusStateNode {
        return ScrollItemFocusStateNode()
    }

    override fun update(node: ScrollItemFocusStateNode) {}

    override fun hashCode(): Int = 0

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun InspectorInfo.inspectableProperties() {
        properties["name"] = "ScrollItemFocusStateNode"
    }
}

private class ScrollItemFocusStateNode : Modifier.Node(),
    FocusEventModifierNode,
    FocusRequesterModifierNode,
    ParentDataModifierNode {
    val listeners = mutableSetOf<(Boolean) -> Unit>()

    override fun onDetach() {
        listeners.clear()
    }

    override fun onFocusEvent(focusState: FocusState) {
        for (listener in listeners) {
            listener(focusState.hasFocus)
        }
    }

    override fun Density.modifyParentData(parentData: Any?): Any {
        return this@ScrollItemFocusStateNode
    }
}

@Composable
fun rememberScrollState(select: Int = 0): ScrollState {
    return rememberSaveable(select, saver = ScrollState.Saver) {
        ScrollState(select)
    }
}

class ScrollState(select: Int = 0) : ScrollListScope {
    var select by mutableIntStateOf(select)
        private set

    override fun select(index: Int) {
        this.select = index
    }

    companion object {
        private const val KEY_SELECT = "0"
        val Saver = object : Saver<ScrollState, Bundle> {
            override fun SaverScope.save(value: ScrollState): Bundle {
                val bundle = Bundle()
                bundle.putInt(KEY_SELECT, value.select)
                return bundle
            }

            override fun restore(value: Bundle): ScrollState {
                val select = value.getInt(KEY_SELECT, 0)
                return ScrollState(select)
            }
        }
    }
}

private class VerticalScrollListMeasurePolicy(
    val spacing: Dp,
    val items: MutableList<IntRect>,
    val focusRequesters: MutableList<() -> Unit>,
    val onSelect: (Int) -> Unit,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        items.clear()
        focusRequesters.clear()
        val childConst = constraints.copy(minWidth = 0, minHeight = 0)
        val placeableList = measurables.map {
            it.measure(childConst)
        }.onEachIndexed { index, placeable ->
            val node = placeable.parentData as? ScrollItemFocusStateNode
            node?.listeners?.add { onSelect(index) }
            focusRequesters.add { node?.requestFocus() }
        }

        if (placeableList.isEmpty()) return layout(constraints.minWidth, constraints.maxHeight) {}

        val width = placeableList.maxOf { it.measuredWidth }
        return layout(constraints.constrainWidth(width), constraints.maxHeight) {
            var offset = 0
            val spacing = spacing.roundToPx()
            for (placeable in placeableList) {
                items.add(IntRect(IntOffset(0, offset), placeable.intSize))
                placeable.placeRelative(0, offset)
                offset += placeable.measuredHeight + spacing
            }
        }
    }
}

private class HorizontalScrollListMeasurePolicy(
    val spacing: Dp,
    val items: MutableList<IntRect>,
    val focusRequesters: MutableList<() -> Unit>,
    val onSelect: (Int) -> Unit,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        items.clear()
        focusRequesters.clear()
        val childConst = constraints.copy(minWidth = 0, minHeight = 0)
        val placeableList = measurables.map {
            it.measure(childConst)
        }.onEachIndexed { index, placeable ->
            val node = placeable.parentData as? ScrollItemFocusStateNode
            node?.listeners?.add { onSelect(index) }
            focusRequesters.add { node?.requestFocus() }
        }

        if (placeableList.isEmpty()) return layout(constraints.maxWidth, constraints.minHeight) {}

        val height = placeableList.maxOf { it.measuredHeight }
        return layout(constraints.maxWidth, constraints.constrainHeight(height)) {
            var offset = 0
            val spacing = spacing.roundToPx()
            for (placeable in placeableList) {
                items.add(IntRect(IntOffset(offset, 0), placeable.intSize))
                placeable.placeRelative(offset, 0)
                offset += placeable.measuredWidth + spacing
            }
        }
    }
}

private val Placeable.intSize get() = IntSize(measuredWidth, measuredHeight)

@Composable
private fun ScrollList(
    modifier: Modifier,
    state: ScrollState,
    spacing: Dp,
    vertical: Boolean,
    windowOffset: Float,
    itemOffset: Float,
    content: @Composable ScrollListScope.() -> Unit,
) {
    val items = remember { mutableStateListOf<IntRect>() }
    val focusRequesters = remember { mutableStateListOf<() -> Unit>() }
    val policy = remember(vertical, spacing, items) {
        if (vertical) {
            VerticalScrollListMeasurePolicy(spacing, items, focusRequesters) { state.select(it) }
        } else {
            HorizontalScrollListMeasurePolicy(spacing, items, focusRequesters) { state.select(it) }
        }
    }
    val ltr by rememberUpdatedState(LocalLayoutDirection.current == LayoutDirection.Ltr)
    val windowOffset by rememberUpdatedState(windowOffset)
    val itemOffset by rememberUpdatedState(itemOffset)

    Layout(
        content = { state.content() },
        modifier = modifier
            .thenIf(vertical) {
                it.graphicsLayer scroll@{
                    if (items.isEmpty()) return@scroll
                    val select = state.select.coerceIn(0, items.lastIndex)
                    val item = items[select]

                    val windowOffset = size.height * windowOffset
                    val itemOffset = item.height * itemOffset
                    val keyline = windowOffset - itemOffset

                    val minOffset = (items.last().bottom.toFloat() - size.height).coerceAtLeast(0f)
                    val scrollOffset = (item.top - keyline).coerceIn(0f, minOffset)
                    translationY = -scrollOffset
                }
            }
            .thenIf(!vertical) {
                it.graphicsLayer scroll@{
                    if (items.isEmpty()) return@scroll
                    val select = state.select.coerceIn(0, items.lastIndex)
                    val item = items[select]

                    val windowOffset = size.width * windowOffset
                    val itemOffset = item.width * itemOffset
                    val keyline = windowOffset - itemOffset

                    val minOffset = (items.last().right.toFloat() - size.width).coerceAtLeast(0f)
                    val scrollOffset = (item.left - keyline).coerceIn(0f, minOffset)
                    translationX = if (ltr) -scrollOffset else scrollOffset
                }
            }
            .focusProperties {
                onEnter = enter@{
                    if (focusRequesters.isEmpty()) return@enter
                    val select = state.select.coerceIn(0, focusRequesters.lastIndex)
                    focusRequesters[select]()
                }
            }
            .focusGroup(),
        measurePolicy = policy
    )
}

@Composable
fun ScrollColumn(
    modifier: Modifier = Modifier,
    state: ScrollState = rememberScrollState(),
    spacing: Dp = 0.dp,
    windowOffset: Float = 0.5f,
    itemOffset: Float = 0.5f,
    content: @Composable ScrollListScope.() -> Unit,
) {
    ScrollList(modifier, state, spacing, true, windowOffset, itemOffset, content)
}

@Composable
fun ScrollRow(
    modifier: Modifier = Modifier,
    state: ScrollState = rememberScrollState(),
    spacing: Dp = 0.dp,
    windowOffset: Float = 0.5f,
    itemOffset: Float = 0.5f,
    content: @Composable ScrollListScope.() -> Unit,
) {
    ScrollList(modifier, state, spacing, false, windowOffset, itemOffset, content)
}