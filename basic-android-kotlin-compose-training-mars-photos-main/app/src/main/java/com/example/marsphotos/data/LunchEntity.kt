import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lunch_table")
data class LunchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,          // ID（自動で番号が振られます）
    val name: String,         // 店名
    val address: String,      // 住所
    val photoUrl: String,     // 写真URL
    val rating: Float,        // 5段階評価
    val comment: String,      // コメント
    val date: Long,           // 日付（ミリ秒）
    val phoneNumber: String,  // 電話番号
    val category: String      // ジャンル
)