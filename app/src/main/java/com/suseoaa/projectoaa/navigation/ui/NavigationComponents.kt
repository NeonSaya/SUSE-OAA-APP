package com.suseoaa.projectoaa.navigation.ui

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.suseoaa.projectoaa.common.navigation.AppRoutes
import com.suseoaa.projectoaa.common.theme.OaaThemeConfig
import com.suseoaa.projectoaa.common.util.WallpaperManager
import com.suseoaa.projectoaa.courseList.ui.screen.CourseListScreen
import com.suseoaa.projectoaa.courseList.viewmodel.CourseListViewModel
import com.suseoaa.projectoaa.login.ui.ProfileScreen
import com.suseoaa.projectoaa.navigation.viewmodel.HomeViewModel // [新增]
import com.suseoaa.projectoaa.navigation.viewmodel.ShareViewModel
import com.suseoaa.projectoaa.student.ui.StudentFormScreen
import com.suseoaa.projectoaa.student.viewmodel.StudentFormViewModel

// ==========================================
// 1. 动画与工具函数
// ==========================================
private val screenOrder = mapOf("home" to 0, "search" to 1, "settings" to 2, "profile" to 3)
fun getNavigationDirection(from: String, to: String): Boolean { val fromIndex = screenOrder.getOrDefault(from, 0); val toIndex = screenOrder.getOrDefault(to, 0); return toIndex > fromIndex }
fun getEnterTransition(isForward: Boolean): EnterTransition { return slideInHorizontally(initialOffsetX = { if (isForward) it else -it }, animationSpec = tween(300)) + fadeIn(tween(300)) }
fun getExitTransition(isForward: Boolean): ExitTransition { return slideOutHorizontally(targetOffsetX = { if (isForward) -it / 2 else it / 2 }, animationSpec = tween(300)) + fadeOut(tween(300)) }
object NavigationTracker { var lastRoute: String = "home"; fun updateRoute(newRoute: String) { lastRoute = newRoute } }

