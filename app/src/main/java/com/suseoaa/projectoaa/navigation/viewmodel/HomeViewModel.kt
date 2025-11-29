package com.suseoaa.projectoaa.navigation.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.suseoaa.projectoaa.common.network.LoliconApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// 1. 定义 UI 状态数据类
data class HomeUiState(
    val isCheckedIn: Boolean = false,
    val checkInCount: Int = 12, // 初始值
    val placeholderImageUrl: String? = null,
    val currentDate: String = "",
    val cspCountdown: String = "",
    val noipCountdown: String = ""
)

// 2. ViewModel
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // SharedPreferences 常量
    companion object {
        private const val PREFS_NAME = "check_in_prefs"
        private const val KEY_LAST_CHECK_IN = "last_check_in_date"
        private const val KEY_IMAGE_URL = "daily_image_url"
        private const val KEY_IMAGE_DATE = "daily_image_date"
    }

    var uiState by mutableStateOf(HomeUiState())
        private set

    private val loliconApi = LoliconApi.create()
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        loadDateInfo()
        checkIfAlreadyCheckedIn()
        fetchPlaceholderImage()
    }

    // --- 事件处理 (Event Handlers) ---

    @SuppressLint("NewApi")
    fun onCheckIn() {
        if (!uiState.isCheckedIn) {
            val todayString = LocalDate.now().toString()
            prefs.edit().putString(KEY_LAST_CHECK_IN, todayString).apply()
            uiState = uiState.copy(
                isCheckedIn = true,
                checkInCount = uiState.checkInCount + 1
            )
        }
    }

    // --- 私有逻辑 (Logic) ---

    @SuppressLint("NewApi")
    private fun checkIfAlreadyCheckedIn() {
        val todayString = LocalDate.now().toString()
        val storedDate = prefs.getString(KEY_LAST_CHECK_IN, null)
        if (todayString == storedDate) {
            uiState = uiState.copy(isCheckedIn = true)
        }
    }

    @SuppressLint("NewApi")
    private fun loadDateInfo() {
        val now = LocalDate.now()
        val targetCsp = LocalDate.of(2026, 10, 17)
        val targetNoip = LocalDate.of(2026, 11, 21)
        val dateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE)

        val cspDays = ChronoUnit.DAYS.between(now, targetCsp)
        val noipDays = ChronoUnit.DAYS.between(now, targetNoip)

        uiState = uiState.copy(
            currentDate = now.format(dateFormatter),
            cspCountdown = "距 CSP-J/S 2026 还剩 $cspDays 天",
            noipCountdown = "距 NOIP 2026 还剩 $noipDays 天"
        )
    }

    @SuppressLint("NewApi")
    private fun fetchPlaceholderImage() {
        val todayString = LocalDate.now().toString()
        val storedImageDate = prefs.getString(KEY_IMAGE_DATE, null)
        val storedImageUrl = prefs.getString(KEY_IMAGE_URL, null)

        // 1. 检查缓存：如果日期匹配，直接使用
        if (todayString == storedImageDate && storedImageUrl != null) {
            uiState = uiState.copy(placeholderImageUrl = storedImageUrl)
            return
        }

        // 2. 开始尝试获取
        viewModelScope.launch {
            Log.d("fetchPlaceholderImage", "尝试获取")
            val maxAttempts = 99999
            var currentAttempt = 0
            var delayTime = 1000L // 初始延迟 1 秒

            while (currentAttempt < maxAttempts) {
                try {
                    val response = loliconApi.getSetu(
                        r18 = 0,
                        excludeAI = true,
                        num = 1,
                        aspectRatio = "2",
                        size = "regular"
                    )
                    if (response.error.isNullOrEmpty() && !response.data.isNullOrEmpty()) {
                        val url = response.data[0].urls["regular"]
                            ?: response.data[0].urls["original"]

                        if (url != null) {
                            prefs.edit()
                                .putString(KEY_IMAGE_DATE, todayString)
                                .putString(KEY_IMAGE_URL, url)
                                .apply()
                            uiState = uiState.copy(placeholderImageUrl = url)
                            return@launch
                        }
                    }

                } catch (e: Exception) {
                    Log.e("fetchPlaceholderImage", "获取失败")
                    e.printStackTrace()
                }
                currentAttempt++
                if (currentAttempt < maxAttempts) {
                    Log.e("fetchPlaceholderImage", "获取失败，等待 ${delayTime/1000} 秒")
                    delay(delayTime)
                    if(delayTime<=10000L) delayTime *= 2
                }
            }
        }
    }
}