package io.github.xiaobaicz.compose.sample

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.xiaobaicz.compose.foundation.Button
import io.github.xiaobaicz.compose.foundation.Feature
import io.github.xiaobaicz.compose.foundation.Skeleton
import io.github.xiaobaicz.compose.foundation.Surface
import io.github.xiaobaicz.compose.foundation.Text
import io.github.xiaobaicz.compose.foundation.rememberSkeletonState
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
    val state = rememberSkeletonState()
    LaunchedEffect(Unit) {
        delay(3000L)
        state.complete()
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