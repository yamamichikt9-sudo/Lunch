package com.example.marsphotos.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.LunchEntity
import com.example.marsphotos.data.LunchesRepository
import com.example.marsphotos.data.ShopApi
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LunchViewModel(
    private val lunchesRepository: LunchesRepository
) : ViewModel() {

    // --- 入力フォームの状態 ---
    var nameInput by mutableStateOf("")
    var addressInput by mutableStateOf("")
    var phoneNumberInput by mutableStateOf("")
    var selectedGenre by mutableStateOf("和食")
    var ratingInput by mutableStateOf(0f)
    var commentInput by mutableStateOf("")
    var photoUriInput by mutableStateOf<String?>(null)
    var selectedLunch by mutableStateOf<LunchEntity?>(null)

    // 今「編集」しているのか「新規」なのかを判断するIDを保持
    var editingLunchId: Int? by mutableStateOf(null)

    var dateInput by mutableStateOf(System.currentTimeMillis())

    private val _filterDate = mutableStateOf<Long?>(null)
    val filterDate: Long?
        get() = _filterDate.value

    fun setFilterDate(date: Long?) {
        _filterDate.value = date
    }

    // --- 期間選択（フィルタ）の状態 ---
    var startDateFilter by mutableStateOf<Long?>(null)
        private set
    var endDateFilter by mutableStateOf<Long?>(null)
        private set

    fun updateStartDate(date: Long?) {
        startDateFilter = date
    }

    fun updateEndDate(date: Long?) {
        endDateFilter = date
    }

    fun clearPeriodFilter() {
        startDateFilter = null
        endDateFilter = null
    }

    var reactionsInput by mutableStateOf<List<String>>(emptyList())

    // 🔍 検索されたお店の候補リストを一時的に保存する変数
    var searchCandidates = mutableStateListOf<com.example.marsphotos.data.ShopSearchItem>()
        private set

    var showNoResultDialog by mutableStateOf(false)
        private set

    // --- データのリスト ---
    val lunchList = mutableStateListOf<LunchEntity>()

    // 🪙 コインアニメーションダイアログを表示するためのフラグ
    var showCoinAnimation by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            lunchesRepository.getAllLunchesStream().collect { items ->
                lunchList.clear()
                lunchList.addAll(items.reversed())
            }
        }
    }

    val groupedLunches: Map<String, List<LunchEntity>>
        get() = lunchList.groupBy {
            SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                .format(Date(it.date))
        }

    // 保存ボタンの有効判定
    val canSave: Boolean
        get() = nameInput.isNotBlank()

    // --- 各入力項目の更新用関数 ---
    fun updateName(newName: String) { nameInput = newName }
    fun updateReactions(reactions: List<String>) { reactionsInput = reactions }
    fun updateAddress(newAddress: String) { addressInput = newAddress }
    fun updatePhoneNumber(input: String) { phoneNumberInput = input }
    fun updateRating(newRating: Float) { ratingInput = newRating }
    fun updateGenre(newGenre: String) { selectedGenre = newGenre }
    fun updateComment(newComment: String) { commentInput = newComment }
    fun updatePhoto(uri: String) { photoUriInput = uri }
    fun updateDate(date: Long) { dateInput = date }

    // 🌐 店名から本物の住所・電話番号を検索する関数（キーワード優先・並び替え版）
    fun searchAndAutoFillShop(context: android.content.Context) {
        val trimmedKeyword = nameInput.trim()
        if (trimmedKeyword.isBlank()) return

        searchCandidates.clear()

        viewModelScope.launch {
            try {
                val response = ShopApi.retrofitService.searchShop(keyword = trimmedKeyword)

                if (response.isNotEmpty()) {
                    val sortedList = response.sortedByDescending { item ->
                        trimmedKeyword.split(" ", " ").any { word ->
                            item.displayName.contains(word)
                        }
                    }
                    searchCandidates.addAll(sortedList)
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "自動入力できませんでした。右側の 🌐 ボタンをお試しください。",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    context,
                    "通信エラーが発生しました。手動で入力するか 🌐 ボタンをお試しください。",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun dismissNoResultDialog() {
        showNoResultDialog = false
    }

    // ユーザーが候補の中から「この店！」と選んだときに呼び出す関数
    fun selectCandidate(item: com.example.marsphotos.data.ShopSearchItem) {
        addressInput = item.displayName

        // 📞 【電話番号対策！】データが空っぽなら親切なガイド文字を入れる
        val telNumber = item.extratags?.phone
            ?: item.extratags?.contactPhone
            ?: "（データなし：手動で入力してください）"
        phoneNumberInput = telNumber

        searchCandidates.clear()
    }

    fun clearCandidates() {
        searchCandidates.clear()
    }

    fun onLunchSelected(lunch: LunchEntity) {
        selectedLunch = lunch
    }

    fun clearSelectedLunch() {
        selectedLunch = null
    }

    fun prepareEdit(lunch: LunchEntity) {
        editingLunchId = lunch.id
        nameInput = lunch.name
        addressInput = lunch.address ?: ""
        phoneNumberInput = lunch.phoneNumber ?: ""
        selectedGenre = lunch.category
        ratingInput = lunch.rating
        commentInput = lunch.comment
        photoUriInput = lunch.photoUrl
        dateInput = lunch.date
        reactionsInput = lunch.reactions
    }

    private fun resetInputs() {
        nameInput = ""
        addressInput = ""
        phoneNumberInput = ""
        selectedGenre = "和食"
        ratingInput = 0f
        commentInput = ""
        photoUriInput = null
        dateInput = System.currentTimeMillis()
        reactionsInput = emptyList()
    }

    // --- DB操作 ---
    fun saveLunch(context: android.content.Context, onSaveComplete: () -> Unit) {
        if (!canSave) return

        val lunch = LunchEntity(
            id = editingLunchId ?: 0,
            name = nameInput,
            address = addressInput,
            phoneNumber = phoneNumberInput,
            rating = ratingInput,
            category = selectedGenre,
            comment = commentInput,
            photoUrl = photoUriInput ?: "",
            date = dateInput,
            reactions = reactionsInput
        )

        viewModelScope.launch {
            if (editingLunchId == null) {
                lunchesRepository.insertLunch(lunch)

                // 🔒 1日1回判定
                val todayStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(java.util.Date())
                val prefs = context.getSharedPreferences("lunch_point_prefs", android.content.Context.MODE_PRIVATE)
                val lastCheckedDate = prefs.getString("last_point_date", "")

                if (lastCheckedDate == todayStr) {
                    // ⚠️ 2回目以降：トーストを出してスマートにメイン画面へ戻る
                    android.widget.Toast.makeText(context, "本日のポイントは獲得済みです", android.widget.Toast.LENGTH_SHORT).show()
                    resetInputs()
                    onSaveComplete()
                } else {
                    // 🎉 初めての登録：ポイントを足してアニメーション開始！
                    prefs.edit().putString("last_point_date", todayStr).apply()

                    val currentCount = prefs.getInt("total_bonus_points", 0)
                    prefs.edit().putInt("total_bonus_points", currentCount + 100).apply()

                    // アニメーションダイアログを表示
                    showCoinAnimation = true
                }

            } else {
                lunchesRepository.updateLunch(lunch)
                resetInputs()
                editingLunchId = null
                onSaveComplete()
            }
        }
    }

    // 🪙 画面に表示する合計ポイントの計算
    fun getTotalPoints(context: android.content.Context): Int {
        val prefs = context.getSharedPreferences("lunch_point_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getInt("total_bonus_points", 0)
    }

    // 🎉 ボタンを押したときに、満を持してメイン画面へ戻る処理
    fun completeCoinAnimation(onSaveComplete: () -> Unit) {
        showCoinAnimation = false // ダイアログを閉じる
        resetInputs()             // 入力内容をリセット
        editingLunchId = null     // 編集IDクリア

        // ✨ 確実に画面を戻します！
        onSaveComplete()
    }

    fun deleteLunch(lunch: LunchEntity) {
        viewModelScope.launch {
            lunchesRepository.deleteLunch(lunch)
        }
    }

    fun saveImageToInternalStorage(context: android.content.Context, uriString: String): String {
        return try {
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "lunch_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(context.filesDir, fileName)

            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            android.net.Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            uriString
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                val repository = application.container.lunchesRepository
                LunchViewModel(lunchesRepository = repository)
            }
        }
    }
}