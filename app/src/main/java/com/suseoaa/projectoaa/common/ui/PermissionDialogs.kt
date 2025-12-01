package com.suseoaa.projectoaa.common.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.suseoaa.projectoaa.navigation.viewmodel.ShareViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * 全局副作用 Composable：
 * 处理保存壁纸时的存储权限请求。
 *
 * 监听 ShareViewModel 的 'requestSavePermissionEvent' 事件。
 */
@Composable
fun HandleSaveWallpaperPermission(
    viewModel: ShareViewModel = hiltViewModel<ShareViewModel>()
) {
    val context = LocalContext.current

    // 1. 注册权限请求回调
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // 权限已授予，执行保存
                viewModel.executeSaveWallpaper(context)
            } else {
                // 权限被拒绝
                Toast.makeText(context, "未授予存储权限，无法保存", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // 2. 监听 ViewModel 的保存事件
    LaunchedEffect(key1 = viewModel, key2 = context) {
        viewModel.requestSavePermissionEvent.onEach {
            // Android 10 (API 29) 及以上，使用分区存储，无需权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                viewModel.executeSaveWallpaper(context)
            }
            // Android 9 (API 28) 及以下，需要检查权限
            else {
                when {
                    // 1. 权限已授予
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        viewModel.executeSaveWallpaper(context)
                    }

                    // 2. 需要显示权限理由
                    (context as? Activity)?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } == true -> {
                        // 用 Toast 显示理由，然后再次请求
                        Toast.makeText(context, "需要存储权限才能保存壁纸", Toast.LENGTH_LONG).show()
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }

                    // 3. 直接请求权限
                    else -> {
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            }
        }.launchIn(this) // 'this' 是 LaunchedEffect 提供的 CoroutineScope
    }
}