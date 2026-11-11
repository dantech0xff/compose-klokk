package dev.dantech.klokk

import androidx.compose.ui.window.ComposeUIViewController
import dev.dantech.klokk.theme.KlokkTheme

fun MainViewController() = ComposeUIViewController {
    KlokkTheme {
        App()
    }
}
