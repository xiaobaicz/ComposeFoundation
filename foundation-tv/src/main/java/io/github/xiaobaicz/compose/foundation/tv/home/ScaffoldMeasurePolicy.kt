package io.github.xiaobaicz.compose.foundation.tv.home

import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints

object ScaffoldMeasurePolicy : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        val childConst = constraints.copy(minWidth = 0, minHeight = 0)
        val placeableList = measurables.map { it.measure(childConst) }
        return layout(constraints.maxWidth, constraints.maxHeight) {
            placeableList[0].placeRelative(0, 0, 1f)
            placeableList[1].placeRelative(placeableList[0].width, 0)
        }
    }
}