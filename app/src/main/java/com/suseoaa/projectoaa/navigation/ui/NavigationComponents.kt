package com.suseoaa.projectoaa.navigation.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.suseoaa.projectoaa.navigation.viewmodel.ShareViewModel

// 定义页面顺序，用于确定动画方向
private val screenOrder = mapOf(
    "home" to 0,
    "search" to 1,
    "settings" to 2,
    "profile" to 3
)

// 根据导航方向确定动画方向
fun getEnterTransition(isForward: Boolean): EnterTransition {
    return if (isForward) {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    } else {
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ) + fadeIn(
            animationSpec = tween(300)
        )
    }
}

fun getExitTransition(isForward: Boolean): ExitTransition {
    return if (isForward) {
        slideOutHorizontally(
            targetOffsetX = { -it / 2 },
            animationSpec = tween(300)
        ) + fadeOut(
            animationSpec = tween(300)
        )
    } else {
        slideOutHorizontally(
            targetOffsetX = { it / 2 },
            animationSpec = tween(300)
        ) + fadeOut(
            animationSpec = tween(300)
        )
    }
}

// 获取导航方向（基于页面在底部导航栏中的顺序）
fun getNavigationDirection(from: String, to: String): Boolean {
    val fromIndex = screenOrder.getOrDefault(from, 0)
    val toIndex = screenOrder.getOrDefault(to, 0)
    return toIndex > fromIndex
}

// 使用一个简单的状态来追踪上一个访问的页面
object NavigationTracker {
    var lastRoute: String = "home"
        private set

    fun updateRoute(newRoute: String) {
        lastRoute = newRoute
    }
}

// ========== 手机布局：底部导航栏 ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactLayout(navController: NavHostController, viewModel: ShareViewModel) {
    // 当前选中的页面（从 NavController 获取）
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route ?: "home"

    // 跟踪导航方向
    var isForward by remember { mutableStateOf(true) }

    // 当前路由变化时更新导航方向
    LaunchedEffect(currentRoute) {
        isForward = getNavigationDirection(NavigationTracker.lastRoute, currentRoute)
        NavigationTracker.updateRoute(currentRoute)
    }

    Scaffold(
        // 【顶部栏】
        topBar = {
            TopAppBar(
                title = { Text("手机模式") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
                )
            )
        },

        // 【底部导航栏】典型的手机布局
        bottomBar = {
            NavigationBar {
                // 首页
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Home, contentDescription = null
                        )
                    },
                    label = { Text("首页") },
                    selected = currentRoute == "home",
                    onClick = {
                        if (currentRoute != "home") {
                            navController.navigate("home") {
                                popUpTo(
                                    navController.graph.startDestinationId
                                ) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    })

                // 搜索
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Search, contentDescription = null
                        )
                    },
                    label = { Text("搜索") },
                    selected = currentRoute == "search",
                    onClick = {
                        if (currentRoute != "search") {
                            navController.navigate("search") {
                                popUpTo(
                                    navController.graph.startDestinationId
                                ) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    })

                // 设置
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Settings, contentDescription = null
                        )
                    },
                    label = { Text("设置") },
                    selected = currentRoute == "settings",
                    onClick = {
                        if (currentRoute != "settings") {
                            navController.navigate("settings") {
                                popUpTo(
                                    navController.graph.startDestinationId
                                )
                                { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    })

                // 个人
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Person, contentDescription = null
                        )
                    },
                    label = { Text("个人") },
                    selected = currentRoute == "profile",
                    onClick = {
                        if (currentRoute != "profile") {
                            navController.navigate("profile") {
                                popUpTo(
                                    navController.graph.startDestinationId
                                ) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    })
            }
        }) { padding ->
        // 【内容区域】使用 NavHost（带过渡动画），并把 ShareViewModel 传给需要的 screen（满足 MVVM）
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            enterTransition = {
                getEnterTransition(isForward)
            },
            exitTransition = {
                getExitTransition(isForward)
            },
            popEnterTransition = {
                getEnterTransition(isForward)
            },
            popExitTransition = {
                getExitTransition(isForward)
            }
        ) {
            composable(route = "home") { HomeContent(viewModel) }
            composable(route = "search") { SearchContent(viewModel) }
            composable(route = "settings") { SettingsContent(viewModel) }
            composable(route = "profile") { ProfileContent(viewModel) }
        }
    }
}

