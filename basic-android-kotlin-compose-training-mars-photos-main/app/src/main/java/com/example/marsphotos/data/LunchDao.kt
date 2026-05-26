package com.example.marsphotos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
@Dao
interface LunchDao {
    @Query("SELECT * FROM lunch_table ORDER BY `date` DESC")
    fun getAllLunches(): Flow<List<LunchEntity>>

    @Insert
    suspend fun insertLunch(lunch: LunchEntity)

    @Update
    suspend fun updateLunch(lunch: LunchEntity)

    @Delete
    suspend fun deleteLunch(lunch: LunchEntity)

    @Query("SELECT * FROM lunch_table WHERE category = :category ORDER BY `date` DESC")
    fun getLunchesByCategory(category: String): Flow<List<LunchEntity>>
}