package org.helo.mew.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.helo.mew.ui.theme.PastelBlue
import org.helo.mew.ui.theme.PastelPink
import org.helo.mew.ui.theme.PastelYellow
import org.helo.mew.viewmodel.DictationViewModel
import org.jetbrains.compose.resources.painterResource
import mew_hear.composeapp.generated.resources.Res
import mew_hear.composeapp.generated.resources.compose_multiplatform

/**
 * 应用首页界面
 */
@Composable
fun HomeScreen(
    viewModel: DictationViewModel,
    onNavigateToLessons: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 应用标题与吉祥物头像
        MascotHeader()

        Spacer(modifier = Modifier.height(24.dp))

        // 欢迎消息
        Text(
            "欢迎使用听写喵！",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "快乐听写，轻松学习！",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 功能卡片区域
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 开始听写卡片
            ElevatedCard(
                onClick = onNavigateToLessons,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PastelBlue.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "📚",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            "开始单课听写",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            "按照教材进度听写单词",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // 常错词复习卡片
            ElevatedCard(
                onClick = { viewModel.startErrorWordsDictation() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PastelPink.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "⭐",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            "常错词复习",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            "智能复习曾经错误的单词",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // 查看记录卡片
            ElevatedCard(
                onClick = { /* 导航到记录页面 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PastelYellow.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "📊",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            "听写记录",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            "查看历史听写记录与进步",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 应用吉祥物头像和应用名称
 */
@Composable
fun MascotHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 临时使用Compose图标代替吉祥物
        // 实际应用中应替换为喵小听的图像
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(PastelPink.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.compose_multiplatform),
                contentDescription = "喵小听",
                modifier = Modifier.size(80.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "听写喵",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
