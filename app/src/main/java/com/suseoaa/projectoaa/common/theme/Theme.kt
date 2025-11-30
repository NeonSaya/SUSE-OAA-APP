package com.suseoaa.projectoaa.common.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun ProjectOAATheme(
    themeConfig: OaaThemeConfig,
    content: @Composable () -> Unit
) {
    val colorScheme = themeConfig.colorScheme
    val shapes = themeConfig.shapes
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            window.statusBarColor = colorScheme.background.toArgb()
            insetsController.isAppearanceLightStatusBars = !themeConfig.isDark
            window.navigationBarColor = colorScheme.background.toArgb()
            insetsController.isAppearanceLightNavigationBars = !themeConfig.isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = shapes,
        content = content
    )
}