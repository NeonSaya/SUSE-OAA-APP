package com.suseoaa.projectoaa.common.network

import com.suseoaa.projectoaa.common.util.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.suseoaa.projectoaa.BuildConfig
import com.suseoaa.projectoaa.login.api.ApiService as LoginApiService
import com.suseoaa.projectoaa.student.network.ApiService as StudentApiService
import com.suseoaa.projectoaa.common.network.TlsCompatUtil
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = BuildConfig.API_BASE_URL

    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // 确保 Moshi 可以处理 Kotlin 类
        .build()

    @Singleton
    @Provides
    fun provideAuthInterceptor(sessionManager: SessionManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val path = originalRequest.url.encodedPath

            if (path.contains("/user/login") || path.contains("/user/register")) {
                return@Interceptor chain.proceed(originalRequest)
            }

            val token = sessionManager.jwtToken
            if (!token.isNullOrBlank()) {
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@Interceptor chain.proceed(newRequest)
            }
            chain.proceed(originalRequest)
        }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        // --- 应用 TLS 1.2 兼容性补丁 ---
        TlsCompatUtil.applyTls12Compat(builder)

        return builder.build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        // [修复] 此函数必须存在并使用 Moshi
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // 正确使用 Moshi
            .build()
    }

    //提供用于登录/个人资料的 ApiService
    @Singleton
    @Provides
    fun provideLoginApiService(retrofit: Retrofit): LoginApiService {
        return retrofit.create(LoginApiService::class.java)
    }

    // 提供用于学生表单的 ApiService
    @Singleton
    @Provides
    fun provideStudentApiService(retrofit: Retrofit): StudentApiService {
        return retrofit.create(StudentApiService::class.java)
    }
}