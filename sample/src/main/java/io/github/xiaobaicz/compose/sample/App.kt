package io.github.xiaobaicz.compose.sample

import androidx.compose.runtime.Composable
import io.github.xiaobaicz.compose.foundation.Feature

@Composable
fun App() {
    if (Feature.hasLeanback) {
        TVApp()
    } else {
        PhoneApp()
    }
}

@Composable
fun TVApp() {

}

@Composable
fun PhoneApp() {

}