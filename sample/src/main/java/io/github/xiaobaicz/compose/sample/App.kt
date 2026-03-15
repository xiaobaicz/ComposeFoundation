package io.github.xiaobaicz.compose.sample

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.xiaobaicz.compose.foundation.Button
import io.github.xiaobaicz.compose.foundation.Feature
import io.github.xiaobaicz.compose.foundation.Skeleton
import io.github.xiaobaicz.compose.foundation.SkeletonState
import io.github.xiaobaicz.compose.foundation.Surface
import io.github.xiaobaicz.compose.foundation.Text
import io.github.xiaobaicz.compose.foundation.skeletonItem
import io.github.xiaobaicz.compose.foundation.tv.lazy.LazyColumn
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
    var state by remember { mutableStateOf(SkeletonState.Loading) }
    LaunchedEffect(Unit) {
        delay(3000L)
        state = SkeletonState.Complete
    }
    Skeleton(state) {
        var size by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            delay(5000L)
            size = 20
        }
        LazyColumn {
            item {
                Button(
                    onClick = {}, modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .skeletonItem()
                        .markAsFocusable()
                ) {
                    Text("First")
                }
            }
            items(size) {
                Button(
                    onClick = {}, modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .skeletonItem()
                        .markAsFocusable()
                ) {
                    Text("A: $it")
                }
            }
        }
    }
}

@Composable
fun PhoneApp() {

}