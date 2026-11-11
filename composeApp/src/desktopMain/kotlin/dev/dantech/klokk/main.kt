package dev.dantech.klokk

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.dantech.klokk.theme.KlokkTheme

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
