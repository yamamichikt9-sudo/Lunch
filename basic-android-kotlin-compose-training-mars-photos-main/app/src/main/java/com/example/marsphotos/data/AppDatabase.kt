package com.example.marsphotos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// もし LunchDao が同じフォルダ(package com.example.marsphotos.data)なら
// インポートは不要、または以下になります
// import com.example.marsphotos.data.LunchDao
import com.example.marsphotos.data.LunchDao
import retrofit2.Converter


@Database(entities = [LunchEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
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

class Converters{
    @androidx.room.TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(separator = ",")
    }

    @androidx.room.TypeConverter
    fun toList(data: String?): List<String>? {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",")
    }
}