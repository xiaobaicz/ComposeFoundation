package io.github.xiaobaicz.compose.foundation.tv.lazy

import android.os.SystemClock
import androidx.collection.arrayMapOf
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasurePolicy
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import io.github.xiaobaicz.compose.foundation.thenIf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.max
import kotlin.math.roundToInt

interface LazyListScope {
    fun items(
        size: Int,
        itemKey: (Int) -> Any? = { null },
        itemType: (Int) -> Any? = { null },
        itemContent: @Composable LazyItemScope.(Int) -> Unit,
    )

    fun item(
        key: Any? = null,
        type: Any? = null,
        content: @Composable LazyItemScope.() -> Unit,
    ) {
        items(1, { key }, { type }, { content() })
    }
}

inline fun <T> LazyListScope.items(
    items: List<T>,
    noinline itemKey: (T) -> Any? = { null },
    noinline itemType: (T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    items(
        size = items.size,
        itemKey = { itemKey(items[it]) },
        itemType = { itemType(items[it]) },
        itemContent = { itemContent(items[it]) })
}

private class LazyListCore(
    content: LazyListScope.() -> Unit,
    state: LazyListState,
) : LazyListScope, LazyItemScope, LazyLayoutItemProvider, LazyLayoutMeasurePolicy {
    class Items(
        val startIndex: Int,
        val size: Int,
        val itemKey: (Int) -> Any?,
        val itemType: (Int) -> Any?,
        val itemContent: @Composable (LazyItemScope.(Int) -> Unit),
        val lastIndex: Int = startIndex + size - 1
    ) {
        fun subIndex(index: Int) = index - startIndex
    }

    val itemsList = arrayListOf<Items>()
    val indexKeyCache = arrayMapOf<Int, Any>()
    val keyIndexCache = arrayMapOf<Any, Int>()
    val indexTypeCache = arrayMapOf<Int, Any?>()
    val indexItemsCache = arrayMapOf<Int, Items>()

    val state = state as LazyListStateImpl

    init {
        content()
    }

    private fun findItems(index: Int): Items {
        val itemsIndex = itemsList.binarySearch {
            when {
                index < it.startIndex -> 1
                index > it.lastIndex -> -1
                else -> 0
            }
        }
        return itemsList[itemsIndex]
    }

    override fun items(
        size: Int,
        itemKey: (Int) -> Any?,
        itemType: (Int) -> Any?,
        itemContent: @Composable (LazyItemScope.(Int) -> Unit)
    ) {
        if (size < 1) return
        val items = Items(itemCount, size, itemKey, itemType, itemContent)
        itemsList.add(items)
        itemCount += size
    }

    @Composable
    override fun Item(index: Int, key: Any) {
        val items = indexItemsCache.getOrPut(index) {
            findItems(index).also {
                indexItemsCache[index] = it
            }
        }
        val subIndex = items.subIndex(index)
        CompositionLocalProvider(LocalLazyItemIndex provides subIndex) {
            val itemContent = items.itemContent
            itemContent(subIndex)
        }
    }

    override fun getContentType(index: Int): Any? = indexTypeCache.getOrPut(index) {
        val items = findItems(index)
        val subIndex = items.subIndex(index)
        val type = items.itemType(subIndex)
        type.also { indexTypeCache[index] = it }
    }

    override fun getIndex(key: Any): Int = keyIndexCache.getOrElse(key) { -1 }

    override fun getKey(index: Int): Any = indexKeyCache.getOrPut(index) {
        val items = findItems(index)
        val subIndex = items.subIndex(index)
        val key = items.itemKey(subIndex) ?: index
        key.also {
            indexKeyCache[index] = it
            keyIndexCache[it] = index
        }
    }

    override var itemCount: Int = 0

    override fun LazyLayoutMeasureScope.measure(constraints: Constraints): MeasureResult {
        state.composerStart()
        val result = when {
            itemCount < 1 -> layoutByEmpty(constraints)
            state.vertical -> layoutByColumn(constraints)
            else -> layoutByRow(constraints)
        }
        state.composerEnd()
        return result
    }

    private fun LazyLayoutMeasureScope.layoutByEmpty(constraints: Constraints): MeasureResult {
        return layout(constraints.constrainWidth(0), constraints.constrainHeight(0)) {}
    }

    private fun LazyLayoutMeasureScope.layoutByColumn(constraints: Constraints): MeasureResult {
        val selectIndex = state.select
        var firstIndex = selectIndex
        var lastIndex = selectIndex

        val paddingStart = state.contentPadding.calculateTopPadding().roundToPx()
        val paddingEnd = state.contentPadding.calculateBottomPadding().roundToPx()

        val spacing = state.spacing.roundToPx()

        val windowOffset = (state.windowOffset * constraints.maxHeight).roundToInt()

        val selectNode = createColumnItemNode(constraints, selectIndex)
        val selectItemOffset = (selectNode.axisSize * state.itemOffset).roundToInt()
        selectNode.offset = windowOffset - selectItemOffset

        var firstNode = selectNode
        var lastNode = selectNode

        while (lastIndex + 1 < itemCount) {
            val nextIndex = lastIndex + 1
            val next = createColumnItemNode(constraints, nextIndex)
            next.offset = lastNode.offset + lastNode.axisSize + spacing
            lastNode.next = next
            lastNode = next
            lastIndex = nextIndex
        }

        while (firstIndex - 1 >= 0) {
            val prevIndex = firstIndex - 1
            val prev = createColumnItemNode(constraints, prevIndex)
            prev.offset = firstNode.offset + spacing + prev.axisSize
            firstNode.prev = prev
            firstNode = prev
            firstIndex = prevIndex
        }

        val maxCrossSize = selectNode.maxCrossSize()

        return layout(
            width = constraints.constrainWidth(maxCrossSize),
            height = constraints.maxHeight,
        ) {
            firstNode.forEach { node ->
                node.placeableList.forEach {
                    it.placeRelative(0, node.offset)
                }
            }
        }
    }

    private fun LazyLayoutMeasureScope.layoutByRow(constraints: Constraints): MeasureResult {
        return layout(
            width = constraints.constrainWidth(0),
            height = constraints.constrainHeight(0)
        ) {}
    }

    private fun LazyLayoutMeasureScope.createColumnItemNode(
        constraints: Constraints,
        index: Int
    ): ItemNode {
        val placeableList = compose(index).map { it.measure(constraints) }
        val maxWidth = placeableList.maxOfOrNull { it.width } ?: 0
        val maxHeight = placeableList.maxOfOrNull { it.height } ?: 0
        return ItemNode(index, maxHeight, maxWidth, placeableList)
    }

    private fun LazyLayoutMeasureScope.createRowItemNode(
        constraints: Constraints,
        index: Int
    ): ItemNode {
        val placeableList = compose(index).map { it.measure(constraints) }
        val maxWidth = placeableList.maxOfOrNull { it.width } ?: 0
        val maxHeight = placeableList.maxOfOrNull { it.height } ?: 0
        return ItemNode(index, maxWidth, maxHeight, placeableList)
    }

    private class ItemNode(
        val index: Int,
        val axisSize: Int,
        val crossSize: Int,
        val placeableList: List<Placeable>,
        var offset: Int = 0,
        var prev: ItemNode? = null,
        var next: ItemNode? = null,
    ) {
        inline fun forEach(block: (ItemNode) -> Unit) {
            block(this)
            var p = prev
            while (p != null) {
                block(p)
                p = p.prev
            }
            var n = next
            while (n != null) {
                block(n)
                n = n.next
            }
        }

        fun maxCrossSize(): Int {
            var max = 0
            forEach {
                max = max(max, it.crossSize)
            }
            return max
        }

        fun offsetPlus(offset: Int) {
            forEach {
                it.offset += offset
            }
        }
    }
}

val LocalLazyItemIndex = compositionLocalOf { 0 }

interface LazyItemScope {
    val itemIndex @Composable get() = LocalLazyItemIndex.current

    fun Modifier.markAsFocusable(): Modifier {
        return this then LazyFocusableItemElement
    }
}

private object LazyFocusableItemElement : ModifierNodeElement<LazyFocusableItemNode>() {
    override fun create(): LazyFocusableItemNode {
        return LazyFocusableItemNode()
    }

    override fun update(node: LazyFocusableItemNode) {}

    override fun hashCode(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun InspectorInfo.inspectableProperties() {
        properties["name"] = "LazyFocusableItemElement"
    }
}

private class LazyFocusableItemNode : Modifier.Node(), ParentDataModifierNode {
    override fun Density.modifyParentData(parentData: Any?): Any {
        return true
    }
}

sealed interface LazyListState {
    val select: Int
    val selectFlow: Flow<Int>
    val composerStart: Flow<Long>
    val composerEnd: Flow<Long>
}

private class LazyListStateImpl(
    select: Int,
    val vertical: Boolean,
    val spacing: Dp,
    val spec: AnimationSpec<Int>,
    val contentPadding: PaddingValues,
    val clip: Boolean,
    val keyline: Keyline,
    val windowOffset: Float,
    val itemOffset: Float,
) : LazyListState {
    override var select by mutableIntStateOf(select)
    override val selectFlow = MutableStateFlow(select)
    override val composerStart = MutableStateFlow(SystemClock.elapsedRealtime())
    override val composerEnd = MutableStateFlow(SystemClock.elapsedRealtime())

    fun composerStart() {
        composerStart.tryEmit(SystemClock.elapsedRealtime())
    }

    fun composerEnd() {
        composerEnd.tryEmit(SystemClock.elapsedRealtime())
    }
}

@Composable
private fun rememberLazyListState(
    vertical: Boolean,
    select: Int = 0,
    spacing: Dp = 0.dp,
    spec: AnimationSpec<Int> = spring(),
    contentPadding: PaddingValues = PaddingValues.Zero,
    clip: Boolean = true,
    keyline: Keyline = Keyline.BothEdge,
    windowOffset: Float = 0.5f,
    itemOffset: Float = 0.5f
): LazyListState {
    val saver = remember(
        vertical,
        select,
        spacing,
        spec,
        contentPadding,
        clip,
        keyline,
        windowOffset,
        itemOffset
    ) {
        listSaver(
            save = { listOf(it.select) },
            restore = {
                LazyListStateImpl(
                    select = it[0],
                    vertical = vertical,
                    spacing = spacing,
                    spec = spec,
                    contentPadding = contentPadding,
                    clip = clip,
                    keyline = keyline,
                    windowOffset = windowOffset,
                    itemOffset = itemOffset
                )
            }
        )
    }
    return rememberSaveable(
        vertical,
        select,
        spacing,
        spec,
        contentPadding,
        clip,
        keyline,
        windowOffset,
        itemOffset,
        saver = saver
    ) {
        LazyListStateImpl(
            select = select,
            vertical = vertical,
            spacing = spacing,
            spec = spec,
            contentPadding = contentPadding,
            clip = clip,
            keyline = keyline,
            windowOffset = windowOffset,
            itemOffset = itemOffset
        )
    }
}

@Composable
fun rememberLazyRowState(
    select: Int = 0,
    spacing: Dp = 0.dp,
    spec: AnimationSpec<Int> = spring(),
    contentPadding: PaddingValues = PaddingValues.Zero,
    clip: Boolean = true,
    keyline: Keyline = Keyline.BothEdge,
    windowOffset: Float = 0.5f,
    itemOffset: Float = 0.5f
): LazyListState {
    return rememberLazyListState(
        vertical = false,
        select = select,
        spacing = spacing,
        spec = spec,
        contentPadding = contentPadding,
        clip = clip,
        keyline = keyline,
        windowOffset = windowOffset,
        itemOffset = itemOffset
    )
}

@Composable
fun rememberLazyColumnState(
    select: Int = 0,
    spacing: Dp = 0.dp,
    spec: AnimationSpec<Int> = spring(),
    contentPadding: PaddingValues = PaddingValues.Zero,
    clip: Boolean = true,
    keyline: Keyline = Keyline.BothEdge,
    windowOffset: Float = 0.5f,
    itemOffset: Float = 0.5f
): LazyListState {
    return rememberLazyListState(
        vertical = true,
        select = select,
        spacing = spacing,
        spec = spec,
        contentPadding = contentPadding,
        clip = clip,
        keyline = keyline,
        windowOffset = windowOffset,
        itemOffset = itemOffset
    )
}

@Composable
private fun LazyList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyColumnState(),
    content: LazyListScope.() -> Unit,
) {
    val state by rememberUpdatedState(state)
    val core by remember {
        derivedStateOf {
            LazyListCore(content, state)
        }
    }
    val modifier = modifier
        .thenIf(core.state.clip) { it.clipToBounds() }
        .padding(core.state.contentPadding)
        .focusGroup()
    LazyLayout(
        itemProvider = { core },
        modifier = modifier,
        prefetchState = null,
        measurePolicy = core
    )
}

@Composable
fun LazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyRowState(),
    content: LazyListScope.() -> Unit
) {
    LazyList(modifier, state, content)
}

@Composable
fun LazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyColumnState(),
    content: LazyListScope.() -> Unit
) {
    LazyList(modifier, state, content)
}