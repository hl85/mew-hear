# 听写喵 App 数据结构技术规范

## 1. 架构概述

本技术规范基于Clean Architecture和MVVM模式，采用Kotlin Multiplatform架构，确保代码在Android和iOS平台间的最大化共享。

### 1.1 分层架构

```
┌─────────────────────────────────────┐
│            UI Layer                 │  ← Platform Specific
│   (Android: Compose / iOS: SwiftUI) │
├─────────────────────────────────────┤
│         Presentation Layer          │  ← Shared
│        (ViewModels & State)         │
├─────────────────────────────────────┤
│          Domain Layer               │  ← Shared
│       (Use Cases & Entities)        │
├─────────────────────────────────────┤
│           Data Layer                │  ← Shared
│    (Repositories & Data Sources)    │
├─────────────────────────────────────┤
│        Platform Layer               │  ← Platform Specific
│     (Local DB & Network Stack)      │
└─────────────────────────────────────┘
```

### 1.2 模块结构

```
shared/
├── src/
│   ├── commonMain/kotlin/
│   │   ├── domain/
│   │   │   ├── entities/        # 核心业务实体
│   │   │   ├── repositories/    # 仓库接口
│   │   │   └── usecases/       # 业务用例
│   │   ├── data/
│   │   │   ├── models/         # 数据传输对象
│   │   │   ├── repositories/   # 仓库实现
│   │   │   ├── datasources/    # 数据源接口
│   │   │   └── mappers/        # 数据映射器
│   │   └── presentation/
│   │       ├── viewmodels/     # 视图模型
│   │       └── states/         # UI状态
│   ├── androidMain/kotlin/     # Android特定实现
│   └── iosMain/kotlin/         # iOS特定实现
└── build.gradle.kts
```

## 2. 数据实体规范

### 2.1 命名约定

- **实体类**：使用PascalCase，以业务含义命名（如：`User`, `DictationSession`）
- **属性**：使用camelCase，描述性命名（如：`createdAt`, `totalWords`）
- **枚举**：使用PascalCase类名，SCREAMING_SNAKE_CASE值（如：`Grade.K1`）
- **常量**：使用SCREAMING_SNAKE_CASE（如：`MAX_RETRY_COUNT`）

### 2.2 数据类设计原则

#### 2.2.1 不可变性（Immutability）
```kotlin
@Serializable
data class User(
    val id: String,
    val username: String,
    val grade: Grade,
    val createdAt: String
) {
    // 提供copy方法进行数据更新
    fun updateGrade(newGrade: Grade): User = copy(grade = newGrade)
}
```

#### 2.2.2 验证和约束
```kotlin
@Serializable
data class UserSettings(
    val userId: String,
    val audioSpeed: Float = 1.0f,
    val audioVolume: Float = 1.0f,
    val repeatCount: Int = 2
) {
    init {
        require(audioSpeed in 0.5f..2.0f) { "音频速度必须在0.5-2.0之间" }
        require(audioVolume in 0.0f..1.0f) { "音量必须在0.0-1.0之间" }
        require(repeatCount in 1..5) { "重复次数必须在1-5之间" }
        require(userId.isNotBlank()) { "用户ID不能为空" }
    }
}
```

#### 2.2.3 序列化配置
```kotlin
@Serializable
data class Word(
    val id: String,
    @SerialName("lesson_id")
    val lessonId: String? = null,
    val text: String,
    val pronunciation: String? = null,
    @SerialName("audio_url")
    val audioUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
```

### 2.3 枚举设计规范

#### 2.3.1 基础枚举
```kotlin
enum class Grade(val displayName: String, val level: Int) {
    K1("一年级", 1),
    K2("二年级", 2),
    K3("三年级", 3),
    K4("四年级", 4),
    K5("五年级", 5),
    K6("六年级", 6);
    
    companion object {
        fun fromLevel(level: Int): Grade? = values().find { it.level == level }
        fun fromDisplayName(name: String): Grade? = values().find { it.displayName == name }
    }
}
```

