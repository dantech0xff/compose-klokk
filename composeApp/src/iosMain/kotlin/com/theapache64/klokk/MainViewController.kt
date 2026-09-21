package com.theapache64.klokk

import androidx.compose.ui.window.ComposeUIViewController
import com.theapache64.klokk.theme.KlokkTheme

fun MainViewController() = ComposeUIViewController {
    KlokkTheme {
        App()
    }
}
