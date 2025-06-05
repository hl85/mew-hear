package org.helo.mew.shared.domain.repositories

/**
 * 教材仓库接口
 */
interface TextbookRepository {
    suspend fun getTextbookList(): Result<List<String>>
    suspend fun getTextbookById(id: String): Result<String>
    suspend fun addTextbook(textbook: String): Result<String>
    suspend fun updateTextbook(id: String, textbook: String): Result<String>
    suspend fun deleteTextbook(id: String): Result<Unit>
    suspend fun getTextbookUnits(textbookId: String): Result<List<String>>
    suspend fun getTextbookUnitById(textbookId: String, unitId: String): Result<String>
}