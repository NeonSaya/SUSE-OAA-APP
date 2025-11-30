package com.suseoaa.projectoaa.navigation

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.suseoaa.projectoaa.navigation.ui.CompactLayout
import com.suseoaa.projectoaa.navigation.ui.ExpandedLayout
import com.suseoaa.projectoaa.navigation.ui.MediumLayout
import com.suseoaa.projectoaa.navigation.viewmodel.ShareViewModel

@Composable
fun AdaptiveApp(
    windowSizeClass: WindowWidthSizeClass,
    onLogout: () -> Unit,
    shareViewModel: ShareViewModel
) {
    val navController = rememberNavController()

    when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> {
            CompactLayout(
                navController = navController,
                onLogout = onLogout,
                shareViewModel = shareViewModel
            )
        }
        WindowWidthSizeClass.Medium -> {
            MediumLayout(
                navController = navController,
                onLogout = onLogout,
                shareViewModel = shareViewModel
            )
        }
        WindowWidthSizeClass.Expanded -> {
            ExpandedLayout(
                navController = navController,
                onLogout = onLogout,
                shareViewModel = shareViewModel
            )
        }
    }
}