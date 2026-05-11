package com.example.marsphotos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
<<<<<<< HEAD
// もし LunchDao が同じフォルダ(package com.example.marsphotos.data)なら
// インポートは不要、または以下になります
// import com.example.marsphotos.data.LunchDao
=======
import com.example.marsphotos.data.LunchDao
>>>>>>> 53ac958c4c2be4a2943b73e4f147fe594ec247d5

@Database(entities = [LunchEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lunchDao(): LunchDao // ここが赤くなくなればOK

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "lunch_database"
                )
                    .fallbackToDestructiveMigration() // 構造変更時にデータをリセットして壊れないようにする（追加推奨）
                    .build()
                    .also { Instance = it }
            }
        }
    }
}