package io.github.xiaobaicz.compose.sample

import androidx.compose.runtime.Composable
import io.github.xiaobaicz.compose.foundation.Button
import io.github.xiaobaicz.compose.foundation.Feature
import io.github.xiaobaicz.compose.foundation.Surface
import io.github.xiaobaicz.compose.foundation.Text

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
    Button({}) {
        Text("AAAAA")
    }
    Button({}) {
        Text("AAAAA")
    }
    Button({}) {
        Text("AAAAA")
    }
    Button({}) {
        Text("AAAAA")
    }
    Button({}) {
        Text("AAAAA")
    }
}

@Composable
fun PhoneApp() {

}