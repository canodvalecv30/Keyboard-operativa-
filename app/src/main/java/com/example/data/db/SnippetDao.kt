package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SnippetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {

    @Query("SELECT * FROM snippets ORDER BY isFavorite DESC, usageCount DESC, createdTimestamp DESC")
    fun getAllSnippets(): Flow<List<SnippetEntity>>

    @Query("SELECT * FROM snippets WHERE categoryId = :categoryId ORDER BY isFavorite DESC, usageCount DESC")
    fun getSnippetsByCategory(categoryId: Int): Flow<List<SnippetEntity>>

    @Query("SELECT * FROM snippets WHERE isFavorite = 1 ORDER BY usageCount DESC")
    fun getFavoriteSnippets(): Flow<List<SnippetEntity>>

    @Query("SELECT * FROM snippets WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchSnippets(query: String): Flow<List<SnippetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: SnippetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippets(snippets: List<SnippetEntity>)

    @Update
    suspend fun updateSnippet(snippet: SnippetEntity)

    @Query("UPDATE snippets SET usageCount = usageCount + 1, lastUsedTimestamp = :timestamp WHERE id = :snippetId")
    suspend fun incrementUsage(snippetId: Int, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteSnippet(snippet: SnippetEntity)

    @Query("DELETE FROM snippets")
    suspend fun deleteAllSnippets()

    @Query("DELETE FROM snippets WHERE id = :id")
    suspend fun deleteSnippetById(id: Int)

    @Query("DELETE FROM snippets WHERE categoryId = :categoryId")
    suspend fun deleteSnippetsByCategoryId(categoryId: Int)
}
