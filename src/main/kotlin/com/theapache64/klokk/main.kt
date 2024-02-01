package com.theapache64.klokk

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.theapache64.klokk.composable.BottomToolBar
import com.theapache64.klokk.composable.Clock
import com.theapache64.klokk.movement.alphabet.TextMatrixGenerator
import com.theapache64.klokk.movement.core.Movement
import com.theapache64.klokk.theme.Black
import com.theapache64.klokk.theme.KlokkTheme
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

const val COLUMNS = 15
const val ROWS = 8
const val PADDING = 100
const val CLOCK_SIZE = 60
const val CLOCKS_CONTAINER_WIDTH = CLOCK_SIZE * COLUMNS
const val CLOCKS_CONTAINER_HEIGHT = CLOCK_SIZE * ROWS
const val IS_DEBUG = true

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalFoundationApi
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kinetic Wall Clock",
        state = rememberWindowState(
            width = (CLOCKS_CONTAINER_WIDTH + PADDING).dp,
            height = (CLOCKS_CONTAINER_HEIGHT + PADDING + 40).dp
        ),
        resizable = false
    ) {
        KlokkTheme {
            App()
        }
    }
}