#### 2.3.2 可扩展枚举
```kotlin
enum class TextbookVersion(
    val displayName: String,
    val code: String,
    val subject: Subject
) {
    // 英语教材
    PEP_ENGLISH("人教版英语", "PEP_EN", Subject.ENGLISH),
    BEIJING_NORMAL_ENGLISH("北师大版英语", "BNU_EN", Subject.ENGLISH),
    BEIJING_EDITION_ENGLISH("北京版英语", "BJ_EN", Subject.ENGLISH),
    SHANGHAI_EDITION_ENGLISH("沪教版英语", "SH_EN", Subject.ENGLISH),
    
    // 语文教材
    PEP_CHINESE("人教版语文", "PEP_CN", Subject.CHINESE),
    BEIJING_NORMAL_CHINESE("北师大版语文", "BNU_CN", Subject.CHINESE),
    BEIJING_EDITION_CHINESE("北京版语文", "BJ_CN", Subject.CHINESE),
    SHANGHAI_EDITION_CHINESE("沪教版语文", "SH_CN", Subject.CHINESE);
    
    companion object {
        fun bySubject(subject: Subject): List<TextbookVersion> = 
            values().filter { it.subject == subject }
            
        fun fromCode(code: String): TextbookVersion? = 
            values().find { it.code == code }
    }
}
```

## 3. Repository模式实现

### 3.1 Repository接口设计

```kotlin
interface UserRepository {
    suspend fun getCurrentUser(): Result<User?>
    suspend fun updateUser(user: User): Result<User>
    suspend fun getUserSettings(userId: String): Result<UserSettings>
    suspend fun updateUserSettings(settings: UserSettings): Result<UserSettings>
    suspend fun deleteUser(userId: String): Result<Unit>
}
```

### 3.2 Repository实现规范

```kotlin
class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource,
    private val networkMonitor: NetworkMonitor
) : UserRepository {
    
    override suspend fun getCurrentUser(): Result<User?> = try {
        when {
            networkMonitor.isConnected -> {
                val remoteUser = remoteDataSource.getCurrentUser()
                remoteUser.onSuccess { user ->
                    user?.let { localDataSource.saveUser(it) }
                }
                remoteUser
            }
            else -> localDataSource.getCurrentUser()
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun updateUser(user: User): Result<User> = try {
        // 先更新本地
        localDataSource.saveUser(user)
        
        // 再同步到远程
        if (networkMonitor.isConnected) {
            remoteDataSource.updateUser(user)
        } else {
            // 离线时加入同步队列
            localDataSource.addToSyncQueue(user)
            Result.success(user)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### 3.3 数据源接口设计

```kotlin
interface UserRemoteDataSource {
    suspend fun getCurrentUser(): Result<User?>
    suspend fun updateUser(user: User): Result<User>
    suspend fun createUser(user: User): Result<User>
    suspend fun deleteUser(userId: String): Result<Unit>
}

interface UserLocalDataSource {
    suspend fun getCurrentUser(): Result<User?>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun addToSyncQueue(user: User): Result<Unit>
    suspend fun getSyncQueue(): Result<List<User>>
}
```

## 4. 数据映射规范

### 4.1 Mapper接口设计

```kotlin
interface DataMapper<Entity, Model> {
    fun mapToEntity(model: Model): Entity
    fun mapToModel(entity: Entity): Model
    fun mapToEntityList(models: List<Model>): List<Entity> = 
        models.map { mapToEntity(it) }
    fun mapToModelList(entities: List<Entity>): List<Model> = 
        entities.map { mapToModel(it) }
}
```

### 4.2 具体Mapper实现

```kotlin
object UserMapper : DataMapper<User, UserDto> {
    override fun mapToEntity(model: UserDto): User = User(
        id = model.id,
        username = model.username,
        nickname = model.nickname,
        avatar = model.avatar,
        grade = Grade.valueOf(model.grade),
        textbookVersions = model.textbookVersions.mapValues { 
            TextbookVersion.valueOf(it.value) 
        },
        createdAt = model.createdAt,
        updatedAt = model.updatedAt
    )
    
