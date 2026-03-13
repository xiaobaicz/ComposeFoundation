package io.github.xiaobaicz.compose.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.xiaobaicz.compose.foundation.Button
import io.github.xiaobaicz.compose.foundation.Feature
import io.github.xiaobaicz.compose.foundation.Surface
import io.github.xiaobaicz.compose.foundation.Text
import io.github.xiaobaicz.compose.foundation.theme.Theme

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
    Surface(modifier = Modifier.fillMaxSize()) {
        Button({}, decorator = Theme.buttonDecorators.roundButton) {
            Text("AAAAA")
        }
        Button({}, decorator = Theme.buttonDecorators.roundButton) {
            Text("AAAAA")
        }
        Button({}, decorator = Theme.buttonDecorators.roundButton) {
            Text("AAAAA")
        }
        Button({}, decorator = Theme.buttonDecorators.roundButton) {
            Text("AAAAA")
        }
        Button({}, decorator = Theme.buttonDecorators.roundButton) {
            Text("AAAAA")
        }
    }
}

@Composable
fun PhoneApp() {

}