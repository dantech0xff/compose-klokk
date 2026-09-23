package com.theapache64.klokk.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val CodGray = Color(0xff141414)
val Black = Color(0xff020202)

/**
 * Semantic palette for the redesign. [onPrimary] is text/icons that sit on a
 * [textPrimary]-colored fill; [scrim] is the dim behind overlays/sheets;
 * [clockFace]/[clockHand] are the kinetic-grid dial colors.
 */
data class KlokkColors(
    val background: Color,
    val surface: Color,
    val field: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val tabInactive: Color,
    val toggleOff: Color,
    val border: Color,
    val borderStrong: Color,
    val sheetShadow: Color,
    val scrim: Color,
    val onPrimary: Color,
    val clockFace: Color,
    val clockHand: Color,
)

fun darkKlokkColors() = KlokkColors(
    background = Color(0xff020202),
    surface = Color(0xff0e0e0e),
    field = Color(0xff1a1a1a),
    textPrimary = Color(0xffffffff),
    textSecondary = Color(0xff8b8b8b),
    tabInactive = Color(0xff767676),
    toggleOff = Color(0xff2a2a2a),
    border = Color(0x14ffffff),
    borderStrong = Color(0x2effffff),
    sheetShadow = Color(0x80000000),
    scrim = Color.Black,
    onPrimary = Color.Black,
    clockFace = Color(0xff141414),
    clockHand = Color.White,
)

fun lightKlokkColors() = KlokkColors(
    background = Color(0xfff2f2f7),
    surface = Color(0xffffffff),
    field = Color(0xffe5e5ea),
    textPrimary = Color(0xff1c1c1e),
    textSecondary = Color(0xff6e6e73),
    tabInactive = Color(0xff8e8e93),
    toggleOff = Color(0xffe0e0e5),
    border = Color(0x14000000),
    borderStrong = Color(0x2e000000),
    sheetShadow = Color(0x40000000),
    scrim = Color.Black,
    onPrimary = Color.White,
    clockFace = Color(0xffe8e8ed),
    clockHand = Color(0xff1c1c1e),
)

val LocalKlokkColors = compositionLocalOf { darkKlokkColors() }

// Existing token names kept as composable reads so call sites stay unchanged.
val KlokkBackground: Color @Composable get() = LocalKlokkColors.current.background
val KlokkSurface: Color @Composable get() = LocalKlokkColors.current.surface
val KlokkField: Color @Composable get() = LocalKlokkColors.current.field
val KlokkTextPrimary: Color @Composable get() = LocalKlokkColors.current.textPrimary
val KlokkTextSecondary: Color @Composable get() = LocalKlokkColors.current.textSecondary
val KlokkTabInactive: Color @Composable get() = LocalKlokkColors.current.tabInactive
val KlokkToggleOff: Color @Composable get() = LocalKlokkColors.current.toggleOff
val KlokkBorder: Color @Composable get() = LocalKlokkColors.current.border
val KlokkBorderStrong: Color @Composable get() = LocalKlokkColors.current.borderStrong
val KlokkSheetShadow: Color @Composable get() = LocalKlokkColors.current.sheetShadow
val KlokkScrim: Color @Composable get() = LocalKlokkColors.current.scrim
val KlokkOnPrimary: Color @Composable get() = LocalKlokkColors.current.onPrimary
val KlokkClockFace: Color @Composable get() = LocalKlokkColors.current.clockFace
val KlokkClockHand: Color @Composable get() = LocalKlokkColors.current.clockHand
