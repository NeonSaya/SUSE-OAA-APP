package com.suseoaa.projectoaa.common.network

import android.os.Build
import android.util.Log
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * TLS 兼容性工具
 * 目标：在 Android API 19 (KitKat) 到 API 23 (Marshmallow) 上强制启用 TLSv1.2
 */
object TlsCompatUtil {

    private const val TAG = "TlsCompatUtil"

    fun applyTls12Compat(builder: OkHttpClient.Builder) {
        // 仅在 Android 4.4 (API 19) 到 Android 6.0 (API 23) 上应用
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.KITKAT..Build.VERSION_CODES.M) {
            try {
                // 1. 获取系统的默认 TrustManager
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers = trustManagerFactory.trustManagers
                if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                    throw IllegalStateException("Unexpected default trust managers:" + trustManagers.contentToString())
                }
                val trustManager = trustManagers[0] as X509TrustManager

                // 2. 创建一个强制使用 TLSv1.2 的 SSLContext
                val sslContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(null, arrayOf<TrustManager>(trustManager), null)

                // 3. 使用 Tls12SocketFactory 包装类
                val socketFactory = Tls12SocketFactory(sslContext.socketFactory)

                // 4. 将 SocketFactory 和 TrustManager 应用到 OkHttp Builder
                builder.sslSocketFactory(socketFactory, trustManager)

                // 5. 明确指定连接规范，强制使用 TLS 1.2
                val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build()

                builder.connectionSpecs(listOf(cs, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))

                Log.i(TAG, "TLSv1.2 兼容性补丁已应用 (API ${Build.VERSION.SDK_INT})")

            } catch (e: Exception) {
                Log.e(TAG, "应用 TLSv1.2 兼容性补丁时出错", e)
            }
        }
    }
}