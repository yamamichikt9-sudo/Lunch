package com.example.marsphotos.data

import LunchEntity
import kotlinx.coroutines.flow.Flow

class LunchesRepository(private val lunchDao: LunchDao) {

    // 1. 全データ取得 (戻り値をFlowにする)
    fun getAllLunchesStream(): Flow<List<LunchEntity>> = lunchDao.getAllLunches()

    // 2. ジャンル絞り込み (ここがエラーの箇所)
    // 戻り値を Flow<List<LunchEntity>> に変更します
    fun getLunchesByCategoryStream(category: String): Flow<List<LunchEntity>> {
        return lunchDao.getLunchesByCategory(category)
    }

    // 3. 保存
    suspend fun insertLunch(lunch: LunchEntity) = lunchDao.insertLunch(lunch)
}