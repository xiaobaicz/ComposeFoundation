package io.github.xiaobaicz.compose.foundation.tv.home

import androidx.compose.runtime.Composable
import io.github.xiaobaicz.compose.foundation.tv.ScrollListScope

interface Router {
    val key: String

    @Composable
    fun ScrollListScope.Title(controller: Controller)

    @Composable
    fun Content(controller: Controller)
}