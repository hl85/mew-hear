# 听写喵 App 数据结构设计文档

## 1. 概述

本文档定义了听写喵应用的核心数据结构，基于Kotlin Multiplatform架构，支持Android和iOS平台。数据存储采用PocketBase作为后端服务。

## 2. 数据模型设计

### 2.1 用户相关数据

#### 2.1.1 用户信息 (User)
```kotlin
@Serializable
data class User(
    val id: String,
    val username: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val grade: Grade,
    val textbookVersions: Map<Subject, TextbookVersion>,
    val createdAt: String,
    val updatedAt: String
)

enum class Grade {
    K1, K2, K3, K4, K5, K6
}

enum class Subject {
    ENGLISH, CHINESE
}

enum class TextbookVersion {
    // 英语教材版本
    PEP_ENGLISH,        // 人教版
    BEIJING_NORMAL,     // 北师大版
    BEIJING_EDITION,    // 北京版
    SHANGHAI_EDITION,   // 沪教版
    
    // 语文教材版本
    PEP_CHINESE,        // 人教版
    BEIJING_NORMAL_CHINESE, // 北师大版
    BEIJING_EDITION_CHINESE, // 北京版
    SHANGHAI_EDITION_CHINESE // 沪教版
}
```

#### 2.1.2 用户设置 (UserSettings)
```kotlin
@Serializable
data class UserSettings(
    val userId: String,
    val audioSpeed: Float = 1.0f,        // 音频播放速度
    val audioVolume: Float = 1.0f,       // 音频音量
    val repeatCount: Int = 2,             // 默认重复次数
    val autoPlayNext: Boolean = true,     // 自动播放下一个
    val showPinyin: Boolean = true,       // 是否显示拼音（中文）
    val showPhonetics: Boolean = true,    // 是否显示音标（英文）
    val darkMode: Boolean = false,        // 深色模式
    val hapticFeedback: Boolean = true    // 触觉反馈
)
```

### 2.2 教材内容数据

#### 2.2.1 教材 (Textbook)
```kotlin
@Serializable
data class Textbook(
    val id: String,
    val name: String,
    val subject: Subject,
    val version: TextbookVersion,
    val grade: Grade,
    val coverImage: String? = null,
    val description: String? = null,
    val createdAt: String,
    val updatedAt: String
)
```

#### 2.2.2 单元 (Unit)
```kotlin
@Serializable
data class Unit(
    val id: String,
    val textbookId: String,
    val name: String,
    val unitNumber: Int,
    val description: String? = null,
    val coverImage: String? = null,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String
)
```

#### 2.2.3 课程 (Lesson)
```kotlin
@Serializable
data class Lesson(
    val id: String,
    val unitId: String,
    val name: String,
    val lessonNumber: Int,
    val description: String? = null,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String
)
```

#### 2.2.4 单词/词汇 (Word)
```kotlin
@Serializable
data class Word(
    val id: String,
    val lessonId: String? = null,        // 可为空，用于练习册词汇
    val text: String,                    // 单词/词汇文本
    val pronunciation: String? = null,    // 发音（拼音或音标）
    val meaning: String? = null,         // 含义
    val audioUrl: String? = null,        // 音频文件URL
    val audioLocalPath: String? = null,  // 本地音频路径
    val difficulty: WordDifficulty = WordDifficulty.NORMAL,
    val tags: List<String> = emptyList(), // 标签
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String
)

enum class WordDifficulty {
    EASY, NORMAL, HARD
}
```

### 2.3 练习册和词汇库

#### 2.3.1 练习册 (Workbook)
```kotlin
@Serializable
data class Workbook(
    val id: String,
    val name: String,
    val description: String? = null,
    val subject: Subject,
    val grade: Grade? = null,            // 可为空，表示通用
    val type: WorkbookType,
    val coverImage: String? = null,
    val isPublic: Boolean = true,        // 是否公开
    val createdBy: String? = null,       // 创建者ID
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String
)

enum class WorkbookType {
    MANUAL_INPUT,     // 手工录入
    COMMON_MISTAKES,  // 常错词汇库
    REVIEW,          // 复习册
    CUSTOM          // 自定义
}
```

#### 2.3.2 练习册词汇关联 (WorkbookWord)
```kotlin
@Serializable
data class WorkbookWord(
    val id: String,
    val workbookId: String,
    val wordId: String,
    val sortOrder: Int,
    val addedAt: String
)
```

### 2.4 听写记录数据

#### 2.4.1 听写会话 (DictationSession)
```kotlin
@Serializable
data class DictationSession(
    val id: String,
    val userId: String,
    val type: DictationSessionType,
    val sourceId: String,                // 课程ID、练习册ID等
    val sourceName: String,              // 来源名称
    val totalWords: Int,
    val correctWords: Int,
    val accuracy: Float,                 // 正确率
    val totalTime: Long,                 // 总耗时（毫秒）
    val averageTimePerWord: Long,        // 平均每词耗时
    val startedAt: String,
    val completedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

enum class DictationSessionType {
    LESSON,          // 单课听写
    WORKBOOK,        // 练习册听写
    COMMON_MISTAKES, // 常错词听写
    REVIEW          // 复习听写
}
```

