package org.helo.mew.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 定义听写喵应用的主题颜色
val PastelPink = Color(0xFFF8BBD0) // 柔和的粉色
val PastelBlue = Color(0xFFBBDEF8) // 柔和的蓝色
val PastelYellow = Color(0xFFF8E8BB) // 柔和的黄色
val CreamWhite = Color(0xFFFFFBF5) // 奶油白色
val LightGray = Color(0xFFEEEEEE) // 浅灰色

// 黑暗模式颜色
val DarkPink = Color(0xFFD48FB1) // 深粉色
val DarkBlue = Color(0xFF7BAFD4) // 深蓝色
val DarkYellow = Color(0xFFD4B87B) // 深黄色
val DarkBackground = Color(0xFF212121) // 深黑色背景
val DarkGray = Color(0xFF424242) // 深灰色

// 浅色主题配色方案
private val LightColorScheme = lightColorScheme(
    primary = PastelPink,
    secondary = PastelBlue,
    tertiary = PastelYellow,
    background = CreamWhite,
    surface = Color.White,
    onPrimary = Color.DarkGray,
    onSecondary = Color.DarkGray,
    onTertiary = Color.DarkGray,
    onBackground = Color.DarkGray,
    onSurface = Color.DarkGray
)

// 深色主题配色方案
private val DarkColorScheme = darkColorScheme(
    primary = DarkPink,
    secondary = DarkBlue,
    tertiary = DarkYellow,
    background = DarkBackground,
    surface = DarkGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

// 定义圆角形状
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun MewHearTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        typography = Typography,
        content = content
    )
}
