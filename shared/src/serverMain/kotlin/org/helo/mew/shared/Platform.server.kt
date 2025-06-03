package org.helo.mew

import io.ktor.server.engine.ApplicationEngine

/**
 * Ktor 服务器平台实现
 */
class ServerPlatform : Platform {
    override val name: String = "Ktor Netty Server"
    
    // 提供额外的服务器信息
    val engineType: String = "Netty"
    val serverInfo: String = "Ktor ${getKtorVersion()} on $engineType"
    
    /**
     * 获取 Ktor 版本信息（如果可用）
     */
    private fun getKtorVersion(): String {
        return try {
            val version = ApplicationEngine::class.java.`package`.implementationVersion ?: "Unknown"
            version
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

actual fun getPlatform(): Platform = ServerPlatform()
