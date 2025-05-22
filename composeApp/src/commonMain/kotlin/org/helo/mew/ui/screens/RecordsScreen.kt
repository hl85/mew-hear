package org.helo.mew.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.helo.mew.model.DictationRecord
import org.helo.mew.viewmodel.DictationViewModel

/**
 * 听写记录界面
 */
@Composable
fun RecordsScreen(viewModel: DictationViewModel) {
    val dictationHistory by viewModel.dictationHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            "听写记录",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 统计信息卡片
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    title = "总记录数",
                    value = dictationHistory.size.toString()
                )
                
                StatisticItem(
                    title = "平均正确率",
                    value = "${calculateAverageCorrectRate(dictationHistory)}%"
                )
                
                StatisticItem(
                    title = "平均用时",
                    value = "${calculateAverageTime(dictationHistory)}秒"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 记录列表
        if (dictationHistory.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "暂无听写记录\n开始一次听写来创建记录吧！",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            Text(
                "历史记录",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dictationHistory) { record ->
                    RecordItem(record = record)
                }
            }
        }
    }
}

/**
 * 统计项组件
 */
@Composable
fun StatisticItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

/**
 * 听写记录项组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordItem(record: DictationRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 日期图标
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 记录信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "课程：${record.lessonId}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    "时间：${formatDateTime(record.startTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // 成绩信息
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${(record.correctRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = getColorForCorrectRate(record.correctRate)
                )
                
                Text(
                    "用时: ${record.duration}秒",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha =