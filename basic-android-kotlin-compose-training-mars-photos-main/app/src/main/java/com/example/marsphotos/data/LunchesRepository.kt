package com.example.marsphotos.data

import LunchEntity
// もし LunchDao が同じパッケージにあるなら下の import は不要です
// 違う場所にあるなら正しいパスを指定してください
import com.example.marsphotos.data.LunchDao
import kotlinx.coroutines.flow.Flow // ← これがないと Flow でエラーが出ます

class LunchesRepository(private val lunchDao: LunchDao) {

    // 1. 全データ取得
    fun getAllLunchesStream(): Flow<List<LunchEntity>> = lunchDao.getAllLunches()

    // 2. ジャンル絞り込み
    fun getLunchesByCategoryStream(category: String): Flow<List<LunchEntity>> {
        return lunchDao.getLunchesByCategory(category)
    }

    // 3. 保存
    suspend fun insertLunch(lunch: LunchEntity) {
        lunchDao.insertLunch(lunch)
    }
}