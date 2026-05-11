package com.example.marsphotos.data

import LunchEntity
import kotlinx.coroutines.flow.Flow

// ロジック担当が使いやすいように整える
class LunchesRepository(private val lunchDao: LunchDao) {
    // 全データ取得（リアルタイムに画面を更新できるようFlowを使うのが一般的です）
    fun getAllLunchesStream(): List<LunchEntity> = lunchDao.getAllLunches()

    // 保存
    suspend fun insertLunch(lunch: LunchEntity) = lunchDao.insertLunch(lunch)

    fun getLunchesByCategory(category: String): List<LunchEntity> {
        return lunchDao.getLunchesByCategory(category)
    }
}