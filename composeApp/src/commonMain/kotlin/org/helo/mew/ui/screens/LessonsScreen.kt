package org.helo.mew.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.helo.mew.model.*
import org.helo.mew.ui.theme.PastelBlue
import org.helo.mew.ui.theme.PastelPink
import org.helo.mew.viewmodel.DictationViewModel

/**
 * 课程列表界面视图状态
 */
enum class LessonViewState {
    GRADES, // 年级选择
    SUBJECTS, // 学科选择
    UNITS, // 单元选择
    LESSONS // 课程选择
}

/**
 * 课程列表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(viewModel: DictationViewModel) {
    val scope = rememberCoroutineScope()
    val userSettings by viewModel.userSettings.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    
    // 如果听写会话已开始，显示听写界面
    if (sessionState is DictationSessionState.Ready || 
        sessionState is DictationSessionState.InProgress) {
        DictationScreen(viewModel)
        return
    }
    
    // 如果听写会话已完成，显示结果界面
    if (sessionState is DictationSessionState.Completed) {
        DictationResultScreen(viewModel) {
            scope.launch {
                viewModel.resetSession()
            }
        }
        return
    }
    
    // 当前显示的视图状态
    var currentView by remember { mutableStateOf(LessonViewState.SUBJECTS) }
    var selectedGrade by remember { mutableStateOf<Grade?>(userSettings.preferredGrade) }
    var selectedSubject by remember { mutableStateOf<Subject?>(userSettings.preferredSubject) }
    var selectedUnit by remember { mutableStateOf<ClassUnit?>(null) }
    
    // 模拟数据 (在MVP版本中，我们使用硬编码数据)
    val subjects = remember { listOf(Subject.ENGLISH, Subject.CHINESE) }
    val grades = remember { Grade.values().toList() }
    
    // 模拟单元数据
    val units = remember {
        listOf(
            ClassUnit("u1", "Unit 1: Everyday English", emptyList(), Grade.K1, Subject.ENGLISH),
            ClassUnit("u2", "Unit 2: Animals & Plants", emptyList(), Grade.K1, Subject.ENGLISH),
            ClassUnit("u3", "单元1: 日常用语", emptyList(), Grade.K1, Subject.CHINESE),
            ClassUnit("u4", "单元2: 自然现象", emptyList(), Grade.K1, Subject.CHINESE)
        )
    }
    
    // 模拟课程数据
    val lessons = remember {
        mapOf(
            "u1" to listOf(
                Lesson("l1", "Lesson 1: Greetings", getEnglishWords1(), "u1"),
                Lesson("l2", "Lesson 2: School Life", getEnglishWords2(), "u1")
            ),
            "u2" to listOf(
                Lesson("l3", "Lesson 1: Animals", getEnglishWords3(), "u2")
            ),
            "u3" to listOf(
                Lesson("l4", "第1课: 问候语", getChineseWords1(), "u3")
            ),
            "u4" to listOf(
                Lesson("l5", "第2课: 学校生活", getChineseWords2(), "u4")
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (currentView) {
                            LessonViewState.GRADES -> "选择年级"
                            LessonViewState.SUBJECTS -> "选择学科"
                            LessonViewState.UNITS -> "${selectedSubject?.displayName ?: ""} 单元"
                            LessonViewState.LESSONS -> "${selectedUnit?.name ?: ""} 课程"
                        }
                    ) 
                },
                navigationIcon = if (currentView != LessonViewState.SUBJECTS) {
                    {
                        IconButton(onClick = {
                            when (currentView) {
                                LessonViewState.GRADES -> currentView = LessonViewState.SUBJECTS
                                LessonViewState.UNITS -> currentView = LessonViewState.SUBJECTS
                                LessonViewState.LESSONS -> currentView = LessonViewState.UNITS
                                else -> {} // 不会发生
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                } else null
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (currentView) {
                LessonViewState.GRADES -> {
                    GradesList(
                        grades = grades, 
                        selectedGrade = selectedGrade,
                        onGradeSelected = { grade ->
                            selectedGrade = grade
                            currentView = LessonViewState.SUBJECTS
                        }
                    )
                }
                LessonViewState.SUBJECTS -> {
                    SubjectsList(
                        subjects = subjects,
                        onSubjectSelected = { subject ->
                            selectedSubject = subject
                            currentView = LessonViewState.UNITS
                        }
                    )
                }
                LessonViewState.UNITS -> {
                    if (selectedSubject != null) {
                        UnitsList(
                            units = units.filter { 
                                it.subject == selectedSubject && 
                                (selectedGrade == null || it.grade == selectedGrade) 
                            },
                            onUnitSelected = { unit ->
                                selectedUnit = unit
                                currentView = LessonViewState.LESSONS
                            }
                        )
                    }
                }
                LessonViewState.LESSONS -> {
                    if (selectedUnit != null) {
                        val unitLessons = lessons[selectedUnit!!.id] ?: emptyList()
                        LessonsList(
                            lessons = unitLessons,
                            onLessonSelected = { lesson ->
                                scope.launch {
                                    // 开始听写会话
                                    viewModel.prepareSession(
                                        lesson.words, 
                                        lesson.id, 
                                        lesson.name
                                    )
                                    viewModel.startSession()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectsList(
    subjects: List<Subject>,
    onSubjectSelected: (Subject) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(subjects) { subject ->
            SubjectItem(subject = subject, onClick = { onSubjectSelected(subject) })
        }
    }
}

@Composable
private fun SubjectItem(
    subject: Subject,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = subject.displayName,
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(PastelBlue.copy(alpha = 0.2f))
                    .padding(8.dp),
                tint = PastelBlue
            )
            
            Text(
                text = subject.displayName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "进入",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun GradesList(
    grades: List<Grade>,
    selectedGrade: Grade?,
    onGradeSelected: (Grade) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(grades) { grade ->
            GradeItem(
                grade = grade, 
                isSelected = grade == selectedGrade,
                onClick = { onGradeSelected(grade) }
            )
        }
    }
}

@Composable
private fun GradeItem(
    grade: Grade,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = grade.displayName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun UnitsList(
    units: List<ClassUnit>,
    onUnitSelected: (ClassUnit) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(units) { unit ->
            UnitItem(unit = unit, onClick = { onUnitSelected(unit) })
        }
    }
}

@Composable
private fun UnitItem(
    unit: ClassUnit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = unit.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "进入单元",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun LessonsList(
    lessons: List<Lesson>,
    onLessonSelected: (Lesson) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(lessons) { lesson ->
            LessonItem(
                lesson = lesson,
                wordCount = lesson.words.size,
                onClick = { onLessonSelected(lesson) }
            )
        }
    }
}

@Composable
private fun LessonItem(
    lesson: Lesson,
    wordCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = lesson.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "$wordCount 个词汇",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Button(onClick = onClick) {
                Text("开始听写")
            }
        }
    }
}

// 模拟数据函数
private fun getEnglishWords1(): List<Word> {
    return listOf(
        Word("w1", "Hello", "audio/hello.mp3", "你好", listOf("Hello, how are you?")),
        Word("w2", "Goodbye", "audio/goodbye.mp3", "再见", listOf("Goodbye, see you tomorrow.")),
        Word("w3", "Thank you", "audio/thankyou.mp3", "谢谢", listOf("Thank you for your help."))
    )
}

private fun getEnglishWords2(): List<Word> {
    return listOf(
        Word("w4", "School", "audio/school.mp3", "学校", listOf("I go to school every day.")),
        Word("w5", "Teacher", "audio/teacher.mp3", "老师", listOf("My teacher is very kind."))
    )
}

private fun getEnglishWords3(): List<Word> {
    return listOf(
        Word("w6", "Cat", "audio/cat.mp3", "猫", listOf("The cat is sleeping.")),
        Word("w7", "Dog", "audio/dog.mp3", "狗", listOf("The dog is barking.")),
        Word("w8", "Bird", "audio/bird.mp3", "鸟", listOf("The bird is singing."))
    )
}

private fun getChineseWords1(): List<Word> {
    return listOf(
        Word("w9", "你好", "audio/nihao.mp3", "Hello", listOf("你好，早上好！")),
        Word("w10", "谢谢", "audio/xiexie.mp3", "Thank you", listOf("非常谢谢你的帮助。"))
    )
}

private fun getChineseWords2(): List<Word> {
    return listOf(
        Word("w11", "学校", "audio/xuexiao.mp3", "School", listOf("我每天去学校。")),
        Word("w12", "老师", "audio/laoshi.mp3", "Teacher", listOf("我的老师很好。")),
        Word("w13", "同学", "audio/tongxue.mp3", "Classmate", listOf("他是我的同学。"))
    )
}

// 听写界面
@Composable
fun DictationScreen(viewModel: DictationViewModel) {
    // 简单的听写界面原型
    val sessionState by viewModel.sessionState.collectAsState()
    val currentWord by viewModel.currentWord.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题
        Text(
            "听写进行中",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 进度信息
        if (sessionState is DictationSessionState.InProgress) {
            val progress = sessionState as DictationSessionState.InProgress
            LinearProgressIndicator(
                progress = { progress.currentWordIndex.toFloat() / progress.totalWords },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "${progress.currentWordIndex}/${progress.totalWords}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 当前单词显示（实际应用中只播放声音）
        if (currentWord != null) {
            Text(
                "请听写：${currentWord?.content}",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!currentWord?.pronunciation.isNullOrBlank()) {
                Text(
                    "发音：${currentWord?.pronunciation}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.replayCurrentWord() }) {
                Text("重播")
            }
            
            Button(onClick = { viewModel.nextWord() }) {
                Text("下一个")
            }
        }
    }
}

// 听写结果界面
@Composable
fun DictationResultScreen(viewModel: DictationViewModel, onClose: () -> Unit) {
    val dictationResult by viewModel.dictationResult.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题
        Text(
            "听写完成！",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 结果卡片
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 正确率
                Text(
                    "正确率：${(dictationResult?.correctRate ?: 0f) * 100}%",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 用时
                Text(
                    "用时：${dictationResult?.duration ?: 0} 秒",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 返回按钮
                Button(onClick = onClose) {
                    Text("返回")
                }
            }
        }
    }
}

// Subject扩展属性
val Subject.displayName: String
    get() = when (this) {
        Subject.ENGLISH -> "英语"
        Subject.CHINESE -> "语文"
    }

// Grade扩展属性
val Grade.displayName: String
    get() = when (this) {
        Grade.K1 -> "一年级"
        Grade.K2 -> "二年级"
        Grade.K3 -> "三年级"
        Grade.K4 -> "四年级"
        Grade.K5 -> "五年级"
        Grade.K6 -> "六年级"
        Grade.CUSTOM -> "自定义"
    }
