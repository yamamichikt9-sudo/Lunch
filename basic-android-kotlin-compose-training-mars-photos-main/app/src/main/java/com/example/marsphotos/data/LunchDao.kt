package com.example.marsphotos.data

import LunchEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

// 注意：LunchEntityが別パッケージにある場合は、以下のようなインポートが必要です
// import com.example.marsphotos.model.LunchEntity

@Dao
interface LunchDao {

    // 全データを取得（日付の新しい順）
    // dateはSQLの予約語に近いため、` `（バッククォート）で囲むとより安全です
    @Query("SELECT * FROM lunch_table ORDER BY `date` DESC")
    fun getAllLunches(): Flow<List<LunchEntity>>

    // 新規保存
    @Insert
    suspend fun insertLunch(lunch: LunchEntity)

    // 更新
    @Update
    suspend fun updateLunch(lunch: LunchEntity)

    // 削除
    @Delete
    suspend fun deleteLunch(lunch: LunchEntity)

    // カテゴリごとに取得（日付の新しい順）
    // :category と ORDER BY の間に必ず半角スペースを入れてください
    @Query("SELECT * FROM lunch_table WHERE category = :category ORDER BY `date` DESC")
    fun getLunchesByCategory(category: String): Flow<List<LunchEntity>>
<<<<<<< HEAD
}
=======
}
>>>>>>> 9caefa2e9dcb51a40837bdcd5036b5575797530f
