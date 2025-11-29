package com.suseoaa.projectoaa.navigation.ui

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // 导入 Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.suseoaa.projectoaa.common.navigation.AppRoutes

// ==========================================
// 1. 首页内容 (HomeContent)
// ==========================================
@Composable
fun HomeContent(
    // HomeViewModel 状态
    isCheckedIn: Boolean,
    checkInCount: Int,
    placeholderImageUrl: String?,
    currentDate: String,
    cspCountdown: String,
    noipCountdown: String,
    onCheckIn: () -> Unit, // HomeViewModel 事件
    // ShareViewModel 状态
    currentThemeName: String,
    onRefreshWallpaper: (Context) -> Unit,
    onSaveWallpaper: (Context) -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    // --- [新增逻辑] ---
    // 1. 检查是否为旧版 Android 主题
    val isLegacyTheme = currentThemeName.contains("Android 4.0") || currentThemeName.contains("Android 2.3")

    // 2. 根据主题定义颜色
    // 如果是旧版主题，强制所有文本/图标颜色为白色或灰色，否则使用主题默认值
    val primaryColor = if (isLegacyTheme) Color.White else MaterialTheme.colorScheme.primary
    val onSurfaceColor = if (isLegacyTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = if (isLegacyTheme) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
    val secondaryColor = if (isLegacyTheme) Color.White else MaterialTheme.colorScheme.secondary
    val tertiaryColor = if (isLegacyTheme) Color.White else MaterialTheme.colorScheme.tertiary
    val outlineColor = if (isLegacyTheme) Color.Gray else MaterialTheme.colorScheme.outline
    // --- [修改结束] ---


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 头部欢迎 ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "欢迎回来，Project OAA",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor // [修改]
                    )
                    Text(
                        text = "今天也是充满活力的一天！",
                        style = MaterialTheme.typography.bodyLarge,
                        color = onSurfaceVariantColor // [修改]
                    )
                }

                if (currentThemeName.contains("二次元")) {
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "更多选项", tint = primaryColor) // [修改]
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                RoundedCornerShape(12.dp)
                            )
                        ) {
                            DropdownMenuItem(
                                text = { Text("刷新壁纸") },
                                onClick = { onRefreshWallpaper(context); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Refresh, null) })
                            DropdownMenuItem(
                                text = { Text("保存壁纸") },
                                onClick = { onSaveWallpaper(context); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Download, null) })
                        }
                    }
                }
            }
        }

        // --- 打卡区域 ---
        item {
            CheckInCard(
                isCheckedIn = isCheckedIn,
                checkInCount = checkInCount,
                placeholderImageUrl = placeholderImageUrl,
                currentDate = currentDate,
                cspCountdown = cspCountdown,
                noipCountdown = noipCountdown,
                onCheckIn = onCheckIn,
                // [新增] 传入颜色
                primaryColor = primaryColor,
                onSurfaceColor = onSurfaceColor,
                onSurfaceVariantColor = onSurfaceVariantColor
            )
        }

        // --- 协会公告 ---
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, null, tint = primaryColor) // [修改]
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "协会公告",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor // [修改]
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "🎉 2025年春季招新活动即将开始，请各位干事做好准备！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceColor // [修改]
                )
            }
        }

        // --- 快捷功能 ---
        item {
            Text(
                "快捷功能",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                color = onSurfaceColor // [修改]
            )
        }

        // --- 课表查询入口 ---
        item {
            AppCard(onClick = { navController.navigate(AppRoutes.CourseList.route) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        AppRoutes.CourseList.icon,
                        null,
                        tint = secondaryColor, // [修改]
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        AppRoutes.CourseList.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor // [修改]
                    )
                }
            }
        }

        // --- 招新报名入口 ---
        item {
            AppCard(onClick = { navController.navigate(AppRoutes.StudentForm.route) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        AppRoutes.StudentForm.icon,
                        null,
                        tint = tertiaryColor, // [修改]
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        AppRoutes.StudentForm.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor // [修改]
                    )
                }
            }
        }

        // --- 待办事项 ---
        item {
            Text(
                "待办事项",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                color = onSurfaceColor // [修改]
            )
        }

        items(
            items = (0..2).toList(),
            key = { "task_item_$it" }
        ) { index ->
            // [修改] 传入颜色
            TaskItem(
                index = index,
                onSurfaceColor = onSurfaceColor,
                secondaryColor = secondaryColor,
                outlineColor = outlineColor
            )
        }

        item { Spacer(modifier = Modifier.height(60.dp)) } // 底部留白
    }
}

