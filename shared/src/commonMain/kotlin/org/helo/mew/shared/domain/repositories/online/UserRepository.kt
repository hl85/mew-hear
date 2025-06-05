package org.helo.mew.shared.domain.repositories

import org.helo.mew.shared.domain.entities.user.User
import org.helo.mew.shared.domain.entities.user.UserSettings

/**
 * 用户仓库接口
 * 提供用户相关数据的访问和操作方法
 */
interface UserRepository {
    suspend fun getCurrentUser():Result<User?>
    suspend fun updateUser(user: User): Result<User>
    suspend fun getUserSettings(userId: String): Result<UserSettings>
    suspend fun updateUserSettings(settings: UserSettings): Result<UserSettings>
    suspend fun login(userIdOrMobileNumer: String, password: String): Result<User>
    suspend fun register(user: User): Result<User>
    suspend fun logout(): Result<Unit>
}