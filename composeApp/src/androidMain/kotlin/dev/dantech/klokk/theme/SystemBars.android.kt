package dev.dantech.klokk.theme

import android.app.Activity
import android.os.Build
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView

@Composable
actual fun KlokkSystemBarStyle(dark: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        if (Build.VERSION.SDK_INT < 30) return@SideEffect
        val controller = window.insetsController ?: return@SideEffect
        val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        val appearance = if (dark) 0 else mask
        controller.setSystemBarsAppearance(appearance, mask)
    }
}
