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
    // 💡 外部（ダイアログ選択時など）から自動入力できるように private set を外しました！
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

    var filterDate by mutableStateOf<Long?>(null)

    var reactionsInput by mutableStateOf<List<String>>(emptyList())

    // 🔍 検索されたお店の候補リストを一時的に保存する変数
    var searchCandidates = mutableStateListOf<com.example.marsphotos.data.ShopSearchItem>()
        private set

    // ❓ お店が見つからなかったときに、救済ダイアログを表示するためのフラグ（重複を削除してここに集約！）
    var showNoResultDialog by mutableStateOf(false)
        private set

    // --- データのリスト ---
    val lunchList = mutableStateListOf<LunchEntity>()

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
                    // 💡 【ここを強化！】
                    // ユーザーが入力した文字（例：日野市）が、住所（displayName）の中に
                    // 含まれているものを一番上（trueが先）に持ってくるように自動で並び替える！
                    val sortedList = response.sortedByDescending { item ->
                        // 入力されたキーワードの一部分（地名など）が住所に含まれているか判定
                        // 全体の店名だと一致しにくいので、スペースで区切られた単語が含まれているかでチェックすると賢いです
                        trimmedKeyword.split(" ", " ").any { word ->
                            item.displayName.contains(word)
                        }
                    }

                    searchCandidates.addAll(sortedList)
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "自動入力できませんでした。手動で入力してください。",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    context,
                    "自動入力できませんでした。手動で入力してください。",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // 救済ダイアログを閉じるための関数
    fun dismissNoResultDialog() {
        showNoResultDialog = false
    }

    // ユーザーが候補の中から「この店！」と選んだときに呼び出す関数
    fun selectCandidate(item: com.example.marsphotos.data.ShopSearchItem) {
        // ① 選ばれたお店の正式名称と住所をそのまま住所欄へ
        addressInput = item.displayName

        // ② 電話番号を取り出して入力
        val telNumber = item.extratags?.phone
            ?: item.extratags?.contactPhone
            ?: ""
        phoneNumberInput = telNumber

        // ③ 用が済んだので、候補リストを空にしてダイアログを自動で閉じさせる
        searchCandidates.clear()
    }

    // ダイアログをキャンセルした時に候補リストをクリアする用
    fun clearCandidates() {
        searchCandidates.clear()
    }

    fun onLunchSelected(lunch: LunchEntity) {
        selectedLunch = lunch
    }

    fun clearSelectedLunch() {
        selectedLunch = null
    }

    // 編集ボタンを押したときに、今のデータを入力欄にセットする関数
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

    // 入力フィールドを空にする
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
    fun saveLunch() {
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
            } else {
                lunchesRepository.updateLunch(lunch)
            }

            resetInputs()
            editingLunchId = null
        }
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