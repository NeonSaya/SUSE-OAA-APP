package com.suseoaa.projectoaa.navigation.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suseoaa.projectoaa.common.theme.OaaThemeConfig
import com.suseoaa.projectoaa.common.theme.ThemeManager
import com.suseoaa.projectoaa.common.util.SessionManager
import com.suseoaa.projectoaa.common.util.WallpaperManager
import com.suseoaa.projectoaa.navigation.repository.FeedbackRepository
import com.suseoaa.projectoaa.navigation.repository.SettingsRepository
import com.suseoaa.projectoaa.navigation.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val feedbackRepository: FeedbackRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // ==========================================
    // 1. 全局状态流
    // ==========================================

    /**
     * 全局壁纸流
     */
    val appWallpaper = wallpaperRepository.currentWallpaper
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * 壁纸遮罩透明度
     */
    val wallpaperAlpha = WallpaperManager.wallpaperAlpha

    /**
     * 一次性事件流 (Toast)
     */
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    // 用户会话信息
    val currentUser: String?
        get() = sessionManager.currentUser
    val currentRole: String?
        get() = sessionManager.currentRole

    // ==========================================
    // 2. 主题控制
    // ==========================================

    var currentTheme by mutableStateOf(ThemeManager.currentTheme)
        private set

    fun updateTheme(themeConfig: OaaThemeConfig) {
        if (currentTheme.name == themeConfig.name) return

        ThemeManager.currentTheme = themeConfig
        currentTheme = themeConfig

        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.saveThemeName(themeConfig.name)
            if (themeConfig.name.contains("二次元") && appWallpaper.value == null) {
                wallpaperRepository.refreshWallpaper()
            }
        }
    }

    // ==========================================
    // 3. 设置相关
    // ==========================================

    var notificationEnabled by mutableStateOf(true)
        private set

    fun onNotificationToggleChanged(isEnabled: Boolean) {
        notificationEnabled = isEnabled
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.saveNotificationEnabled(isEnabled)
        }
    }

    var privacyEnabled by mutableStateOf(false)
        private set

    fun onPrivacyToggleChanged(isEnabled: Boolean) {
        privacyEnabled = isEnabled
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.savePrivacyEnabled(isEnabled)
        }
    }

    // ==========================================
    // 4. 反馈与杂项
    // ==========================================

    var feedbackText by mutableStateOf("")
        private set
    var isSubmittingFeedback by mutableStateOf(false)
        private set

    fun onFeedbackTextChanged(text: String) {
        feedbackText = text
    }

    fun submitFeedback() {
        if (feedbackText.isBlank()) {
            viewModelScope.launch { _toastEvent.emit("反馈内容不能为空") }
            return
        }
        isSubmittingFeedback = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isSuccess = feedbackRepository.submit(feedbackText)
                withContext(Dispatchers.Main) {
                    isSubmittingFeedback = false
                    if (isSuccess) {
                        feedbackText = ""
                        _toastEvent.emit("感谢您的反馈！")
                    } else {
                        _toastEvent.emit("提交失败，请重试")
                    }
                }
            } catch (e: Exception) {
                Log.e("ShareViewModel", "Feedback error", e)
                withContext(Dispatchers.Main) {
                    isSubmittingFeedback = false
                    _toastEvent.emit("网络错误，请稍后重试")
                }
            }
        }
    }

    private val _showOfState = MutableStateFlow(false)
    val showOfState: StateFlow<Boolean> = _showOfState
    fun toggleOfUI() { _showOfState.value = !_showOfState.value }

    // ========================================================
    // 5. 壁纸操作委托 ( [修复] 解耦权限逻辑 )
    // ========================================================

    /**
     * [新增] 用于从 UI 触发权限请求的事件流
     */
    private val _requestSavePermissionEvent = MutableSharedFlow<Unit>()
    val requestSavePermissionEvent = _requestSavePermissionEvent.asSharedFlow()

    fun updateWallpaperAlpha(alpha: Float) {
        // 调用 Manager 的静态方法，Manager 内部处理持久化逻辑
        WallpaperManager.setWallpaperAlpha(context, alpha)
    }

    /**
     * [修改] 由 UI (HomeScreen) 调用，仅用于发送“请求保存”事件
     */
    fun onSaveWallpaper() {
        viewModelScope.launch {
            _requestSavePermissionEvent.emit(Unit)
        }
    }

    /**
     * [新增] 由 PermissionDialogs.kt 在权限通过后调用
     */
    fun executeSaveWallpaper(context: Context) {
        // context 参数是为匹配 PermissionDialogs 协定而保留的
        // 实际调用仓库方法，它内部处理 IO 线程和 Toast
        viewModelScope.launch {
            wallpaperRepository.saveCurrentToGallery()
        }
    }

    fun onRefreshWallpaper() {
        viewModelScope.launch {
            // Manager 内部已处理 IO 线程和 Toast
            wallpaperRepository.refreshWallpaper()
        }
    }

    // ==========================================
    // 6. 初始化
    // ==========================================

    init {
        loadAllSettings()
    }

    private fun loadAllSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedThemeName = settingsRepository.getThemeName()
            val savedTheme = ThemeManager.themeList.find { it.name == savedThemeName }
                ?: ThemeManager.themeList.first()
            val notif = settingsRepository.getNotificationEnabled()
            val privacy = settingsRepository.getPrivacyEnabled()

            withContext(Dispatchers.Main) {
                ThemeManager.currentTheme = savedTheme
                currentTheme = savedTheme
                notificationEnabled = notif
                privacyEnabled = privacy
            }
        }
    }
}