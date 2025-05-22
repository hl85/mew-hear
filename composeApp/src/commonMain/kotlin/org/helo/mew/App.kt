package org.helo.mew

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mew_hear.composeapp.generated.resources.Res
import mew_hear.composeapp.generated.resources.compose_multiplatform
import org.helo.mew.model.Grade
import org.helo.mew.model.Subject
import org.helo.mew.model.TextbookVersion
import org.helo.mew.repository.DictationRepository
import org.helo.mew.repository.InMemoryDictationRepository
import org.helo.mew.ui.screens.*
import org.helo.mew.ui.theme.MewHearTheme
import org.helo.mew.ui.theme.PastelPink
import org.helo.mew.viewmodel.DictationViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// 定义应用程序的主要导航目标
enum class AppScreen {
    Home,
    Lessons,
    ErrorWords,
    Records,
    Settings
}

@Composable
@Preview
fun App() {
    // 创建仓库和ViewModel (对于MVP版本，我们使用内存实现)
    val repository: DictationRepository = remember { InMemoryDictationRepository() }
    val viewModel = remember { DictationViewModel(repository, FakeAudioService()) }
    
    // 初始化ViewModel
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    // 当前选中的屏幕
    var currentScreen by remember { mutableStateOf(AppScreen.Home) }

    MewHearTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Home,
                        onClick = { currentScreen = AppScreen.Home },
                        icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                        label = { Text("首页") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Lessons,
                        onClick = { currentScreen = AppScreen.Lessons },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "课程") },
                        label = { Text("课程") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.ErrorWords,
                        onClick = { currentScreen = AppScreen.ErrorWords },
                        icon = { Icon(Icons.Default.Star, contentDescription = "常错词") },
                        label = { Text("常错词") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Records,
                        onClick = { currentScreen = AppScreen.Records },
                        icon = { Icon(Icons.Default.History, contentDescription = "记录") },
                        label = { Text("记录") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Settings,
                        onClick = { currentScreen = AppScreen.Settings },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                        label = { Text("设置") }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.Home -> HomeScreen(viewModel) {
                        currentScreen = AppScreen.Lessons
                    }
                    AppScreen.Lessons -> LessonsScreen(viewModel)
                    AppScreen.ErrorWords -> ErrorWordsScreen(viewModel)
                    AppScreen.Records -> RecordsScreen(viewModel)
                    AppScreen.Settings -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

// MVP版本的临时伪音频服务，实际应用中应该实现真正的音频播放功能
class FakeAudioService : org.helo.mew.service.AudioService {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    override fun playAudio(audioPath: String, onComplete: () -> Unit) {
        println("播放音频: $audioPath")
        // 模拟音频播放完成的延迟
        scope.launch {
            delay(2000)
            onComplete()
        }
    }

    override fun stopAudio() {
        println("停止音频")
    }

    override fun setVolume(volume: Float) {
        println("设置音量: $volume")
    }

    override fun isPlaying(): Boolean {
        return false
    }
}
