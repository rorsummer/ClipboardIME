package com.clipboardime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ClipboardEntity): Long

    @Query("SELECT * FROM clipboard_history WHERE content LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    fun searchByKeyword(keyword: String): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboard_history ORDER BY timestamp DESC LIMIT 100")
    fun getAll(): Flow<List<ClipboardEntity>>

    @Query("SELECT COUNT(*) FROM clipboard_history WHERE content = :content")
    suspend fun countByContent(content: String): Int

    @Query("DELETE FROM clipboard_history")
    suspend fun deleteAll()

    @Query("DELETE FROM clipboard_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