// ==========================================
// 2. 抽离出的 NavHost (核心路由)
// ==========================================
@Composable
private fun AppNavHost(
    navController: NavHostController,
    viewModel: ShareViewModel, // 这是 ShareViewModel
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ShareViewModel 的稳定 lambda
    val onRefreshWallpaper = remember { { ctx: Context -> WallpaperManager.refreshWallpaper(ctx) } }
    val onSaveWallpaper = remember { { ctx: Context -> WallpaperManager.saveCurrentToGallery(ctx) } }
    val onThemeSelected = remember(viewModel) { { theme: OaaThemeConfig -> viewModel.updateTheme(theme) } }
    val onAppNotificationToggle = remember(viewModel) { { enabled: Boolean -> viewModel.onNotificationToggleChanged(enabled) } }
    val onPrivacyToggle = remember(viewModel) { { enabled: Boolean -> viewModel.onPrivacyToggleChanged(enabled) } }
    val onFeedbackTextChanged = remember(viewModel) { { text: String -> viewModel.onFeedbackTextChanged(text) } }
    val onSubmitFeedback = remember(viewModel) { { ctx: Context -> viewModel.submitFeedback(ctx) } }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Home.route,
        modifier = modifier,
        enterTransition = { getEnterTransition(true) },
        exitTransition = { getExitTransition(true) },
        popEnterTransition = { getEnterTransition(false) },
        popExitTransition = { getExitTransition(false) }
    ) {
        // 注入 HomeViewModel
        composable(route = AppRoutes.Home.route) {
            // 自动创建 HomeViewModel 实例
            val homeViewModel: HomeViewModel = viewModel()
            val homeUiState = homeViewModel.uiState

            HomeContent(
                // HomeViewModel 状态
                isCheckedIn = homeUiState.isCheckedIn,
                checkInCount = homeUiState.checkInCount,
                placeholderImageUrl = homeUiState.placeholderImageUrl,
                currentDate = homeUiState.currentDate,
                cspCountdown = homeUiState.cspCountdown,
                noipCountdown = homeUiState.noipCountdown,
                onCheckIn = homeViewModel::onCheckIn,

                // ShareViewModel 状态
                currentThemeName = viewModel.currentTheme.name,
                onRefreshWallpaper = onRefreshWallpaper,
                onSaveWallpaper = onSaveWallpaper,
                navController = navController
            )
        }

        composable(route = AppRoutes.Search.route) { SearchContent(viewModel) }

        composable(route = AppRoutes.Settings.route) {
            SettingsContent(
                currentTheme = viewModel.currentTheme,
                notificationEnabled = viewModel.notificationEnabled,
                onThemeSelected = onThemeSelected,
                onSaveWallpaper = onSaveWallpaper,
                navController = navController
            )
        }

        composable(route = AppRoutes.Profile.route) {
            ProfileScreen(onBack = { navController.navigate(AppRoutes.Home.route) }, onLogout = onLogout)
        }

        // --- 新增功能页 ---
        composable(route = AppRoutes.CourseList.route) {
            CourseListScreen(
                viewModel = viewModel<CourseListViewModel>(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = AppRoutes.StudentForm.route) {
            StudentFormScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel<StudentFormViewModel>(),
                currentThemeName = viewModel.currentTheme.name
            )
        }

        // --- 设置子页面 ---
        composable(route = "settings_notifications") {
            NotificationsScreen(
                isAppNotificationEnabled = viewModel.notificationEnabled,
                onAppNotificationToggle = onAppNotificationToggle,
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = "settings_privacy") {
            PrivacyScreen(
                isPrivacyEnabled = viewModel.privacyEnabled,
                onPrivacyToggle = onPrivacyToggle,
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = "settings_about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(route = "settings_feedback") {
            FeedbackScreen(
                feedbackText = viewModel.feedbackText,
                isSubmitting = viewModel.isSubmittingFeedback,
                onFeedbackTextChanged = onFeedbackTextChanged,
                onSubmit = onSubmitFeedback,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ==========================================
// 3. 布局组件 (Compact, Medium, Expanded)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactLayout(
    navController: NavHostController,
    viewModel: ShareViewModel,
    onLogout: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: AppRoutes.Home.route
    val showBottomBar = currentRoute in listOf(AppRoutes.Home.route, AppRoutes.Search.route, AppRoutes.Settings.route, AppRoutes.Profile.route)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)) {
                    val navigateTo = { route: String ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                    NavigationBarItem(icon = { Icon(AppRoutes.Home.icon, null) }, label = { Text(AppRoutes.Home.title) }, selected = currentRoute == AppRoutes.Home.route, onClick = { navigateTo(AppRoutes.Home.route) })
                    NavigationBarItem(icon = { Icon(AppRoutes.Search.icon, null) }, label = { Text(AppRoutes.Search.title) }, selected = currentRoute == AppRoutes.Search.route, onClick = { navigateTo(AppRoutes.Search.route) })
                    NavigationBarItem(icon = { Icon(AppRoutes.Settings.icon, null) }, label = { Text(AppRoutes.Settings.title) }, selected = currentRoute == AppRoutes.Settings.route, onClick = { navigateTo(AppRoutes.Settings.route) })
                    NavigationBarItem(icon = { Icon(AppRoutes.Profile.icon, null) }, label = { Text(AppRoutes.Profile.title) }, selected = currentRoute == AppRoutes.Profile.route, onClick = { navigateTo(AppRoutes.Profile.route) })
                }
            }
        }
    ) { padding ->
        AppNavHost(
            navController = navController,
            viewModel = viewModel,
            onLogout = onLogout,
            modifier = Modifier.padding(padding).fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediumLayout(
    navController: NavHostController,
    viewModel: ShareViewModel,
    onLogout: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: AppRoutes.Home.route
    val showNavRail = currentRoute in listOf(AppRoutes.Home.route, AppRoutes.Search.route, AppRoutes.Settings.route, AppRoutes.Profile.route)

    Row(modifier = Modifier.fillMaxSize()) {
        if (showNavRail) {
            NavigationRail(modifier = Modifier.fillMaxHeight(), containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), header = { Icon(Icons.Default.Menu, null, Modifier.padding(vertical = 16.dp)) }) {
                val navigateTo = { route: String ->
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
                NavigationRailItem(icon = { Icon(AppRoutes.Home.icon, null) }, label = { Text(AppRoutes.Home.title) }, selected = currentRoute == AppRoutes.Home.route, onClick = { navigateTo(AppRoutes.Home.route) })
                NavigationRailItem(icon = { Icon(AppRoutes.Search.icon, null) }, label = { Text(AppRoutes.Search.title) }, selected = currentRoute == AppRoutes.Search.route, onClick = { navigateTo(AppRoutes.Search.route) })
                NavigationRailItem(icon = { Icon(AppRoutes.Settings.icon, null) }, label = { Text(AppRoutes.Settings.title) }, selected = currentRoute == AppRoutes.Settings.route, onClick = { navigateTo(AppRoutes.Settings.route) })
                NavigationRailItem(icon = { Icon(AppRoutes.Profile.icon, null) }, label = { Text(AppRoutes.Profile.title) }, selected = currentRoute == AppRoutes.Profile.route, onClick = { navigateTo(AppRoutes.Profile.route) })
            }
        }
        AppNavHost(
            navController = navController,
            viewModel = viewModel,
            onLogout = onLogout,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedLayout(
    navController: NavHostController,
    viewModel: ShareViewModel,
    onLogout: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: AppRoutes.Home.route
    Row(modifier = Modifier.fillMaxSize()) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(modifier = Modifier.width(280.dp).shadow(10.dp)) {
                    Spacer(Modifier.height(16.dp))
                    val navigateTo = { route: String ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                    NavigationDrawerItem(icon = { Icon(AppRoutes.Home.icon, null) }, label = { Text(AppRoutes.Home.title) }, selected = currentRoute == AppRoutes.Home.route, onClick = { navigateTo(AppRoutes.Home.route) }, modifier = Modifier.padding(horizontal = 12.dp))
                    NavigationDrawerItem(icon = { Icon(AppRoutes.Search.icon, null) }, label = { Text(AppRoutes.Search.title) }, selected = currentRoute == AppRoutes.Search.route, onClick = { navigateTo(AppRoutes.Search.route) }, modifier = Modifier.padding(horizontal = 12.dp))
                    NavigationDrawerItem(icon = { Icon(AppRoutes.Settings.icon, null) }, label = { Text(AppRoutes.Settings.title) }, selected = currentRoute == AppRoutes.Settings.route, onClick = { navigateTo(AppRoutes.Settings.route) }, modifier = Modifier.padding(horizontal = 12.dp))
                    NavigationDrawerItem(icon = { Icon(AppRoutes.Profile.icon, null) }, label = { Text(AppRoutes.Profile.title) }, selected = currentRoute == AppRoutes.Profile.route, onClick = { navigateTo(AppRoutes.Profile.route) }, modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        ) {
            AppNavHost(
                navController = navController,
                viewModel = viewModel,
                onLogout = onLogout,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}