# KMP 项目架构规范指南

## 目录
- [1. 项目结构](#1-项目结构)
- [2. 模块划分](#2-模块划分)
- [3. 命名规范](#3-命名规范)
- [4. 设计模式](#4-设计模式)
- [5. 最佳实践](#5-最佳实践)
- [6. 云同步扩展](#6-云同步扩展)

## 1. 项目结构

### 1.1 基础目录结构
```
├── app/                    # Android 应用模块
├── ios-app/               # iOS 应用模块
├── desktop-app/           # 桌面应用模块
├── shared/                # 跨平台共享代码
├── domain/                # 领域层
├── data/                  # 数据层
├── features/              # 功能模块
└── libraries/             # 第三方库配置
```

### 1.2 核心模块说明
- **app/ios-app/desktop-app**: 平台特定的入口应用
- **shared**: 跨平台共享的代码，包含通用工具和配置
- **domain**: 业务实体和用例
- **data**: 数据访问和存储实现
- **features**: 功能模块，包含UI和业务逻辑

## 2. 模块划分

### 2.1 表现层（UI）
- 位置：`features/`
- 职责：
  - 用户界面实现
  - 视图模型（ViewModel）
  - 状态管理
  - 用户交互处理

### 2.2 领域层（Domain）
- 位置：`domain/`
- 职责：
  - 业务实体定义
  - 用例（Use Cases）实现
  - 仓库接口定义
  - 业务规则实现

### 2.3 数据层（Data）
- 位置：`data/`
- 子模块：
  - `local/`: 本地数据存储
  - `repository/`: 仓库模式实现
  - `remote/`: 远程数据访问（如果需要）

## 3. 命名规范

### 3.1 类命名
- **实体类**：使用名词，如 `Task`、`Category`
- **接口**：描述功能，如 `TaskRepository`、`TaskNotification`
- **实现类**：接口名 + Impl，如 `TaskRepositoryImpl`
- **用例类**：动词 + 名词，如 `AddTask`、`UpdateTaskTitle`
- **视图模型**：特性名 + ViewModel，如 `TaskListViewModel`

### 3.2 数据库命名
- **表名**：单数形式，如 `Task`、`Category`
- **字段名**：表名_字段名，如 `task_id`、`task_title`
- **索引名**：index_表名_字段名，如 `index_Task_category_id`

### 3.3 文件组织
- **包名**：功能模块名，如 `com.example.task`
- **测试类**：被测试类名 + Test，如 `TaskRepositoryTest`
- **Fake实现**：Fake + 类名，如 `FakeTaskRepository`

## 4. 设计模式

### 4.1 核心设计模式
1. **Repository Pattern**
   ```kotlin
   interface TaskRepository {
       suspend fun getTasks(): Flow<List<Task>>
       suspend fun addTask(task: Task)
       suspend fun updateTask(task: Task)
       suspend fun deleteTask(task: Task)
   }
   ```

2. **Mapper Pattern**
   ```kotlin
   interface EntityMapper<Domain, Data> {
       fun toDomain(data: Data): Domain
       fun toData(domain: Domain): Data
   }
   ```

3. **Use Case Pattern**
   ```kotlin
   interface AddTask {
       suspend operator fun invoke(task: Task)
   }
   ```

### 4.2 依赖注入
- 使用 Koin/Hilt 进行依赖注入
- 按模块组织依赖注入模块
- 提供测试专用的依赖注入配置

## 5. 最佳实践

### 5.1 错误处理
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### 5.2 协程使用
```kotlin
class TaskRepository(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun getTasks() = withContext(dispatcher) {
        // 实现
    }
}
```

### 5.3 测试规范
- 单元测试覆盖核心业务逻辑
- 提供 Fake 实现用于测试
- 使用测试专用的调度器
- 避免在测试中使用实际的网络/数据库调用

## 6. 云同步扩展

### 6.1 同步模块结构
```
data/
├── remote/
│   ├── api/          # API 接口
│   ├── model/        # 远程数据模型
│   └── datasource/   # 远程数据源
├── sync/
│   ├── worker/       # 同步工作器
│   ├── strategy/     # 同步策略
│   └── conflict/     # 冲突处理
└── repository/
    └── impl/         # 同步实现
```

### 6.2 同步相关命名规范
- **远程实体**：以 RemoteEntity 结尾
- **API接口**：以 Api 结尾
- **数据源**：以 DataSource 结尾
- **同步相关**：以 Sync 开头或结尾
- **冲突处理**：以 Resolver 结尾

### 6.3 同步模式示例
```kotlin
interface SyncStrategy {
    suspend fun sync(
        localData: List<LocalEntity>,
        remoteData: List<RemoteEntity>
    ): SyncResult
}

interface ConflictResolver {
    suspend fun resolve(
        local: LocalEntity,
        remote: RemoteEntity
    ): ResolutionResult
}
```

### 6.4 同步状态管理
```kotlin
enum class SyncStatus {
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DELETE,
    CONFLICT
}

data class SyncMetadata(
    val lastSyncTime: LocalDateTime,
    val version: Long,
    val status: SyncStatus
)
```

## 附录：示例代码

### A.1 完整的 Task 实体示例
```kotlin
data class Task(
    val id: Long = 0,
    val completed: Boolean = false,
    val title: String,
    val description: String? = null,
    val categoryId: Long? = null,
    val dueDate: LocalDateTime? = null,
    val creationDate: LocalDateTime? = null,
    val completedDate: LocalDateTime? = null,
    val isRepeating: Boolean = false,
    val alarmInterval: AlarmInterval? = null,
)
```

### A.2 Repository 实现示例
```kotlin
class TaskRepositoryImpl(
    private val localDataSource: TaskLocalDataSource,
    private val remoteDataSource: TaskRemoteDataSource,
    private val mapper: TaskMapper
) : TaskRepository {
    override suspend fun getTasks(): Flow<List<Task>> =
        combine(
            localDataSource.getTasks(),
            remoteDataSource.getTasks()
        ) { local, remote ->
            // 合并逻辑
        }
}
``` 