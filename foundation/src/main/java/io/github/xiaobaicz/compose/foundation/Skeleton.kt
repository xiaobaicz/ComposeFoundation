package io.github.xiaobaicz.compose.foundation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VectorizedAnimationSpec
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import kotlin.math.min

class SkeletonState {
    enum class State {
        Loading, Complete;

        val isLoading get() = this == Loading
        val isComplete get() = this == Complete
    }

    var value by mutableStateOf(State.Loading)
        private set

    fun loading() {
        value = State.Loading
    }

    fun complete() {
        value = State.Complete
    }
}

@Composable
fun rememberSkeletonState(): SkeletonState = remember { SkeletonState() }

val LocalSkeletonState = compositionLocalOf {
    SkeletonState.State.Complete
}
private val LocalSkeletonSpec = compositionLocalOf {
    defaultSkeletonAnimationSpec
}
private val LocalSkeletonDraw = compositionLocalOf {
    defaultSkeletonDrawScope
}

@Composable
fun Skeleton(
    state: SkeletonState,
    spec: AnimationSpec<Float> = defaultSkeletonAnimationSpec,
    scope: DrawScope.(Float) -> Unit = defaultSkeletonDrawScope,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSkeletonState provides state.value,
        LocalSkeletonSpec provides spec,
        LocalSkeletonDraw provides scope,
    ) {
        content()
    }
}

fun Modifier.skeletonItem(): Modifier {
    return this then SkeletonItemModifierNodeElement
}

private class SkeletonItemModifierNode : Modifier.Node(), DrawModifierNode,
    CompositionLocalConsumerModifierNode {
    val cache = SingleKeyCache<AnimationSpec<Float>, VectorizedAnimationSpec<AnimationVector1D>>()

    val init = AnimationVector(0f)
    val target = AnimationVector(1f)
    val velocity = AnimationVector(0f)

    val startTime = System.nanoTime()
    val playTime get() = System.nanoTime() - startTime

    var progress by mutableFloatStateOf(0f)

    override fun ContentDrawScope.draw() {
        val state = currentValueOf(LocalSkeletonState)
        if (state.isLoading) {
            val key = currentValueOf(LocalSkeletonSpec)
            val vectorSpec = cache.get(key) { key.vectorize(Float.VectorConverter) }
            progress = vectorSpec.getValueFromNanos(playTime, init, target, velocity).value
            currentValueOf(LocalSkeletonDraw)(progress)
            return
        }
        drawContent()
    }

    class SingleKeyCache<K, V> {
        var key: K? = null
        var value: V? = null

        inline fun get(key: K, block: () -> V): V {
            if (this.key === key) return value!!
            this.key = key
            return block().also {
                value = it
            }
        }
    }
}

private object SkeletonItemModifierNodeElement : ModifierNodeElement<SkeletonItemModifierNode>() {
    override fun create(): SkeletonItemModifierNode {
        return SkeletonItemModifierNode()
    }

    override fun update(node: SkeletonItemModifierNode) {}

    override fun hashCode(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun InspectorInfo.inspectableProperties() {
        properties["name"] = "skeleton"
    }
}

private val defaultSkeletonAnimationSpec: AnimationSpec<Float> = infiniteRepeatable(
    animation = tween(
        durationMillis = 1000,
        delayMillis = 0,
        easing = LinearEasing
    ),
    repeatMode = RepeatMode.Restart,
    initialStartOffset = StartOffset(0),
)

private val defaultSkeletonDrawScope: DrawScope.(Float) -> Unit = {
    drawRoundRect(
        brush = Brush.horizontalGradient(
            0f to Color.Gray,
            it to Color(0xFFCCCCCC),
            1f to Color.Gray,
        ),
        cornerRadius = CornerRadius(min(size.width, size.height) / 2)
    )
}