package com.mewhear.app.platform

import com.mewhear.app.AppContext
import java.io.IOException

// Actual function to read a file from Android assets
internal actual fun loadJsonFromResources(fileName: String): String? {
    return try {
        AppContext.context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        e.printStackTrace() // Log error
        null
    }
}
