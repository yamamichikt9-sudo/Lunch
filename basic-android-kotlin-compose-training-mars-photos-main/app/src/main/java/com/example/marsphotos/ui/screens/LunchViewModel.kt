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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LunchViewModel(
    private val lunchesRepository: LunchesRepository
) : ViewModel() {

    // --- 入力フォームの状態 ---
    var nameInput by mutableStateOf("")
        private set
    var addressInput by mutableStateOf("")
        private set
    var phoneNumberInput by mutableStateOf("")
        private set
    var selectedGenre by mutableStateOf("和食")
        private set
    var ratingInput by mutableStateOf(0f)
        private set
    var commentInput by mutableStateOf("")
        private set
    var photoUriInput by mutableStateOf<String?>(null)
        private set
    var selectedLunch by mutableStateOf<LunchEntity?>(null)
        private set
    // 1. 今「編集」しているのか「新規」なのかを判断するIDを保持
    var editingLunchId: Int? by mutableStateOf(null)
        private set

    var dateInput by mutableStateOf(System.currentTimeMillis())
        private set


    // --- データのリスト ---
    // 重複していた宣言を1つにまとめました
    val lunchList = mutableStateListOf<LunchEntity>()

    init {
        viewModelScope.launch {
            // リポジトリからデータを取得してリストに反映させる
            lunchesRepository.getAllLunchesStream().collect { items ->
                lunchList.clear()
                // 新しいデータが上に来るように逆順で追加
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
    fun updateAddress(newAddress: String) { addressInput = newAddress }
    fun updatePhoneNumber(input: String) { phoneNumberInput = input }
    fun updateRating(newRating: Float) { ratingInput = newRating }
    fun updateGenre(newGenre: String) { selectedGenre = newGenre }
    fun updateComment(newComment: String) { commentInput = newComment }
    fun updatePhoto(uri: String) { photoUriInput = uri }

    fun updateDate(date: Long) { dateInput = date }

    fun onLunchSelected(lunch: LunchEntity) {
        selectedLunch = lunch
        // ここで「画面を詳細へ切り替える」命令をUIに出す
    }

    // 詳細画面から戻るときなどに、選択を解除する関数
    fun clearSelectedLunch() {
        selectedLunch = null
    }

    // 2. 編集ボタンを押したときに、今のデータを入力欄にセットする関数
    fun prepareEdit(lunch: LunchEntity) {
        editingLunchId = lunch.id
        nameInput = lunch.name
        addressInput = lunch.address ?: ""
        phoneNumberInput = lunch.phoneNumber ?: ""
        selectedGenre = lunch.category
        ratingInput = lunch.rating.toFloat()
        commentInput = lunch.comment
        photoUriInput = lunch.photoUrl
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
    }

    // --- DB操作 ---
    fun saveLunch() {
        if (!canSave) return

        // 編集なら元のIDを使い、新規なら0（自動採番）を使う
        val lunch = LunchEntity(
            id = editingLunchId ?: 0,
            name = nameInput,
            address = addressInput,
            phoneNumber = phoneNumberInput,
            rating = ratingInput,
            category = selectedGenre,
            comment = commentInput,
            photoUrl = photoUriInput ?: "",
            date = dateInput
        )

        viewModelScope.launch {
            if (editingLunchId == null) {
                // 新規登録
                lunchesRepository.insertLunch(lunch)
            } else {
                // ★編集保存（上書き）
                lunchesRepository.updateLunch(lunch)
            }

            resetInputs() // 保存完了後に入力をリセット
            editingLunchId = null // 編集モードを終了
        }
    }

    fun deleteLunch(lunch: LunchEntity) {
        viewModelScope.launch {
            lunchesRepository.deleteLunch(lunch)
        }
    }

    /**
     * 選択された画像をアプリ専用の内部ストレージに保存し、新しいURIを返す
     */
    fun saveImageToInternalStorage(context: android.content.Context, uriString: String): String {
        return try {
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)

            // ファイル名をユニークにする（例：lunch_1715650000.jpg）
            val fileName = "lunch_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(context.filesDir, fileName)

            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            // コープした新しいファイルのURI（file://...）を文字列で返す
            android.net.Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            uriString // 失敗した場合は元のURIを返す（バックアップ策）
        }
    }

    // ViewModelFactoryの設定
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

// MarsViewModel.kt 内