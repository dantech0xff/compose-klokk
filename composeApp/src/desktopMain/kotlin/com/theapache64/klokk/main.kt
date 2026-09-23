package com.theapache64.klokk

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.theapache64.klokk.theme.KlokkTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Klokk",
        state = rememberWindowState(
            width = 402.dp,
            height = 874.dp
        ),
        resizable = false
    ) {
        KlokkTheme {
            App()
        }
    }
}
