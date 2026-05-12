package com.example.marsphotos.data

import com.example.marsphotos.data.LunchEntity
// もし LunchDao が同じパッケージにあるなら下の import は不要です
// 違う場所にあるなら正しいパスを指定してください
import kotlinx.coroutines.flow.Flow // ← これがないと Flow でエラーが出ます
import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

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

    // ランチ情報を消す
    suspend fun deleteLunch(lunch: LunchEntity) {
        lunchDao.deleteLunch(lunch)
    }

    //中身を書き換える
    suspend fun updateLunch(lunch: LunchEntity) {
        lunchDao.updateLunch(lunch)
    }

    suspend fun saveImageToInternalStorage(uriString: String, context: Context): String {
        val uri = Uri.parse(uriString)
        val fileName = "lunch_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        // 保存した「内部ストレージのパス」を返す
        return file.absolutePath
    }
}