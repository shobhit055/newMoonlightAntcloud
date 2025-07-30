package com.antcloud.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.antcloud.app.components.novaSquare

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp)



    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val subtitle = TextStyle(
    fontFamily = novaSquare,
    fontWeight = FontWeight.Light,
    fontSize = 16.sp,
    color = textColorWhite
)
val titleText = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    color = textColorWhite
)

val mainTitle = TextStyle(
    fontFamily = novaSquare,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    textAlign = TextAlign.Center,
    color = textColorDark
)
val heading = TextStyle(
    fontFamily = novaSquare,
    fontWeight = FontWeight.Normal,
    textAlign = TextAlign.Center,
    fontSize = 28.sp,
    color = textColorDark
)
