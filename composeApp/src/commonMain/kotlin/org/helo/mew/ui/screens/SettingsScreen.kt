package org.helo.mew.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.helo.mew.model.Grade
import org.helo.mew.model.TextbookVersion
import org.helo.mew.model.UserSettings
import org.helo.mew.viewmodel.DictationViewModel

/**
 * 设置界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DictationViewModel) {
    val userSettings by viewModel.userSettings.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // 本地状态，用于跟踪设置变更
    var textbookVersion by remember { mutableStateOf(TextbookVersion.PEOPLE_EDUCATION) }
    var grade by remember { mutableStateOf(Grade.K1) }
    var enableAutoPlay by remember { mutableStateOf(true) }
    var dictationInterval by remember { mutableStateOf(3) }

    // 当用户设置加载时，更新本地状态
    LaunchedEffect(userSettings) {
        userSettings?.let {
            textbookVersion = it.preferredTextbookVersion
            grade = it.preferredGrade
            enableAutoPlay = it.enableAudioAutoPlay
            dictationInterval = it.dictationInterval
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // 标题
        Text(
            "设置",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 设置卡片
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "学习设置",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 教材版本选择
                Text(
                    "教材版本",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextbookVersionSelection(
                    selectedVersion = textbookVersion,
                    onVersionSelected = { textbookVersion = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 年级选择
                Text(
                    "年级",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                GradeSelection(
                    selectedGrade = grade,
                    onGradeSelected = { grade = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 听写设置卡片
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "听写设置",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 自动播放开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "自动播放音频",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Switch(
                        checked = enableAutoPlay,
                        onCheckedChange = { enableAutoPlay = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 听写间隔调整
                Text(
                    "听写间隔（秒）：$dictationInterval",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = dictationInterval.toFloat(),
                    onValueChange = { dictationInterval = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 保存按钮
        Button(
            onClick = {
                val updatedSettings = UserSettings(
                    userId = userSettings?.userId ?: "user1",
                    preferredTextbookVersion = textbookVersion,
                    preferredGrade = grade,
                    enableAudioAutoPlay = enableAutoPlay,
                    dictationInterval = dictationInterval
                )
                scope.launch {
                    viewModel.updateSettings(updatedSettings)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("保存设置")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 应用信息
        Text(
            "听写喵 v1.0.0\n© 2025 MewHear",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * 教材版本选择组件
 */
@Composable
fun TextbookVersionSelection(
    selectedVersion: TextbookVersion,
    onVersionSelected: (TextbookVersion) -> Unit
) {
    val versions = remember {
        listOf(
            TextbookVersion.PEOPLE_EDUCATION,
            TextbookVersion.BEIJING_NORMAL_UNIVERSITY,
            TextbookVersion.BEIJING,
            TextbookVersion.SHANGHAI,
            TextbookVersion.CUSTOM
        )
    }

    Column {
        versions.forEach { version ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedVersion == version,
                    onClick = { onVersionSelected(version) }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = getVersionDisplayName(version),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * 年级选择组件
 */
@Composable
fun GradeSelection(
    selectedGrade: Grade,
    onGradeSelected: (Grade) -> Unit
) {
    val grades = remember {
        listOf(
            Grade.K1,
            Grade.K2,
            Grade.K3,
            Grade.K4,
            Grade.K5,
            Grade.K6,
            Grade.CUSTOM
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        grades.take(6).forEach { grade ->
            OutlinedButton(
                onClick = { onGradeSelected(grade) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedGrade == grade) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text(getGradeDisplayName(grade))
            }
        }
    }
}

/**
 * 辅助函数：获取教材版本显示名称
 */
fun getVersionDisplayName(version: TextbookVersion): String {
    return when (version) {
        TextbookVersion.PEOPLE_EDUCATION -> "人教版"
        TextbookVersion.BEIJING_NORMAL_UNIVERSITY -> "北师大版"
        TextbookVersion.BEIJING -> "北京版"
        TextbookVersion.SHANGHAI -> "沪教版"
        TextbookVersion.CUSTOM -> "自定义/手工录入"
    }
}

/**
 * 辅助函数：获取年级显示名称
 */
fun getGradeDisplayName(grade: Grade): String {
    return when (grade) {
        Grade.K1 -> "一年级"
        Grade.K2 -> "二年级"
        Grade.K3 -> "三年级"
        Grade.K4 -> "四年级"
        Grade.K5 -> "五年级"
        Grade.K6 -> "六年级"
        Grade.CUSTOM -> "自定义"
    }
}