    override fun mapToModel(entity: User): UserDto = UserDto(
        id = entity.id,
        username = entity.username,
        nickname = entity.nickname,
        avatar = entity.avatar,
        grade = entity.grade.name,
        textbookVersions = entity.textbookVersions.mapValues { it.value.name },
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )
}
```

## 5. 错误处理规范

### 5.1 异常层次结构

```kotlin
sealed class MewHearException(message: String, cause: Throwable? = null) : 
    Exception(message, cause) {
    
    class NetworkException(message: String, cause: Throwable? = null) : 
        MewHearException(message, cause)
    
    class DatabaseException(message: String, cause: Throwable? = null) : 
        MewHearException(message, cause)
    
    class ValidationException(message: String) : 
        MewHearException(message)
    
    class AuthenticationException(message: String) : 
        MewHearException(message)
    
    class BusinessLogicException(message: String) : 
        MewHearException(message)
}
```

### 5.2 Result类型使用

```kotlin
// 推荐使用Kotlin的Result类型进行错误处理
suspend fun createDictationSession(
    userId: String, 
    sourceId: String, 
    type: DictationSessionType
): Result<DictationSession> = runCatching {
    validateInput(userId, sourceId)
    
    val session = DictationSession(
        id = UUID.randomUUID().toString(),
        userId = userId,
        type = type,
        sourceId = sourceId,
        // ... 其他属性
    )
    
    repository.saveDictationSession(session).getOrThrow()
}.onFailure { exception ->
    logger.error("创建听写会话失败", exception)
}

