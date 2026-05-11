package com.example.marsphotos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.marsphotos.LunchDao

// データベースの設定：保存するデータの種類（Entity）を指定します
@Database(entities = [LunchEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // DAO（注文票）を外から使えるようにします
    abstract fun lunchDao(): LunchDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        // データベースの実体（インスタンス）を作る「魔法の言葉」です
        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "lunch_database" // スマホ内に保存されるファイル名
                ).build().also { Instance = it }
            }
        }
    }
}