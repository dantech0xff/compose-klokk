package com.theapache64.klokk.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.theapache64.klokk.generated.resources.Res
import com.theapache64.klokk.generated.resources.googlesans_bold
import com.theapache64.klokk.generated.resources.googlesans_medium
import com.theapache64.klokk.generated.resources.googlesans_regular
import org.jetbrains.compose.resources.Font

@Composable
fun klokkTypography(): Typography {
    val googleSans = FontFamily(
        Font(Res.font.googlesans_regular, FontWeight.Normal),
        Font(Res.font.googlesans_medium, FontWeight.Medium),
        Font(Res.font.googlesans_bold, FontWeight.Bold),
    )

    return Typography(

        defaultFontFamily = googleSans,

        h1 = TextStyle(
            fontSize = 95.sp,
            fontWeight = FontWeight.Normal,
        ),
        h2 = TextStyle(
            fontSize = 59.sp,
            fontWeight = FontWeight.Normal,
        ),
        h3 = TextStyle(
            fontSize = 48.sp,
            fontWeight = FontWeight.Medium
        ),
        h4 = TextStyle(
            fontSize = 34.sp,
            fontWeight = FontWeight.Medium,
        ),
        h5 = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        ),
        h6 = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        ),
        subtitle1 = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        ),
        subtitle2 = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        ),
        body1 = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
        ),
        body2 = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        ),
        button = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        ),
        caption = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        ),
        overline = TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
        )
    )
}
