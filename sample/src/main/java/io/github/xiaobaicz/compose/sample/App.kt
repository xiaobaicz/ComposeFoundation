package io.github.xiaobaicz.compose.sample

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
        Button({}, modifier = Modifier.padding(top = 8.dp).skeletonItem()) {
            Text("AAAAA")
        }
        Button({}, modifier = Modifier.padding(top = 8.dp).skeletonItem()) {
            Text("AAAAAAAAAA")
        }
        Button({}, modifier = Modifier.padding(top = 8.dp).skeletonItem()) {
            Text("AAAAA")
        }
        Button({}, modifier = Modifier.padding(top = 8.dp).skeletonItem()) {
            Text("AAAAAAAAAA")
        }
        Button({}, modifier = Modifier.padding(top = 8.dp).skeletonItem()) {
            Text("AAAAA")
        }
    }
}

@Composable
fun PhoneApp() {

}