// ==========================================
// 2. 打卡卡片 (CheckInCard)
// ==========================================
@Composable
private fun CheckInCard(
    isCheckedIn: Boolean,
    checkInCount: Int,
    placeholderImageUrl: String?,
    currentDate: String,
    cspCountdown: String,
    noipCountdown: String,
    onCheckIn: () -> Unit,
    // [新增] 接收颜色
    primaryColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- 1. 背景图片 ---
            AsyncImage(
                model = placeholderImageUrl,
                contentDescription = "打卡占位图",
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            )

            // --- 2. 覆盖层 ---
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // === 2.1 顶部动画区域 (运势图) ===
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // 权重应用于Box
                ) {
                    // [改动] 使用 Crossfade 替换 AnimatedVisibility
                    Crossfade(
                        targetState = isCheckedIn,
                        animationSpec = tween(durationMillis = 400),
                        modifier = Modifier.fillMaxSize(),
                        label = "FortuneContentCrossfade"
                    ) { isChecked ->
                        if (isChecked) {
                            FortuneContent(
                                checkInCount,
                                cspCountdown,
                                noipCountdown,
                                // [新增] 传入颜色
                                primaryColor = primaryColor,
                                onSurfaceColor = onSurfaceColor,
                                onSurfaceVariantColor = onSurfaceVariantColor
                            )
                        } else {
                            // Crossfade 需要一个 "else" 块来进行淡入淡出
                            // 我们可以使用一个空的 Box 作为占位符
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }


                // === 2.2 底部信息区域 ===
                Box(modifier = Modifier.fillMaxWidth()) {
                    Crossfade(
                        targetState = isCheckedIn,
                        animationSpec = tween(durationMillis = 400),
                        label = "CheckInInfoCrossfade"
                    ) { isChecked ->
                        if (isChecked) {
                            AfterCheckInInfo()
                        } else {
                            BeforeCheckInInfo(
                                currentDate = currentDate,
                                cspCountdown = cspCountdown,
                                noipCountdown = noipCountdown,
                                onCheckIn = onCheckIn,
                                // [新增] 传入颜色
                                onSurfaceColor = onSurfaceColor,
                                onSurfaceVariantColor = onSurfaceVariantColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 顶部内容 - 打卡后 (运势)
 */
@Composable
private fun FortuneContent(
    checkInCount: Int,
    cspCountdown: String,
    noipCountdown: String,
    // [新增] 接收颜色
    primaryColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // 蒙层使用 surface
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(20.dp),
    ) {
        // --- 顶部行：中吉 + 打卡天数/倒计时 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                "中吉",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor // [修改]
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "打卡天数",
                    fontSize = 14.sp,
                    color = onSurfaceVariantColor // [修改]
                )
                Text(
                    text = "$checkInCount",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor, // [修改]
                    lineHeight = 40.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    cspCountdown,
                    color = onSurfaceVariantColor, // [修改]
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    noipCountdown,
                    color = onSurfaceVariantColor, // [修改]
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- 底部行：宜/忌 分栏 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FortuneHeader(isGood = true)
                FortuneItem(title = "打东方", subtitle = "All clear !", onSurfaceColor, onSurfaceVariantColor) // [修改]
                FortuneItem(title = "请教问题", subtitle = "获得解答", onSurfaceColor, onSurfaceVariantColor) // [修改]
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FortuneHeader(isGood = false)
                FortuneItem(title = "写作文", subtitle = "不知所云", onSurfaceColor, onSurfaceVariantColor) // [修改]
                FortuneItem(title = "写晨读", subtitle = "第一一年", onSurfaceColor, onSurfaceVariantColor) // [修改]
            }
        }
    }
}

/**
 * 宜/忌 标题头
 */
@Composable
private fun FortuneHeader(isGood: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (isGood) "宜" else "忌",
            // [注意] 这些颜色来自 Container，是反色的，不需要修改
            color = if (isGood) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(
                    if (isGood) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    CircleShape
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}


/**
 * 运势子项
 */
@Composable
private fun FortuneItem(
    title: String,
    subtitle: String,
    onSurfaceColor: Color, // [新增]
    onSurfaceVariantColor: Color // [新增]
) {
    Column {
        Text(title, color = onSurfaceColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) // [修改]
        Text(subtitle, color = onSurfaceVariantColor, fontSize = 12.sp) // [修改]
    }
}


/**
 * 底部内容 - 打卡前
 */
@Composable
private fun BeforeCheckInInfo(
    currentDate: String,
    cspCountdown: String,
    noipCountdown: String,
    onCheckIn: () -> Unit,
    // [新增]
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(currentDate, color = onSurfaceColor, style = MaterialTheme.typography.titleMedium) // [修改]
        Spacer(Modifier.height(8.dp))
        Text(cspCountdown, color = onSurfaceVariantColor, style = MaterialTheme.typography.bodyMedium) // [修改]
        Text(noipCountdown, color = onSurfaceVariantColor, style = MaterialTheme.typography.bodyMedium) // [修改]
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onCheckIn,
            shape = RoundedCornerShape(8.dp),
            // [注意] 按钮颜色使用 primary，它会自动使用 onPrimary (白色) 作为文字颜色，不需要修改
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("打卡")
        }
    }
}

/**
 * 底部内容 - 打卡后 (仅分享按钮)
 */
@Composable
private fun AfterCheckInInfo() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Button(
            onClick = { /* TODO: 分享逻辑 */ },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("分享")
        }
    }
}


// ==========================================
// 3. 其他卡片 (TaskItem)
// ==========================================

/**
 * 待办事项卡片
 */
@Composable
private fun TaskItem(
    index: Int,
    // [新增]
    onSurfaceColor: Color,
    secondaryColor: Color,
    outlineColor: Color
) {
    AppCard { // 这里的 AppCard 会使用 CommonUIComponents.kt 中的定义
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Assignment, null, tint = secondaryColor, modifier = Modifier.size(32.dp)) // [修改]
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "协会事务处理事项 #${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor // [修改]
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "截止日期: 2025-12-31",
                    style = MaterialTheme.typography.bodySmall,
                    color = outlineColor // [修改]
                )
            }
        }
    }
}