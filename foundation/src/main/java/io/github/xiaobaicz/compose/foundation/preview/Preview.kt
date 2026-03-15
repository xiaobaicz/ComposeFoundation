package io.github.xiaobaicz.compose.foundation.preview

import androidx.compose.ui.tooling.preview.Preview

@Preview(group = "LTR", showBackground = true, device = Devices.PHONE, locale = "en")
annotation class PagePreview

@Preview(group = "RTL", showBackground = true, device = Devices.PHONE, locale = "ar")
annotation class PageRTLPreview

@Preview(group = "LTR", device = Devices.PHONE, locale = "en")
annotation class Preview

@Preview(group = "RTL", device = Devices.PHONE, locale = "ar")
annotation class RTLPreview