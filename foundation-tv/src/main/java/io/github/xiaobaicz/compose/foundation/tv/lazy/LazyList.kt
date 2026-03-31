package io.github.xiaobaicz.compose.foundation.tv.lazy

import android.os.SystemClock
import androidx.collection.arrayMapOf
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasurePolicy
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusEnterExitScope
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusProperties
import androidx.compose.ui.focus.FocusRequesterModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import io.github.xiaobaicz.compose.foundation.thenIf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
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
        itemContent = { itemContent(items[it]) }
    )
}

inline fun <T> LazyListScope.itemsIndexed(
    items: List<T>,
    noinline itemKey: (T) -> Any? = { null },
    noinline itemType: (T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(T, Int) -> Unit,
) {
    items(
        size = items.size,
        itemKey = { itemKey(items[it]) },
        itemType = { itemType(items[it]) },
        itemContent = { itemContent(items[it], it) }
    )
}

private class LazyListCore(
    content: LazyListScope.() -> Unit,
    val state: LazyListStateImpl,
    val coroutineScope: CoroutineScope,
) : LazyListScope, LazyLayoutItemProvider, LazyLayoutMeasurePolicy {
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

    private val itemsList = arrayListOf<Items>()
    private val indexKeyCache = arrayMapOf<Int, Any>()
    private val keyIndexCache = arrayMapOf<Any, Int>()
    private val indexTypeCache = arrayMapOf<Int, Any?>()
    private val indexItemsCache = arrayMapOf<Int, Items>()

    private val layout = if (state.vertical) LayoutByColumn() else LayoutByRow()

    private val chain = NodeChain()

    private var ltr = true

    private var animInProgress = false

    private var animOffset by mutableFloatStateOf(0f)

    init {
        content()
        state.onRequestFocus = ::requestSelectFocus
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
        val itemContent = items.itemContent
        val itemScope = LazyItemScopeImpl(state.spec) { onItemGainFocus(index) }
        itemScope.itemContent(subIndex)
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
        animInProgress = true
        state.composerStart()
        ltr = layoutDirection == LayoutDirection.Ltr
        chain.clear()
        return with(layout) { measure(constraints, state) }
    }

    fun onFocusRestore(properties: FocusProperties) {
        with(properties) {
            onEnter = { onFocusEnter() }
        }
    }

    fun FocusEnterExitScope.onFocusEnter() {
        if (chain.isNotEmpty) {
            if (chain.select.requestFocus()) return
            var target = chain.select.next
            while (target != null) {
                if (target.requestFocus()) return
                target = target.next
            }
            target = chain.select.prev
            while (target != null) {
                if (target.requestFocus()) return
                target = target.next
            }
        }
        cancelFocusChange()
    }

    private fun startScrollAnim(index: Int, offset: Int, skip: Boolean = false) {
        animInProgress = true
        if (skip) {
            state.select = index
            return
        }
        coroutineScope.launch {
            try {
                startAnim { animOffset = it * offset }
            } finally {
                state.select = index
            }
        }
    }

    private suspend inline fun startAnim(progress: (Float) -> Unit) {
        val startTime = SystemClock.elapsedRealtimeNanos()
        val vec = state.spec.vectorize(Float.VectorConverter)
        val initValue = AnimationVector1D(0f)
        val targetValue = AnimationVector1D(1f)
        val duration = vec.getDurationNanos(initValue, targetValue, initValue)
        while (currentCoroutineContext().isActive) {
            val current = SystemClock.elapsedRealtimeNanos() - startTime
            if (current > duration) break
            val value = vec.getValueFromNanos(current, initValue, targetValue, initValue)
            progress(value.value)
            delay(5)
        }
        progress(1f)
    }

    fun onItemGainFocus(index: Int) {
        if (chain.isEmpty) return
        when {
            chain.select.index == index -> return
            chain.prev?.index == index -> startScrollAnim(index, chain.forward)
            chain.next?.index == index -> startScrollAnim(index, chain.backward)
            else -> startScrollAnim(index, 0, true)
        }
    }

    fun onKeyIntercept(event: KeyEvent): Boolean {
        return animInProgress
    }

    fun requestSelectFocus(): Boolean {
        if (chain.isEmpty) return false
        return chain.select.requestFocus()
    }

    fun onScroll(scope: GraphicsLayerScope) {
        with(scope) {
            if (state.vertical) {
                translationY = -animOffset
            } else {
                translationX = if (ltr) -animOffset else animOffset
            }
        }
    }

    private data class ItemNode(
        val index: Int = 0,
        val axisSize: Int = 0,
        val crossSize: Int = 0,
        val placeable: Placeable,
        var offset: Int = 0,
        var prev: ItemNode? = null,
        var next: ItemNode? = null,
    ) {
        val parentData = (placeable.parentData as? LazyParentData) ?: LazyParentData.EmptyNode
        val focusable = parentData.focusableNode != null
        val startEdge get() = offset
        val endEdge get() = offset + axisSize

        fun requestFocus(): Boolean {
            val node = parentData.focusableNode
            if (node == null || !node.isAttached) return false
            if (node.hasFocus) return true
            return node.requestFocus()
        }
    }

    private class NodeChain(
        private var selectOrNull: ItemNode? = null,
        private var firstOrNull: ItemNode? = selectOrNull,
        private var lastOrNull: ItemNode? = selectOrNull,
        var prev: ItemNode? = null,
        var next: ItemNode? = null,
        var forward: Int = 0,
        var backward: Int = 0,
    ) {
        val cache = arrayMapOf<Int, ItemNode>()

        var select
            get() = selectOrNull!!
            set(value) {
                selectOrNull = value
                first = value
                last = value
            }
        var first
            get() = firstOrNull!!
            set(value) {
                firstOrNull = value
            }
        var last
            get() = lastOrNull!!
            set(value) {
                lastOrNull = value
            }

        val isEmpty get() = selectOrNull == null
        val isNotEmpty get() = !isEmpty

        inline fun prev(block: NodeChain.(ItemNode) -> Unit) {
            prev?.also { block(it) }
        }

        inline fun next(block: NodeChain.(ItemNode) -> Unit) {
            next?.also { block(it) }
        }

        inline fun forEach(block: (ItemNode) -> Unit) {
            var next: ItemNode? = first
            while (next != null) {
                block(next)
                next = next.next
            }
        }

        inline fun findFirst(block: (ItemNode) -> Boolean): ItemNode? {
            var next: ItemNode? = first
            while (next != null) {
                if (block(next)) return next
                next = next.next
            }
            return null
        }

        fun axisSize(): Int {
            var size = 0
            forEach { size += it.axisSize }
            return size
        }

        fun crossSize(): Int {
            var max = 0
            forEach { max = max(max, it.crossSize) }
            return max
        }

        fun offsetPlus(offset: Int) {
            forEach { it.offset += offset }
        }

        fun reset() {
            selectOrNull = null
            firstOrNull = null
            lastOrNull = null
            prev = null
            next = null
            forward = 0
            backward = 0
        }

        fun clear() {
            reset()
            cache.clear()
        }
    }

    private abstract inner class Layout {
        abstract val IntrinsicMeasureScope.paddingStart: Int
        abstract val IntrinsicMeasureScope.paddingEnd: Int

        fun LazyLayoutMeasureScope.createNode(const: Constraints, index: Int): ItemNode {
            return chain.cache.getOrPut(index) { onCreateNode(const, index) }
        }

        abstract fun LazyLayoutMeasureScope.onCreateNode(const: Constraints, index: Int): ItemNode

        fun LazyLayoutMeasureScope.createNodeChain(constraints: Constraints): NodeChain {
            val selectIndex = state.select.coerceIn(0, itemCount - 1)
            val selectNode = createNode(constraints, selectIndex)
            if (!selectNode.focusable) throw NoRegisterFocusException(selectIndex)
            val windowOffset = state.windowOffset * containerAxisSize(constraints)
            val selectNodeOffset = selectNode.axisSize * state.itemOffset
            selectNode.offset = (windowOffset - selectNodeOffset).roundToInt()
            chain.reset()
            return chain.also { it.select = selectNode }
        }

        fun LazyLayoutMeasureScope.fillStartEdge(
            constraints: Constraints,
            condition: (ItemNode) -> Boolean
        ) {
            val spacing = state.spacing.roundToPx()
            while (true) {
                val firstNode = chain.first
                if (chain.select !== firstNode && condition(firstNode)) break
                if (firstNode.index - 1 < 0) break
                val prevNode = createNode(constraints, firstNode.index - 1)
                prevNode.offset = firstNode.startEdge - spacing - prevNode.axisSize
                firstNode.prev = prevNode
                prevNode.next = firstNode
                chain.first = prevNode
            }
        }

        fun LazyLayoutMeasureScope.fillStartEdge(constraints: Constraints) {
            val containerAxisSize = containerAxisSize(constraints)
            val windowOffset = containerAxisSize * state.windowOffset
            val limit = windowOffset.roundToInt()
            val edge = (chain.select.startEdge - limit).coerceAtMost(0)
            val maxEdge = edge - paddingStart
            fillStartEdge(constraints) { it.startEdge <= maxEdge }
        }

        fun LazyLayoutMeasureScope.fillEndEdge(
            constraints: Constraints,
            condition: (ItemNode) -> Boolean
        ) {
            val spacing = state.spacing.roundToPx()
            while (true) {
                val lastNode = chain.last
                if (chain.select !== lastNode && condition(lastNode)) break
                if (lastNode.index + 1 >= itemCount) break
                val nextNode = createNode(constraints, lastNode.index + 1)
                nextNode.offset = lastNode.endEdge + spacing
                lastNode.next = nextNode
                nextNode.prev = lastNode
                chain.last = nextNode
            }
        }

        fun LazyLayoutMeasureScope.fillEndEdge(constraints: Constraints) {
            val containerAxisSize = containerAxisSize(constraints)
            val windowOffset = containerAxisSize * state.windowOffset
            val limit = (containerAxisSize - windowOffset).roundToInt()
            val edge = (chain.select.endEdge + limit).coerceAtLeast(containerAxisSize)
            val maxEdge = edge + paddingEnd
            fillEndEdge(constraints) { it.endEdge >= maxEdge }
        }

        abstract fun containerAxisSize(constraints: Constraints): Int

        fun move2StartEdge(): Boolean {
            if (chain.first.startEdge > 0) {
                val offset = 0 - chain.first.startEdge
                chain.offsetPlus(offset)
                return true
            }
            return false
        }

        fun move2EndEdge(edge: Int): Boolean {
            if (chain.last.endEdge < edge) {
                val offset = edge - chain.last.endEdge
                chain.offsetPlus(offset)
                return true
            }
            return false
        }

        fun LazyLayoutMeasureScope.measure(
            constraints: Constraints,
            state: LazyListStateImpl,
        ): MeasureResult {
            chain.reset()
            if (itemCount < 1) return layout(constraints.minWidth, constraints.minHeight) {
                onLayoutEnd()
            }
            val childConst = constraints.copy(minWidth = 0, minHeight = 0)
            createNodeChain(childConst)

            val containerAxisSize = containerAxisSize(childConst)

            var hasNext = false
            fillEndEdge(childConst) { node ->
                node.focusable.also { hasNext = it }
            }
            chain.next = if (hasNext) chain.last else null

            var hasPrev = false
            fillStartEdge(childConst) { node ->
                node.focusable.also { hasPrev = it }
            }
            chain.prev = if (hasPrev) chain.first else null

            when (state.keyline) {
                Keyline.BothEdge -> {
                    chain.next { fillEndEdge(childConst) }
                    move2EndEdge(containerAxisSize)
                    chain.prev { fillStartEdge(childConst) }
                    if (move2StartEdge()) {
                        chain.next { fillEndEdge(childConst) }
                    }
                    chain.prev {
                        val maxForward = first.startEdge.coerceAtMost(0)
                        val maxBackward = (last.endEdge - containerAxisSize).coerceAtLeast(0)
                        val forward = calculateScrollOffset(childConst, it)
                        this.forward = min(maxBackward, max(maxForward, forward))
                    }
                    chain.next {
                        val maxForward = first.startEdge.coerceAtMost(0)
                        val maxBackward = (last.endEdge - containerAxisSize).coerceAtLeast(0)
                        val backward = calculateScrollOffset(childConst, it)
                        this.backward = max(maxForward, min(maxBackward, backward))
                    }
                }

                Keyline.StartEdge -> {
                    chain.prev { fillStartEdge(childConst) }
                    move2StartEdge()
                    chain.next { fillEndEdge(childConst) }
                    chain.prev {
                        val maxForward = first.startEdge.coerceAtMost(0)
                        val forward = calculateScrollOffset(childConst, it)
                        this.forward = max(maxForward, forward)
                    }
                    chain.next {
                        this.backward = calculateScrollOffset(childConst, it).coerceAtLeast(0)
                    }
                }

                Keyline.EndEdge -> {
                    chain.next { fillEndEdge(childConst) }
                    move2EndEdge(containerAxisSize)
                    chain.prev {
                        fillStartEdge(childConst)
                        this.forward = calculateScrollOffset(childConst, it).coerceAtMost(0)
                    }
                    chain.next {
                        val maxBackward = (last.endEdge - containerAxisSize).coerceAtLeast(0)
                        val backward = calculateScrollOffset(childConst, it)
                        this.backward = min(maxBackward, backward)
                    }
                }

                Keyline.NoEdge -> {
                    chain.prev { fillStartEdge(childConst) }
                    chain.next { fillEndEdge(childConst) }
                    chain.prev {
                        this.forward = calculateScrollOffset(childConst, it).coerceAtMost(0)
                    }
                    chain.next {
                        this.backward = calculateScrollOffset(childConst, it).coerceAtLeast(0)
                    }
                }
            }

            val focusNode = chain.findFirst { it.parentData.focusableNode?.hasFocus == true }
            if (focusNode != null) {
                if (focusNode.index != chain.select.index) {
                    state.select = focusNode.index
                    return measure(constraints, state)
                }
                requestSelectFocus()
            }

            return layout(constraints) {
                onLayoutEnd()
            }
        }

        abstract fun LazyLayoutMeasureScope.layout(
            constraints: Constraints,
            layoutEnd: () -> Unit
        ): MeasureResult

        private fun onLayoutEnd() {
            animOffset = 0f
            state.composerEnd()
            animInProgress = false
        }

        private fun calculateScrollOffset(constraints: Constraints, node: ItemNode): Int {
            val windowOffset = state.windowOffset * containerAxisSize(constraints)
            val selectNodeOffset = node.axisSize * state.itemOffset
            val startEdge = (windowOffset - selectNodeOffset).roundToInt()
            return node.startEdge - startEdge
        }
    }

    private inner class LayoutByColumn : Layout() {
        override val IntrinsicMeasureScope.paddingStart: Int
            get() = state.contentPadding.calculateTopPadding().roundToPx()
        override val IntrinsicMeasureScope.paddingEnd: Int
            get() = state.contentPadding.calculateBottomPadding().roundToPx()

        override fun LazyLayoutMeasureScope.onCreateNode(const: Constraints, index: Int): ItemNode {
            val placeableList = compose(index).map { it.measure(const) }
            if (placeableList.size != 1) throw PlaceableQuantityException(index)
            val placeable = placeableList.first()
            return ItemNode(index, placeable.height, placeable.width, placeable)
        }

        override fun containerAxisSize(constraints: Constraints): Int {
            return constraints.maxHeight
        }

        override fun LazyLayoutMeasureScope.layout(
            constraints: Constraints,
            layoutEnd: () -> Unit
        ): MeasureResult {
            val width = constraints.constrainWidth(chain.crossSize())
            val height = constraints.constrainHeight(chain.axisSize())
            return layout(width, height) {
                chain.forEach { it.placeable.placeRelative(0, it.offset) }
                layoutEnd()
            }
        }
    }

    private inner class LayoutByRow : Layout() {
        override val IntrinsicMeasureScope.paddingStart: Int
            get() = state.contentPadding.calculateStartPadding(layoutDirection).roundToPx()
        override val IntrinsicMeasureScope.paddingEnd: Int
            get() = state.contentPadding.calculateEndPadding(layoutDirection).roundToPx()

        override fun LazyLayoutMeasureScope.onCreateNode(const: Constraints, index: Int): ItemNode {
            val placeableList = compose(index).map { it.measure(const) }
            if (placeableList.size != 1) throw PlaceableQuantityException(index)
            val placeable = placeableList.first()
            return ItemNode(index, placeable.width, placeable.height, placeable)
        }

        override fun containerAxisSize(constraints: Constraints): Int {
            return constraints.maxWidth
        }

        override fun LazyLayoutMeasureScope.layout(
            constraints: Constraints,
            layoutEnd: () -> Unit
        ): MeasureResult {
            val width = constraints.constrainWidth(chain.axisSize())
            val height = constraints.constrainHeight(chain.crossSize())
            return layout(width, height) {
                chain.forEach { it.placeable.placeRelative(it.offset, 0) }
                layoutEnd()
            }
        }
    }
}

sealed interface LazyItemScope {
    @Composable
    fun Modifier.registerFocusable(): Modifier

    @Composable
    fun Modifier.itemFocusAnim(block: GraphicsLayerScope.(Float) -> Unit): Modifier

    @Composable
    fun Modifier.itemFocusDecorate(block: ContentDrawScope.(Boolean) -> Unit): Modifier

    @Composable
    fun Modifier.itemFocusAnimByScale(focus: Float, unfocus: Float = 1f): Modifier {
        return itemFocusAnim {
            val multiple = it * (focus - unfocus) + unfocus
            scaleX = multiple
            scaleY = multiple
        }
    }

    @Composable
    fun Modifier.itemFocusBorder(width: Dp, color: Color, shape: Shape): Modifier {
        return itemFocusDecorate {
            drawContent()
            if (!it) return@itemFocusDecorate
            val outline = shape.createOutline(size, layoutDirection, this)
            drawOutline(outline, color, 1f, Stroke(width.toPx()))
        }
    }
}

private class LazyItemScopeImpl(
    val spec: AnimationSpec<Float>,
    val onItemGainFocus: () -> Unit
) : LazyItemScope {
    private var hasFocus: Boolean by mutableStateOf(false)

    @Composable
    override fun Modifier.registerFocusable(): Modifier {
        return onFocusChanged {
            hasFocus = it.hasFocus
            if (it.hasFocus) onItemGainFocus()
        }.then(LazyFocusableItemElement)
    }

    @Composable
    override fun Modifier.itemFocusAnim(block: GraphicsLayerScope.(Float) -> Unit): Modifier {
        val progress by remember { derivedStateOf { if (hasFocus) 1f else 0f } }
        val progressAnim by animateFloatAsState(progress, spec)
        return graphicsLayer { block(progressAnim) }
    }

    @Composable
    override fun Modifier.itemFocusDecorate(block: ContentDrawScope.(Boolean) -> Unit): Modifier {
        return drawWithContent { block(hasFocus) }
    }
}

private class LazyParentData(
    var focusableNode: LazyFocusableItemNode? = null
) {
    companion object {
        val EmptyNode = LazyParentData()
    }
}

private object LazyFocusableItemElement : ModifierNodeElement<LazyFocusableItemNode>() {
    override fun create(): LazyFocusableItemNode {
        return LazyFocusableItemNode()
    }

    override fun update(node: LazyFocusableItemNode) {}

    override fun hashCode(): Int = 0

    override fun equals(other: Any?): Boolean = this === other

    override fun InspectorInfo.inspectableProperties() {
        properties["name"] = "LazyItemRegisterFocusableElement"
    }
}

private class LazyFocusableItemNode : Modifier.Node(),
    FocusRequesterModifierNode,
    FocusEventModifierNode,
    ParentDataModifierNode {
    var hasFocus = false
        private set

    override fun Density.modifyParentData(parentData: Any?): Any {
        val parentData = (parentData as? LazyParentData) ?: LazyParentData()
        parentData.focusableNode = this@LazyFocusableItemNode
        return parentData
    }

    override fun onFocusEvent(focusState: FocusState) {
        hasFocus = focusState.hasFocus
    }
}

sealed interface LazyListState {
    val select: Int
    val selectFlow: Flow<Int>
    val composerStart: Flow<Long>
    val composerEnd: Flow<Long>

    fun requestFocus(): Boolean
}

private class LazyListStateImpl(
    select: Int,
    val vertical: Boolean,
    val spacing: Dp,
    val spec: AnimationSpec<Float>,
    val contentPadding: PaddingValues,
    val clip: Boolean,
    val keyline: Keyline,
    val windowOffset: Float,
    val itemOffset: Float,
) : LazyListState {
    init {
        require(windowOffset in 0f..1f) { "windowOffset should be in range [0f, 1f]" }
        require(itemOffset in 0f..1f) { "itemOffset should be in range [0f, 1f]" }
        require(spacing >= 0.dp) { "spacing should be non-negative" }
    }

    override var select by mutableIntStateOf(select)
    override val selectFlow = snapshotFlow { this.select }
    override val composerStart = MutableStateFlow(SystemClock.elapsedRealtime())
    override val composerEnd = MutableStateFlow(SystemClock.elapsedRealtime())
    var onRequestFocus = { false }

    override fun requestFocus(): Boolean {
        return onRequestFocus()
    }

    fun composerStart() {
        composerStart.tryEmit(SystemClock.elapsedRealtime())
    }

    fun composerEnd() {
        composerEnd.tryEmit(SystemClock.elapsedRealtime())
    }

    class StateSaver(
        val vertical: Boolean,
        val spacing: Dp,
        val spec: AnimationSpec<Float>,
        val contentPadding: PaddingValues,
        val clip: Boolean,
        val keyline: Keyline,
        val windowOffset: Float,
        val itemOffset: Float
    ) : Saver<LazyListStateImpl, Int> {
        override fun SaverScope.save(value: LazyListStateImpl): Int {
            return value.select
        }

        override fun restore(value: Int): LazyListStateImpl {
            return LazyListStateImpl(
                select = value,
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
}

enum class Keyline {
    BothEdge, StartEdge, EndEdge, NoEdge
}

private class PlaceableQuantityException(itemIndex: Int) :
    RuntimeException("Item $itemIndex placeable quantity is not 1")

private class NoRegisterFocusException(index: Int) :
    RuntimeException("Item(index: $index) did not register with registerFocusable()")

private val defSpec = tween<Float>(150, 0, LinearEasing)

@Composable
private fun rememberLazyListState(
    vertical: Boolean,
    select: Int,
    spacing: Dp,
    spec: AnimationSpec<Float>,
    contentPadding: PaddingValues,
    clip: Boolean,
    keyline: Keyline,
    windowOffset: Float,
    itemOffset: Float,
): LazyListState {
    val saver = remember(
        vertical, spacing, spec, contentPadding,
        clip, keyline, windowOffset, itemOffset,
    ) {
        LazyListStateImpl.StateSaver(
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
    return rememberSaveable(
        vertical, select, spacing, spec, contentPadding,
        clip, keyline, windowOffset, itemOffset,
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
            itemOffset = itemOffset,
        )
    }
}

@Composable
fun rememberLazyRowState(
    select: Int = 0,
    spacing: Dp = 0.dp,
    spec: AnimationSpec<Float> = defSpec,
    contentPadding: PaddingValues = PaddingValues.Zero,
    clip: Boolean = true,
    keyline: Keyline = Keyline.BothEdge,
    windowOffset: Float = 0.5f,
    itemOffset: Float = 0.5f,
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
        itemOffset = itemOffset,
    )
}

@Composable
fun rememberLazyColumnState(
    select: Int = 0,
    spacing: Dp = 0.dp,
    spec: AnimationSpec<Float> = defSpec,
    contentPadding: PaddingValues = PaddingValues.Zero,
    clip: Boolean = true,
    keyline: Keyline = Keyline.BothEdge,
    windowOffset: Float = 0.5f,
    itemOffset: Float = 0.5f,
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
        itemOffset = itemOffset,
    )
}

@Composable
fun LazyList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyColumnState(),
    content: LazyListScope.() -> Unit,
) {
    val state by rememberUpdatedState(state)
    val content by rememberUpdatedState(content)
    val coroutineScope = rememberCoroutineScope()
    val core by remember {
        derivedStateOf {
            val state = state as LazyListStateImpl
            LazyListCore(content, state, coroutineScope)
        }
    }
    val modifier = modifier
        .onPreviewKeyEvent(core::onKeyIntercept)
        .thenIf(core.state.clip) { it.clipToBounds() }
        .padding(core.state.contentPadding)
        .graphicsLayer(core::onScroll)
        .focusProperties(core::onFocusRestore)
        .focusGroup()
    LazyLayout(
        itemProvider = { core },
        modifier = modifier,
        prefetchState = null,
        measurePolicy = core
    )
}