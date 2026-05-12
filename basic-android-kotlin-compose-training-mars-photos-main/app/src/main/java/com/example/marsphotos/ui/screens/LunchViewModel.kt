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
    var phoneInput by mutableStateOf("")
        private set
    var selectedGenre by mutableStateOf("和食")
        private set
    var ratingInput by mutableStateOf(0f)
        private set
    var commentInput by mutableStateOf("")
        private set
    var photoUriInput by mutableStateOf<String?>(null)
        private set

    // --- 2. バリデーションロジック ---
    // 修正案：店名さえ入っていればOKにする
    val canSave: Boolean
        get() = nameInput.isNotBlank()
    /* 一旦他のチェックをコメントアウト
    && photoUriInput != null &&
    phoneInput.all { it.isDigit() } &&
    phoneInput.isNotBlank()
    */

    var phoneNumberInput by mutableStateOf("")
        private set

    fun updatePhoneNumber(input: String) {
        phoneNumberInput = input
    }

    // --- 3. UIからの更新用関数 ---
    fun updateName(newName: String) { nameInput = newName }
    fun updateAddress(newAddress: String) { addressInput = newAddress }
    fun updatePhone(newPhone: String) { phoneInput = newPhone }
    fun updateRating(newRating: Float) { ratingInput = newRating }
    fun updateGenre(newGenre: String) { selectedGenre = newGenre }
    fun updateComment(newComment: String) { commentInput = newComment }
    fun updatePhoto(uri: String) { photoUriInput = uri }

    // --- 4. 保存アクション ---
    fun saveLunch() {
        if (!canSave) return

        val newLunch = LunchEntity(
            name = nameInput,
            address = addressInput,
            phoneNumber = phoneInput,
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
    }

    // --- 5. 作成マニュアル（Factory） ---
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // ここで MarsPhotosApplication を正しくキャスト
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                val repository = application.container.lunchesRepository
                LunchViewModel(lunchesRepository = repository)
            }
        }
    }
}