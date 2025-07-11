package com.asadbyte.translatorapp.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(translation: TranslationHistory): Long

    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TranslationHistory>>

    @Query("DELETE FROM translation_history")
    suspend fun clearAllHistory()

    @Query("SELECT * FROM translation_history WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedItems(): Flow<List<TranslationHistory>>

    @Query("DELETE FROM translation_history WHERE id IN (:itemIds)")
    suspend fun deleteItemsByIds(itemIds: List<Int>)

    @Update
    suspend fun update(translation: TranslationHistory)
}