// ========== 小平板布局：侧边导航栏 ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediumLayout(navController: NavHostController, viewModel: ShareViewModel) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route ?: "home"

    // 跟踪导航方向
    var isForward by remember { mutableStateOf(true) }

    // 当前路由变化时更新导航方向
    LaunchedEffect(currentRoute) {
        isForward = getNavigationDirection(NavigationTracker.lastRoute, currentRoute)
        NavigationTracker.updateRoute(currentRoute)
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // 【左侧导航栏】紧凑的侧边栏，只显示图标
        NavigationRail(
            modifier = Modifier.fillMaxHeight(), header = {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }) {
            NavigationRailItem(
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text("首页") },
                selected = currentRoute == "home",
                onClick = {
                    if (currentRoute != "home") {
                        navController.navigate("home") {
                            popUpTo(
                                navController.graph.startDestinationId
                            ) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                })

            NavigationRailItem(
                icon = {
                    Icon(
                        Icons.Default.Search, contentDescription = null
                    )
                },
                label = { Text("搜索") },
                selected = currentRoute == "search",
                onClick = {
                    if (currentRoute != "search") {
                        navController.navigate("search") {
                            popUpTo(
                                navController.graph.startDestinationId
                            ) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                })

            NavigationRailItem(
                icon = {
                    Icon(
                        Icons.Default.Settings, contentDescription = null
                    )
                },
                label = { Text("设置") },
                selected = currentRoute == "settings",
                onClick = {
                    if (currentRoute != "settings") {
                        navController.navigate("settings") {
                            popUpTo(
                                navController.graph.startDestinationId
                            ) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                })

            NavigationRailItem(
                icon = {
                    Icon(
                        Icons.Default.Person, contentDescription = null
                    )
                },
                label = { Text("个人") },
                selected = currentRoute == "profile",
                onClick = {
                    if (currentRoute != "profile") {
                        navController.navigate("profile") {
                            popUpTo(
                                navController.graph.startDestinationId
                            ) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                })
        }

        // 【右侧内容区域】直接使用 NavHost（带过渡动画）
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("小平板模式") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )

            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    getEnterTransition(isForward)
                },
                exitTransition = {
                    getExitTransition(isForward)
                },
                popEnterTransition = {
                    getEnterTransition(isForward)
                },
                popExitTransition = {
                    getExitTransition(isForward)
                }
            ) {
                composable(route = "home") { HomeContent(viewModel) }
                composable(route = "search") { SearchContent(viewModel) }
                composable(route = "settings") { SettingsContent(viewModel) }
                composable(route = "profile") { ProfileContent(viewModel) }
            }
        }
    }
}

// ========== 大平板布局：双栏布局（主从模式） ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedLayout(
    navController: NavHostController, viewModel: ShareViewModel
) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route ?: "home"

    // 跟踪导航方向
    var isForward by remember { mutableStateOf(true) }

    // 当前路由变化时更新导航方向
    LaunchedEffect(currentRoute) {
        isForward = getNavigationDirection(NavigationTracker.lastRoute, currentRoute)
        NavigationTracker.updateRoute(currentRoute)
    }

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // 【左侧：永久可见的导航抽屉】
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(
                    modifier = Modifier
                        .width(280.dp)
                        .shadow(
                            elevation = 10.dp,
                            ambientColor = Color.Gray,
                            spotColor = Color.DarkGray
                        )
                ) {
                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                Icons.Default.Home, contentDescription = null
                            )
                        },
                        label = { Text("首页") },
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                Icons.Default.Search, contentDescription = null
                            )
                        },
                        label = { Text("搜索") },
                        selected = currentRoute == "search",
                        onClick = {
                            if (currentRoute != "search") {
                                navController.navigate("search") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                Icons.Default.Settings, contentDescription = null
                            )
                        },
                        label = { Text("设置") },
                        selected = currentRoute == "settings",
                        onClick = {
                            if (currentRoute != "settings") {
                                navController.navigate("settings") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                Icons.Default.Person, contentDescription = null
                            )
                        },
                        label = { Text("个人中心") },
                        selected = currentRoute == "profile",
                        onClick = {
                            if (currentRoute != "profile") {
                                navController.navigate("profile") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }) {
            // 【中间和右侧：双栏内容区域】
            Row(modifier = Modifier.fillMaxSize()) {
                // 【主内容区域】占 60% 宽度
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                ) {
                    Column {
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = {
                                getEnterTransition(isForward)
                            },
                            exitTransition = {
                                getExitTransition(isForward)
                            },
                            popEnterTransition = {
                                getEnterTransition(isForward)
                            },
                            popExitTransition = {
                                getExitTransition(isForward)
                            }
                        ) {
                            composable(route = "home") { HomeContent(viewModel) }
                            composable(route = "search") { SearchContent(viewModel) }
                            composable(route = "settings") { SettingsContent(viewModel) }
                            composable(route = "profile") { ProfileContent(viewModel) }
                        }
                    }
                }
            }
        }
    }
}