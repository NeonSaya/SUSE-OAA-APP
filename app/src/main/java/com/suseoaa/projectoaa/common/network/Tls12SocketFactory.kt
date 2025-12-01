package com.suseoaa.projectoaa.common.network

import android.util.Log
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * 委托 SSLSocketFactory，用于在支持的设备上强制启用 TLSv1.2
 */
internal class Tls12SocketFactory(
    private val delegate: SSLSocketFactory
) : SSLSocketFactory() {

    // 仅启用 TLSv1.2 协议
    private val TLS_V1_2_ONLY = arrayOf("TLSv1.2")

    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites
    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return patch(delegate.createSocket(s, host, port, autoClose))
    }

    override fun createSocket(host: String, port: Int): Socket {
        return patch(delegate.createSocket(host, port))
    }

    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        return patch(delegate.createSocket(host, port, localHost, localPort))
    }

    override fun createSocket(host: InetAddress, port: Int): Socket {
        return patch(delegate.createSocket(host, port))
    }

    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return patch(delegate.createSocket(address, port, localAddress, localPort))
    }

    /**
     * 核心逻辑：获取 socket 并启用 TSLv1.2
     */
    private fun patch(s: Socket): Socket {
        if (s is SSLSocket) {
            try {
                s.enabledProtocols = TLS_V1_2_ONLY
            } catch (e: Exception) {
                // 某些设备可能在设置时失败
                Log.e("Tls12SocketFactory", "无法在 Socket 上设置 TLSv1.2", e)
            }
        }
        return s
    }
}