package com.clipboardime.data

import kotlinx.coroutines.flow.Flow

class ClipboardRepository(private val dao: ClipboardDao) {

    fun searchByKeyword(keyword: String): Flow<List<ClipboardEntity>> {
        return dao.searchByKeyword(keyword)
    }

    fun getAll(): Flow<List<ClipboardEntity>> {
        return dao.getAll()
    }

    suspend fun addClipboardEntry(content: String) {
        if (content.isBlank()) return
        val trimmed = content.trim()
        // 去重：如果已存在相同内容则跳过
        if (dao.countByContent(trimmed) > 0) return
        dao.insert(ClipboardEntity(content = trimmed))
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}
