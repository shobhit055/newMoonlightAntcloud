package com.limelight

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import com.limelight.theme.PinkGradient
import com.limelight.theme.accentPrimaryGreen
import com.limelight.theme.accentPurpleDark
import com.limelight.theme.bgColorDark
import com.limelight.theme.headingColorDark
import com.limelight.theme.secondaryColor
import com.limelight.theme.secondaryVariant

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
    onSurface = accentPurpleDark,
    onBackground = accentPurpleDark,
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