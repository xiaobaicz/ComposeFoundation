package io.github.xiaobaicz.compose.foundation

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

object Feature {
    val hasLeanback @Composable get() = hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    val hasLeanbackOnly @Composable get() = hasSystemFeature(PackageManager.FEATURE_LEANBACK_ONLY)

    @Composable
    fun hasSystemFeature(feature: String): Boolean {
        return LocalContext.current.packageManager.hasSystemFeature(feature)
    }
}
