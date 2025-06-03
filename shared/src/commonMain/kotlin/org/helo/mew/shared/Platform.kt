package org.helo.mew

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform