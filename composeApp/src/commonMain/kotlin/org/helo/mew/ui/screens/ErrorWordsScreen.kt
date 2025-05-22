package org.helo.mew.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.helo.mew.model.DictationSessionState
import org.helo.mew.model.Word
import org.helo.mew.ui.theme.PastelPink
import org.helo.mew.viewmodel.DictationViewModel

/**
 * 常错词界面
 */
@Composable
fun ErrorWordsScreen(viewModel: DictationViewModel) {
    val errorWords by viewModel.errorWords.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val scope = rememberCoroutineScope()
    
    // 如果听写会话已开始，显示听写界面
    if (sessionState is DictationSessionState.Ready || 
        sessionState is DictationSessionState.InProgress) {
        DictationSessionScreen(viewModel)
        return
    }
    
    // 如果听写会话已完成，显示结果界面
    if (sessionState is DictationSessionState.Completed) {
        DictationResultScreen(viewModel) {
            // 重置会话状态
            scope.launch {
                viewModel.resetSession()
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            "常错词复习",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 说明文字
        Text(
            "基于艾宾浩斯遗忘曲线，智能安排复习计划，帮助你高效记忆！",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 开始复习按钮
        Button(
            onClick = { viewModel.startErrorWordsDictation() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("开始复习")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 常错词列表
        if (errorWords.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "目前没有常错词记录\n完成一些听写练习后再来吧！",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(errorWords) { word ->
                    ErrorWordItem(word = word, viewModel = viewModel)
                }
            }
        }
    }
}

/**
 * 常错词项目组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorWordItem(word: Word, viewModel: DictationViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 单词图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PastelPink.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    word.content.first().uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 单词信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    word.content,
                    style = MaterialTheme.typography.headlineSmall
                )
                
                if (word.translation.isNotEmpty()) {
                    Text(
                        word.translation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (word.pronunciation.isNotEmpty()) {
                    Text(
                        word.pronunciation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // 播放按钮
            IconButton(onClick = { viewModel.playCurrentWordAudio() }) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "播放发音"
                )
            }
        }
    }
}

/**
 * 听写会话界面 - 占位实现，实际应该定义在单独的文件中
 */
@Composable
fun DictationSessionScreen(viewModel: DictationViewModel) {
    // 会话状态
    val sessionState by viewModel.sessionState.collectAsState()
    val inProgressState = sessionState as? DictationSessionState.InProgress
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 只是一个简单的实现
        Text(
            text = "听写进行中...",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (inProgressState != null) {
            Text(
                text = "进度: ${inProgressState.currentWordIndex + 1}/${inProgressState.totalWords}"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = inProgressState.userInputs[inProgressState.currentWordIndex],
                onValueChange = { viewModel.updateUserInput(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("请输入听到的内容") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { viewModel.playCurrentWordAudio() }
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "播放")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("再听一次")
                }
                
                Button(
                    onClick = { viewModel.moveToNext() }
                ) {
                    Text("下一题")
                }
            }
        }
    }
}

/**
 * 听写结果界面 - 占位实现
 */
@Composable
fun DictationResultScreen(
    viewModel: DictationViewModel,
    onRestart: () -> Unit
) {
    // 会话状态
    val sessionState by viewModel.sessionState.collectAsState()
    val completedState = sessionState as? DictationSessionState.Completed
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (completedState != null) {
            // 结果标题
            Text(
                text = "听写完成!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 得分
            val score = (completedState.correctCount.toFloat() / completedState.words.size * 100).toInt()
            Text(
                text = "得分: $score",
                style = MaterialTheme.typography.titleLarge
            )
            
            Text(
                text = "正确: ${completedState.correctCount}/${completedState.words.size}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 重新开始按钮
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "重新开始")
                Spacer(modifier = Modifier.width(8.dp))
                Text("返回")
            }
        }
    }
}
