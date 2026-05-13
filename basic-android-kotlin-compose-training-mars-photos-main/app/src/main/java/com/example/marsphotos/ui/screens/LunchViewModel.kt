package com.example.marsphotos.ui.screens

import androidx.compose.runtime.getValue
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

class LunchViewModel(
    private val lunchesRepository: LunchesRepository
) : ViewModel() {

    // --- 1. UIの状態管理 ---
    var nameInput by mutableStateOf("")
        private set
    var addressInput by mutableStateOf("")
        private set

    // 変数名を統一（phoneInputを削除し、こちらに一本化）
    var phoneNumberInput by mutableStateOf("")
        private set
    val lunchList = androidx.compose.runtime.mutableStateListOf<LunchEntity>()

    init {
        viewModelScope.launch {
            // リポジトリからデータを取ってきてリストに反映させる
            lunchesRepository.getAllLunchesStream().collect { items ->
                lunchList.clear()
                lunchList.addAll(items.reversed()) // 新しい順に並べる
            }
        }
    }

    var selectedGenre by mutableStateOf("和食")
        private set
    var ratingInput by mutableStateOf(0f)
        private set
    var commentInput by mutableStateOf("")
        private set
    var photoUriInput by mutableStateOf<String?>(null)
        private set

    // --- 2. バリデーションロジック ---
    // 電話番号はここに含まれていないので、空でも保存可能です（任意項目）
    val canSave: Boolean
        get() = nameInput.isNotBlank()

    // --- 3. UIからの更新用関数 ---
    fun updateName(newName: String) { nameInput = newName }
    fun updateAddress(newAddress: String) { addressInput = newAddress }

    // UI側のコード（AddLunchScreen）の呼び出しもこれに合わせてください
    fun updatePhoneNumber(input: String) {
        phoneNumberInput = input
    }

    fun updateRating(newRating: Float) { ratingInput = newRating }
    fun updateGenre(newGenre: String) { selectedGenre = newGenre }
    fun updateComment(newComment: String) { commentInput = newComment }
    fun updatePhoto(uri: String) { photoUriInput = uri }

    private fun resetInputs() {
        nameInput = ""
        addressInput = ""
        phoneNumberInput = ""
        selectedGenre = "和食"
        ratingInput = 0f
        commentInput = ""
        photoUriInput = null
    }

    // --- 4. 保存アクション ---
    fun saveLunch() {
        if (!canSave) return

        val newLunch = LunchEntity(
            name = nameInput,
            address = addressInput,
            // Entityの引数名に合わせて、UIの入力値を渡す
            phoneNumber = phoneNumberInput,
            rating = ratingInput,
            category = selectedGenre,
            comment = commentInput,
            photoUrl = photoUriInput ?: "",
            date = System.currentTimeMillis()
        )

        viewModelScope.launch {
            lunchesRepository.insertLunch(newLunch)
            println("DBに保存しました: $newLunch")
        }

        resetInputs()
    }

    // --- 5. Factory (変更なし) ---
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