private fun validateInput(userId: String, sourceId: String) {
    if (userId.isBlank()) throw ValidationException("用户ID不能为空")
    if (sourceId.isBlank()) throw ValidationException("来源ID不能为空")
}
```

## 6. 数据验证规范

### 6.1 输入验证器

```kotlin
object InputValidator {
    
    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult.Error("用户名不能为空")
            username.length < 3 -> ValidationResult.Error("用户名长度不能少于3个字符")
            username.length > 20 -> ValidationResult.Error("用户名长度不能超过20个字符")
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> 
                ValidationResult.Error("用户名只能包含字母、数字和下划线")
            else -> ValidationResult.Success
        }
    }
    
    fun validateAudioSpeed(speed: Float): ValidationResult {
        return when {
            speed < 0.5f -> ValidationResult.Error("音频速度不能低于0.5倍")
            speed > 2.0f -> ValidationResult.Error("音频速度不能高于2.0倍")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

### 6.2 业务规则验证

```kotlin
class DictationSessionValidator {
    
    fun validateSessionCreation(
        userId: String,
        words: List<Word>
    ): ValidationResult {
        return when {
            userId.isBlank() -> ValidationResult.Error("用户ID不能为空")
            words.isEmpty() -> ValidationResult.Error("听写单词列表不能为空")
            words.size > 50 -> ValidationResult.Error("单次听写单词数量不能超过50个")
            else -> ValidationResult.Success
        }
    }
    
    fun validateWordRecord(
        wordId: String,
        timeSpent: Long
    ): ValidationResult {
        return when {
            wordId.isBlank() -> ValidationResult.Error("单词ID不能为空")
            timeSpent < 0 -> ValidationResult.Error("耗时不能为负数")
            timeSpent > 300_000 -> ValidationResult.Error("单词耗时不能超过5分钟")
            else -> ValidationResult.Success
        }
    }
}
```

## 7. 缓存策略规范

### 7.1 缓存接口设计

```kotlin
interface CacheManager<K, V> {
    suspend fun get(key: K): V?
    suspend fun put(key: K, value: V, ttl: Duration? = null)
    suspend fun remove(key: K)
    suspend fun clear()
    suspend fun size(): Int
}
```

### 7.2 缓存实现策略

```kotlin
class ContentCacheManager : CacheManager<String, Any> {
    private val memoryCache = LruCache<String, CacheEntry>(maxSize = 100)
    private val diskCache = DiskLruCache()
    
    override suspend fun get(key: String): Any? {
        // 1. 先查内存缓存
        memoryCache[key]?.let { entry ->
            if (!entry.isExpired()) {
                return entry.value
            } else {
                memoryCache.remove(key)
            }
        }
        
        // 2. 再查磁盘缓存
        diskCache.get(key)?.let { value ->
            memoryCache.put(key, CacheEntry(value))
            return value
        }
        
        return null
    }
    
    override suspend fun put(key: String, value: Any, ttl: Duration?) {
        val entry = CacheEntry(value, ttl?.let { System.currentTimeMillis() + it.inWholeMilliseconds })
        memoryCache.put(key, entry)
        diskCache.put(key, value)
    }
}

private data class CacheEntry(
    val value: Any,
    val expireTime: Long? = null
) {
    fun isExpired(): Boolean = expireTime?.let { System.currentTimeMillis() > it } ?: false
}
```

### 7.3 缓存策略配置

```kotlin
object CacheConfig {
    // 教材内容缓存7天
    val CONTENT_CACHE_TTL = 7.days
    
    // 用户设置持久化存储
    val USER_SETTINGS_TTL = Duration.INFINITE
    
    // 音频文件缓存30天
    val AUDIO_CACHE_TTL = 30.days
    
    // 听写记录缓存1天
    val DICTATION_RECORD_TTL = 1.days
}
```

## 8. 数据同步规范

### 8.1 同步状态管理

```kotlin
enum class SyncStatus {
    PENDING,    // 待同步
    SYNCING,    // 同步中
    SYNCED,     // 已同步
    FAILED,     // 同步失败
    CONFLICT    // 数据冲突
}

@Serializable
data class SyncRecord(
    val id: String,
    val entityType: String,
    val entityId: String,
    val operation: SyncOperation,
    val status: SyncStatus,
    val lastAttempt: String? = null,
    val retryCount: Int = 0,
    val createdAt: String
)

enum class SyncOperation {
    CREATE, UPDATE, DELETE
}
```

### 8.2 冲突解决策略

```kotlin
interface ConflictResolver<T> {
    suspend fun resolve(local: T, remote: T): T
}

class UserConflictResolver : ConflictResolver<User> {
    override suspend fun resolve(local: User, remote: User): User {
        // 使用最后更新时间戳作为解决策略
        return if (local.updatedAt > remote.updatedAt) local else remote
    }
}

class DictationSessionConflictResolver : ConflictResolver<DictationSession> {
    override suspend fun resolve(local: DictationSession, remote: DictationSession): DictationSession {
        // 听写记录以本地为准（用户已完成的记录不应被覆盖）
        return local
    }
}
```

### 8.3 增量同步实现

```kotlin
class SyncManager(
    private val repositories: List<SyncableRepository>,
    private val conflictResolvers: Map<String, ConflictResolver<*>>
) {
    
    suspend fun performSync(): SyncResult {
        val results = mutableListOf<EntitySyncResult>()
        
        repositories.forEach { repository ->
            try {
                val lastSyncTime = getLastSyncTime(repository.entityType)
                val localChanges = repository.getChangedSince(lastSyncTime)
                val remoteChanges = repository.getRemoteChanges(lastSyncTime)
                
                val syncResult = synchronizeChanges(localChanges, remoteChanges, repository)
                results.add(syncResult)
                
                updateLastSyncTime(repository.entityType)
            } catch (e: Exception) {
                results.add(EntitySyncResult.Failed(repository.entityType, e.message ?: "Unknown error"))
            }
        }
        
        return SyncResult(results)
    }
    
    private suspend fun synchronizeChanges(
        localChanges: List<SyncableEntity>,
        remoteChanges: List<SyncableEntity>,
        repository: SyncableRepository
    ): EntitySyncResult {
        // 实现具体的同步逻辑
        // 1. 处理无冲突的变更
        // 2. 识别并解决冲突
        // 3. 应用变更到本地和远程
        // 4. 返回同步结果
    }
}
```

## 9. 性能优化规范

### 9.1 数据库查询优化

```kotlin
// 使用分页查询避免一次性加载大量数据
interface WordRepository {
    suspend fun getWordsByLesson(
        lessonId: String,
        page: Int = 0,
        pageSize: Int = 50
    ): Result<PagedResult<Word>>
}

data class PagedResult<T>(
    val items: List<T>,
    val totalCount: Int,
    val currentPage: Int,
    val pageSize: Int,
    val hasNext: Boolean
)

// 使用索引优化查询
@Entity(
    tableName = "words",
    indices = [
        Index(value = ["lesson_id"]),
        Index(value = ["text"]),
        Index(value = ["difficulty", "lesson_id"])
    ]
)
data class WordEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "lesson_id") val lessonId: String?,
    val text: String,
    val difficulty: String
)
```

### 9.2 内存使用优化

```kotlin
// 使用Sequence进行惰性计算
fun processLargeWordList(words: List<Word>): Sequence<ProcessedWord> {
    return words.asSequence()
        .filter { it.difficulty == WordDifficulty.HARD }
        .map { processWord(it) }
        .take(100) // 只处理前100个
}

// 及时释放资源
class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    
    fun playAudio(audioUrl: String) {
        releasePlayer()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioUrl)
            prepareAsync()
        }
    }
    
    fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
```

### 9.3 网络请求优化

```kotlin
// 使用请求合并减少网络请求
class BatchRequestManager {
    private val pendingRequests = mutableListOf<BatchRequest>()
    private var batchTimer: Timer? = null
    
    fun addRequest(request: BatchRequest) {
        pendingRequests.add(request)
        scheduleBatch()
    }
    
    private fun scheduleBatch() {
        batchTimer?.cancel()
        batchTimer = Timer().apply {
            schedule(timerTask { executeBatch() }, 100) // 100ms后执行批处理
        }
    }
    
    private suspend fun executeBatch() {
        if (pendingRequests.isNotEmpty()) {
            val batch = pendingRequests.toList()
            pendingRequests.clear()
            
            // 执行批量请求
            apiService.batchRequest(batch)
        }
    }
}
```

## 10. 测试规范

### 10.1 单元测试结构

```kotlin
class UserRepositoryTest {
    
    @Mock
    private lateinit var remoteDataSource: UserRemoteDataSource
    
    @Mock
    private lateinit var localDataSource: UserLocalDataSource
    
    @Mock
    private lateinit var networkMonitor: NetworkMonitor
    
    private lateinit var repository: UserRepository
    
    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = UserRepositoryImpl(remoteDataSource, localDataSource, networkMonitor)
    }
    
    @Test
    fun `获取当前用户 - 网络可用时应返回远程数据`() = runTest {
        // Given
        val expectedUser = createTestUser()
        whenever(networkMonitor.isConnected).thenReturn(true)
        whenever(remoteDataSource.getCurrentUser()).thenReturn(Result.success(expectedUser))
        
        // When
        val result = repository.getCurrentUser()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
        verify(localDataSource).saveUser(expectedUser)
    }
    
    @Test
    fun `获取当前用户 - 网络不可用时应返回本地数据`() = runTest {
        // Given
        val expectedUser = createTestUser()
        whenever(networkMonitor.isConnected).thenReturn(false)
        whenever(localDataSource.getCurrentUser()).thenReturn(Result.success(expectedUser))
        
        // When
        val result = repository.getCurrentUser()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
        verify(remoteDataSource, never()).getCurrentUser()
    }
    
    private fun createTestUser() = User(
        id = "test_id",
        username = "test_user",
        grade = Grade.K1,
        textbookVersions = mapOf(Subject.ENGLISH to TextbookVersion.PEP_ENGLISH),
        createdAt = "2023-01-01T00:00:00Z",
        updatedAt = "2023-01-01T00:00:00Z"
    )
}
```

### 10.2 集成测试规范

```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DictationFlowIntegrationTest {
    
    private lateinit var database: TestDatabase
    private lateinit var apiServer: MockWebServer
    
    @BeforeAll
    fun setupTestEnvironment() {
        database = createTestDatabase()
        apiServer = MockWebServer().apply { start() }
    }
    
    @AfterAll
    fun tearDownTestEnvironment() {
        database.close()
        apiServer.shutdown()
    }
    
    @Test
    fun `完整听写流程测试`() = runTest {
        // 1. 创建测试数据
        val user = createAndSaveTestUser()
        val lesson = createAndSaveTestLesson()
        val words = createAndSaveTestWords(lesson.id)
        
        // 2. 开始听写会话
        val session = dictationService.startDictationSession(
            userId = user.id,
            sourceId = lesson.id,
            type = DictationSessionType.LESSON
        ).getOrThrow()
        
        // 3. 记录听写结果
        words.forEachIndexed { index, word ->
            val isCorrect = index % 2 == 0 // 模拟50%正确率
            dictationService.recordWordResult(
                sessionId = session.id,
                wordId = word.id,
                isCorrect = isCorrect,
                timeSpent = Random.nextLong(1000, 5000)
            ).getOrThrow()
        }
        
        // 4. 完成听写会话
        val completedSession = dictationService.completeSession(session.id).getOrThrow()
        
        // 5. 验证结果
        assertEquals(words.size, completedSession.totalWords)
        assertEquals(words.size / 2, completedSession.correctWords)
        assertEquals(0.5f, completedSession.accuracy, 0.01f)
        assertNotNull(completedSession.completedAt)
        
        // 6. 验证常错词记录
        val mistakeWords = dictationService.getCommonMistakeWords(user.id).getOrThrow()
        assertEquals(words.size / 2, mistakeWords.size)
    }
}
```

## 11. 文档和注释规范

### 11.1 类和接口文档

```kotlin
/**
 * 听写会话管理器
 * 
 * 负责管理用户的听写会话生命周期，包括会话的创建、进行中状态管理、
 * 完成和数据统计等功能。
 * 
 * @property sessionRepository 听写会话数据仓库
 * @property wordRepository 单词数据仓库
 * @property statisticsCalculator 统计计算器
 * 
 * @author MewHear Team
 * @since 1.0.0
 */
class DictationSessionManager(
    private val sessionRepository: DictationSessionRepository,
    private val wordRepository: WordRepository,
    private val statisticsCalculator: StatisticsCalculator
) {
    
    /**
     * 创建新的听写会话
     * 
     * @param userId 用户ID，不能为空
     * @param sourceId 听写来源ID（课程ID或练习册ID）
     * @param type 听写类型
     * @return 创建成功返回会话对象，失败返回错误信息
     * @throws ValidationException 当输入参数无效时抛出
     * @throws BusinessLogicException 当业务规则验证失败时抛出
     */
    suspend fun createSession(
        userId: String,
        sourceId: String,
        type: DictationSessionType
    ): Result<DictationSession> {
        // 实现逻辑...
    }
}
```

### 11.2 复杂算法文档

```kotlin
/**
 * 艾宾浩斯遗忘曲线复习算法实现
 * 
 * 基于SuperMemo SM-2算法的改进版本，用于计算单词的最佳复习间隔。
 * 算法考虑以下因素：
 * 1. 用户对单词的掌握程度（质量评分0-5）
 * 2. 当前的复习间隔
 * 3. 单词的容易因子（默认2.5）
 * 4. 已复习的次数
 * 
 * 算法公式：
 * - 如果质量评分 < 3：重置间隔为1天，重复次数归零
 * - 如果质量评分 >= 3：
 *   - 第一次复习：间隔 = 1天
 *   - 第二次复习：间隔 = 6天
 *   - 后续复习：间隔 = 上次间隔 × 容易因子
 * 
 * 容易因子更新公式：
 * EF' = EF + (0.1 - (5-质量评分) × (0.08 + (5-质量评分) × 0.02))
 * 
 * @see <a href="https://www.supermemo.com/en/archives1990-2015/english/ol/sm2">SuperMemo SM-2 Algorithm</a>
 */
class EbbinghausAlgorithm {
    
    /**
     * 计算下次复习时间
     * 
     * @param currentSchedule 当前复习计划
     * @param quality 用户本次复习的质量评分（0-5）
     *                0: 完全不记得
     *                1: 想起来了但很困难
     *                2: 想起来了但有些困难
     *                3: 想起来了但需要一些努力
     *                4: 想起来了且相对容易
     *                5: 完全记得且非常容易
     * @return 更新后的复习计划
     */
    fun calculateNextReview(
        currentSchedule: ReviewSchedule,
        quality: Int
    ): ReviewSchedule {
        require(quality in 0..5) { "质量评分必须在0-5之间" }
        
        // 实现算法逻辑...
    }
}
```

这份技术规范为听写喵App的数据结构实现提供了详细的指导原则和最佳实践。遵循这些规范将确保代码的质量、可维护性和跨平台兼容性。 