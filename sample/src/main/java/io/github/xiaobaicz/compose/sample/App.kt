package io.github.xiaobaicz.compose.sample

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.xiaobaicz.compose.foundation.Button
import io.github.xiaobaicz.compose.foundation.ButtonStateColor
import io.github.xiaobaicz.compose.foundation.Feature
import io.github.xiaobaicz.compose.foundation.RoundButtonDecorator
import io.github.xiaobaicz.compose.foundation.Skeleton
import io.github.xiaobaicz.compose.foundation.Surface
import io.github.xiaobaicz.compose.foundation.Text
import io.github.xiaobaicz.compose.foundation.rememberSkeletonState
import io.github.xiaobaicz.compose.foundation.tv.lazy.LazyList
import io.github.xiaobaicz.compose.foundation.tv.lazy.itemsIndexed
import io.github.xiaobaicz.compose.foundation.tv.lazy.rememberLazyColumnState
import io.github.xiaobaicz.compose.foundation.tv.lazy.rememberLazyRowState
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay

@Composable
fun App() {
    Surface(maxSize = true) {
        if (Feature.hasLeanback) {
            TVApp()
        } else {
            PhoneApp()
        }
    }
}

@Composable
fun TVApp() {
    val skeletonState = rememberSkeletonState()
    Skeleton(skeletonState) {
        val data = remember { mutableStateListOf<Int>() }

        val columnState = rememberLazyColumnState()

        LaunchedEffect(data) {
            columnState.composerEnd.collect {
                if (columnState.requestFocus()) cancel()
            }
        }

        LaunchedEffect(Unit) {
            delay(2000L)
            skeletonState.complete()
            repeat(40) {
                data.add(it)
            }

            repeat(20) {
                delay(5000L)
                data.removeAt(0)
            }
        }

        val decorator = remember {
            RoundButtonDecorator(
                horizontal = 64.dp,
                vertices = 0.dp,
                radius = 32.dp,
                textColor = ButtonStateColor(Color.White, Color.Black),
                background = ButtonStateColor(Color.Gray, Color.Yellow),
            )
        }

        LazyList(state = columnState, modifier = Modifier.fillMaxSize()) {
            itemsIndexed(data, itemKey = { it }) { v, r ->
                LazyList(
                    state = rememberLazyRowState(
                        clip = false,
                        contentPadding = PaddingValues(32.dp)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .registerFocusable()
                ) {
                    items(30) { c ->
                        Button(
                            decorator = decorator,
                            onClick = {}, modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .registerFocusable()
                                .itemFocusAnimByScale(1.15f)
                                .itemFocusBorder(2.dp, Color.Red, RoundedCornerShape(32.dp))
                        ) {
                            Text("$v: $c", lineHeight = 150.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneApp() {

}