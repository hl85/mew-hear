package org.helo.mew.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform