package org.helo.mew.shared

/**
 * JVM 平台实现
 */
class JVMPlatform : Platform {
    override val name: String = "JVM ${getJvmVersion()}"
    
    // 提供额外的平台信息
    val javaVersion: String = System.getProperty("java.version")
    val javaVendor: String = System.getProperty("java.vendor")
    val osName: String = System.getProperty("os.name")
    val osVersion: String = System.getProperty("os.version")
    
    /**
     * 获取 JVM 版本信息
     */
    private fun getJvmVersion(): String {
        return try {
            System.getProperty("java.version")
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

actual fun getPlatform(): Platform = JVMPlatform()
