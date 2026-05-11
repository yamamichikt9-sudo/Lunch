package com.example.marsphotos

@Dao
interface LunchDao {
    // 全データを取得（新しい順）
    @Query("SELECT * FROM lunch_table ORDER BY date DESC")
    fun getAllLunches(): List<LunchEntity>

    // 新規保存
    @Insert
    suspend fun insertLunch(lunch: LunchEntity)

    // 更新（あとで編集したい場合）
    @Update
    suspend fun updateLunch(lunch: LunchEntity)

    // 削除
    @Delete
    suspend fun deleteLunch(lunch: LunchEntity)
}