package io.github.xiaobaicz.compose.foundation.tv.home

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class Controller(show: Boolean, select: Int) {
    var showSidebar by mutableStateOf(show)
        private set

    var select by mutableIntStateOf(select)
        private set

    fun switch(index: Int) {
        this.select = index
    }

    fun showSidebar() {
        this.showSidebar = true
    }

    fun hideSidebar() {
        this.showSidebar = false
    }

    companion object {
        private const val KEY_SHOW_SIDEBAR = "0"
        private const val KEY_SELECT = "1"

        val Saver = object : Saver<Controller, Bundle> {
            override fun SaverScope.save(value: Controller): Bundle {
                return Bundle().also {
                    it.putBoolean(KEY_SHOW_SIDEBAR, value.showSidebar)
                    it.putInt(KEY_SELECT, value.select)
                }
            }

            override fun restore(value: Bundle): Controller {
                val show = value.getBoolean(KEY_SHOW_SIDEBAR, true)
                val select = value.getInt(KEY_SELECT, 0)
                return Controller(show, select)
            }
        }
    }
}

@Composable
fun rememberController(open: Boolean = true, select: Int = 0): Controller {
    return rememberSaveable(open, select, saver = Controller.Saver) {
        Controller(open, select)
    }
}