#### 2.4.2 单词听写记录 (WordDictationRecord)
```kotlin
@Serializable
data class WordDictationRecord(
    val id: String,
    val sessionId: String,
    val wordId: String,
    val wordText: String,
    val isCorrect: Boolean,
    val timeSpent: Long,                 // 耗时（毫秒）
    val attempts: Int = 1,               // 尝试次数
    val mistakeType: MistakeType? = null, // 错误类型
    val recordedAt: String
)

enum class MistakeType {
    SPELLING,        // 拼写错误
    PRONUNCIATION,   // 发音理解错误
    UNFAMILIAR,     // 不熟悉
    CARELESS        // 粗心
}
```

#### 2.4.3 常错词记录 (CommonMistakeWord)
```kotlin
@Serializable
data class CommonMistakeWord(
    val id: String,
    val userId: String,
    val wordId: String,
    val mistakeCount: Int,               // 错误次数
    val totalAttempts: Int,              // 总尝试次数
    val lastMistakeAt: String,           // 最后错误时间
    val nextReviewAt: String,            // 下次复习时间（基于艾宾浩斯曲线）
    val difficultyLevel: Int = 1,        // 难度等级（1-5）
    val isResolved: Boolean = false,     // 是否已解决
    val createdAt: String,
    val updatedAt: String
)
```

### 2.5 艾宾浩斯复习数据

#### 2.5.1 复习计划 (ReviewSchedule)
```kotlin
@Serializable
data class ReviewSchedule(
    val id: String,
    val userId: String,
    val wordId: String,
    val currentInterval: Int,            // 当前间隔天数
    val easinessFactor: Float = 2.5f,    // 容易因子
    val repetitions: Int = 0,            // 重复次数
    val nextReviewDate: String,          // 下次复习日期
    val quality: Int? = null,            // 上次复习质量(0-5)
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)
```

## 3. 数据关系图

```
User (用户)
├── UserSettings (用户设置)
├── DictationSession (听写会话)
├── CommonMistakeWord (常错词)
└── ReviewSchedule (复习计划)

Textbook (教材)
├── Unit (单元)
│   └── Lesson (课程)
│       └── Word (单词)

Workbook (练习册)
└── WorkbookWord (练习册词汇关联)
    └── Word (单词)

DictationSession (听写会话)
└── WordDictationRecord (单词听写记录)
    └── Word (单词)
```

## 4. 数据库表结构设计（PocketBase）

### 4.1 集合定义

#### users 集合
- id (text, primary)
- username (text, unique)
- nickname (text, optional)
- avatar (file, optional)
- grade (select: K1,K2,K3,K4,K5,K6)
- textbook_versions (json)
- created (datetime)
- updated (datetime)

#### user_settings 集合
- id (text, primary)
- user_id (relation: users)
- audio_speed (number, default: 1.0)
- audio_volume (number, default: 1.0)
- repeat_count (number, default: 2)
- auto_play_next (bool, default: true)
- show_pinyin (bool, default: true)
- show_phonetics (bool, default: true)
- dark_mode (bool, default: false)
- haptic_feedback (bool, default: true)
- created (datetime)
- updated (datetime)

#### textbooks 集合
- id (text, primary)
- name (text)
- subject (select: ENGLISH,CHINESE)
- version (select: 教材版本枚举)
- grade (select: K1,K2,K3,K4,K5,K6)
- cover_image (file, optional)
- description (text, optional)
- created (datetime)
- updated (datetime)

#### units 集合
- id (text, primary)
- textbook_id (relation: textbooks)
- name (text)
- unit_number (number)
- description (text, optional)
- cover_image (file, optional)
- sort_order (number)
- created (datetime)
- updated (datetime)

#### lessons 集合
- id (text, primary)
- unit_id (relation: units)
- name (text)
- lesson_number (number)
- description (text, optional)
- sort_order (number)
- created (datetime)
- updated (datetime)

#### words 集合
- id (text, primary)
- lesson_id (relation: lessons, optional)
- text (text)
- pronunciation (text, optional)
- meaning (text, optional)
- audio_url (url, optional)
- audio_local_path (text, optional)
- difficulty (select: EASY,NORMAL,HARD)
- tags (json)
- sort_order (number)
- created (datetime)
- updated (datetime)

#### workbooks 集合
- id (text, primary)
- name (text)
- description (text, optional)
- subject (select: ENGLISH,CHINESE)
- grade (select: K1,K2,K3,K4,K5,K6, optional)
- type (select: MANUAL_INPUT,COMMON_MISTAKES,REVIEW,CUSTOM)
- cover_image (file, optional)
- is_public (bool, default: true)
- created_by (relation: users, optional)
- sort_order (number)
- created (datetime)
- updated (datetime)

