package com.antcloud.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import com.antcloud.app.theme.PinkGradient
import com.antcloud.app.theme.accentPrimaryGreen
import com.antcloud.app.theme.accentPurpleDark
import com.antcloud.app.theme.bgColorDark
import com.antcloud.app.theme.dark_grey
import com.antcloud.app.theme.headingColorDark
import com.antcloud.app.theme.secondaryColor
import com.antcloud.app.theme.secondaryVariant

//val LightColorPalette = lightColors(
//    primary = primaryPurpleLight,
//    secondary = textColorLight,
//    surface = bgColorLight,
//    background = bgColorLight,
//    primaryVariant = headingColorLight,
//    onPrimary = accentPurpleLight,
//    onBackground = accentPurpleLight,
//    onSurface = accentPurpleLight
//)

val DarkColorPalette = darkColors(
//    primary = primaryGreen,
    primary = PinkGradient,
    secondary = secondaryColor/*textColorDark*/,
    secondaryVariant = secondaryVariant,
    background = bgColorDark,
    surface = bgColorDark,
    primaryVariant = headingColorDark,
    onPrimary = accentPurpleDark,
    onSurface = dark_grey,
    onBackground = dark_grey,
    onSecondary = accentPrimaryGreen,
)



@Composable
fun Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else DarkColorPalette,
        typography = typography,
        content = content,
        shapes = shapes
    )
}