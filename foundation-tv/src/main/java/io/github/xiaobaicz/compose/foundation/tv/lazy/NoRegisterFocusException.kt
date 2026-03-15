package io.github.xiaobaicz.compose.foundation.tv.lazy

class NoRegisterFocusException(index: Int) : RuntimeException("Item(index: $index) did not register with registerFocusable()")