#### workbook_words 集合
- id (text, primary)
- workbook_id (relation: workbooks)
- word_id (relation: words)
- sort_order (number)
- added_at (datetime)

#### dictation_sessions 集合
- id (text, primary)
- user_id (relation: users)
- type (select: LESSON,WORKBOOK,COMMON_MISTAKES,REVIEW)
- source_id (text)
- source_name (text)
- total_words (number)
- correct_words (number)
- accuracy (number)
- total_time (number)
- average_time_per_word (number)
- started_at (datetime)
- completed_at (datetime, optional)
- created (datetime)
- updated (datetime)

#### word_dictation_records 集合
- id (text, primary)
- session_id (relation: dictation_sessions)
- word_id (relation: words)
- word_text (text)
- is_correct (bool)
- time_spent (number)
- attempts (number, default: 1)
- mistake_type (select: SPELLING,PRONUNCIATION,UNFAMILIAR,CARELESS, optional)
- recorded_at (datetime)

#### common_mistake_words 集合
- id (text, primary)
- user_id (relation: users)
- word_id (relation: words)
- mistake_count (number)
- total_attempts (number)
- last_mistake_at (datetime)
- next_review_at (datetime)
- difficulty_level (number, default: 1)
- is_resolved (bool, default: false)
- created (datetime)
- updated (datetime)

#### review_schedules 集合
- id (text, primary)
- user_id (relation: users)
- word_id (relation: words)
- current_interval (number)
- easiness_factor (number, default: 2.5)
- repetitions (number, default: 0)
- next_review_date (datetime)
- quality (number, optional)
- is_active (bool, default: true)
- created (datetime)
- updated (datetime)

## 5. 索引设计

为了优化查询性能，建议创建以下索引：

```sql
-- 用户相关查询
CREATE INDEX idx_user_settings_user_id ON user_settings(user_id);

-- 教材内容查询
CREATE INDEX idx_units_textbook_id ON units(textbook_id);
CREATE INDEX idx_lessons_unit_id ON lessons(unit_id);
CREATE INDEX idx_words_lesson_id ON words(lesson_id);
CREATE INDEX idx_textbooks_subject_grade ON textbooks(subject, grade);

-- 练习册查询
CREATE INDEX idx_workbook_words_workbook_id ON workbook_words(workbook_id);
CREATE INDEX idx_workbook_words_word_id ON workbook_words(word_id);
CREATE INDEX idx_workbooks_subject_grade ON workbooks(subject, grade);

-- 听写记录查询
CREATE INDEX idx_dictation_sessions_user_id ON dictation_sessions(user_id);
CREATE INDEX idx_dictation_sessions_type ON dictation_sessions(type);
CREATE INDEX idx_word_dictation_records_session_id ON word_dictation_records(session_id);
CREATE INDEX idx_word_dictation_records_word_id ON word_dictation_records(word_id);

-- 常错词和复习计划查询
CREATE INDEX idx_common_mistake_words_user_id ON common_mistake_words(user_id);
CREATE INDEX idx_common_mistake_words_next_review ON common_mistake_words(next_review_at);
CREATE INDEX idx_review_schedules_user_id ON review_schedules(user_id);
CREATE INDEX idx_review_schedules_next_review ON review_schedules(next_review_date);
```

## 6. 数据验证规则

### 6.1 字段验证
- 用户名：3-20个字符，字母数字下划线
- 年级：只能是K1-K6
- 正确率：0-1之间的浮点数
- 音频速度：0.5-2.0之间
- 难度等级：1-5之间的整数

### 6.2 业务规则
- 一个用户在同一时间只能有一个进行中的听写会话
- 单词必须属于某个课程或练习册
- 听写记录必须关联到有效的听写会话
- 复习计划的下次复习时间不能早于当前时间

## 7. 数据迁移和版本控制

### 7.1 版本管理
- 使用语义化版本控制（如：v1.0.0）
- 每个数据结构变更都需要版本升级
- 保持向后兼容性

### 7.2 迁移策略
- 新字段添加时设置合理默认值
- 字段类型变更需要数据转换脚本
- 删除字段前先标记为废弃，下个大版本再删除

## 8. 性能优化建议

### 8.1 数据分页
- 课程列表：每页20条
- 单词列表：每页50条
- 听写记录：每页30条

### 8.2 缓存策略
- 教材内容数据：本地缓存7天
- 用户设置：本地持久化存储
- 音频文件：本地缓存30天

### 8.3 数据同步
- 增量同步用户生成的数据
- 定期全量同步基础教材数据
- 离线模式支持本地数据操作

## 9. 安全考虑

### 9.1 数据隐私
- 用户个人信息加密存储
- 听写记录数据匿名化处理
- 遵循COPPA儿童隐私保护法规

### 9.2 访问控制
- 用户只能访问自己的数据
- 教材内容按权限分级
- API接口实现权限验证

### 9.3 数据备份
- 每日自动备份用户数据
- 支持数据导出功能
- 意外删除数据恢复机制 