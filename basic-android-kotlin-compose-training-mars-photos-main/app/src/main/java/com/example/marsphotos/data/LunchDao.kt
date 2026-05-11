package com.example.marsphotos.data

<<<<<<< HEAD
//ddddddddddd
=======
import LunchEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface LunchDao {
    // 全データを取得（新しい順）
    // LunchDao.kt
    @Query("SELECT * FROM lunch_table ORDER BY date DESC")
    fun getAllLunches(): Flow<List<LunchEntity>> // ← ここが Flow<List<...>> になっていますか？
    // 新規保存
    @Insert
    suspend fun insertLunch(lunch: LunchEntity)

    // 更新（あとで編集したい場合）
    @Update
    suspend fun updateLunch(lunch: LunchEntity)

    // 削除
    @Delete
    suspend fun deleteLunch(lunch: LunchEntity)

    @Query("SELECT * FROM lunch_table WHERE category = :category ORDER BY date DESC")
    fun getLunchesByCategory(category: String): Flow<List<LunchEntity>>
}
>>>>>>> 374ae4ef6e1cab32798f2bfffb191b1682dcc4c0
