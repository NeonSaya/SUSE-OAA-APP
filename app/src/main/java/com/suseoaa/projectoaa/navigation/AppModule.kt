package com.suseoaa.projectoaa.navigation

import android.content.Context
import android.content.SharedPreferences
import com.suseoaa.projectoaa.common.util.SessionManager
import com.suseoaa.projectoaa.navigation.repository.*
import com.suseoaa.projectoaa.navigation.repository.detail.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 模块 - 用于 @Provides
 * 职责：提供 Hilt 无法自动构造的类的实例 (如: SharedPreferences)
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供应用的 SharedPreferences 单例
     */
    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE)
        //
    }

    /**
     * [修复] 移除了 provideKotlinxJson()
     * 该功能已由 NetworkModule 中的 provideMoshi() 代替
     */
    //

    /**
     * [修复] 移除了 provideRetrofit()
     * Retrofit 实例现在由 NetworkModule 统一提供
     */
    //

    /**
     * [修复] 移除了 provideDetailApiService()
     * 所有 ApiService 实例都应在 NetworkModule 中提供，以保持一致性
     */
    //
}

/**
 * Hilt 模块 - 用于 @Binds
 * 职责：将存储库 (Repository) 的接口 (Interface) 绑定到其具体实现 (Implementation)。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingModule {

    /**
     * 绑定详情页仓库 [开发开关]
     * [当前]: FakeDetailRepository (返回随机数据)
     * [未来]: 切换到 RealDetailRepository (对接后端)
     */
    @Binds
    @Singleton
    abstract fun bindDetailRepository(
        impl: FakeDetailRepository
    ): DetailRepository

    /*
    @Binds
    @Singleton
    abstract fun bindDetailRepository(
        impl: RealDetailRepository
    ): DetailRepository
    */ //

    @Singleton
    @Binds
    abstract fun bindUserDataRepository(
        impl: UserDataRepositoryImpl
    ): UserDataRepository

    @Singleton
    @Binds
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Singleton
    @Binds
    abstract fun bindFeedbackRepository(
        impl: FeedbackRepositoryImpl
    ): FeedbackRepository

    @Singleton
    @Binds
    abstract fun bindImageRepository(
        impl: ImageRepositoryImpl
    ): ImageRepository

    @Singleton
    @Binds
    abstract fun bindWallpaperRepository(
        impl: WallpaperRepositoryImpl
    ): WallpaperRepository
}