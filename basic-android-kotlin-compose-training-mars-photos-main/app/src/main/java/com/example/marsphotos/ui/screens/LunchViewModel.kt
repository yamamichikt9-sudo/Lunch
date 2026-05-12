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

    // 表示用のリスト
    val lunchList = androidx.compose.runtime.mutableStateListOf<LunchEntity>()

    init {
        viewModelScope.launch {
            // リポジトリからデータを取得して反映
            lunchesRepository.getAllLunchesStream().collect { items ->
                lunchList.clear()
                lunchList.addAll(items.reversed())
            }
        }
    }

    // --- 2. バリデーション ---
    val canSave: Boolean
        get() = nameInput.isNotBlank()

    // --- 3. UIからの更新用関数 ---
    fun updateName(newName: String) { nameInput = newName }
    fun updateAddress(newAddress: String) { addressInput = newAddress }
    fun updatePhoneNumber(input: String) { phoneNumberInput = input }
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

    // --- 4. アクション ---
    // 保存
    fun saveLunch() {
        if (!canSave) return
        val newLunch = LunchEntity(
            name = nameInput,
            address = addressInput,
            phoneNumber = phoneNumberInput,
            rating = ratingInput,
            category = selectedGenre,
            comment = commentInput,
            photoUrl = photoUriInput ?: "",
            date = System.currentTimeMillis()
        )
        viewModelScope.launch {
            lunchesRepository.insertLunch(newLunch)
        }
        resetInputs()
    }

    // ★削除機能を追加！
    fun deleteLunch(lunch: LunchEntity) {
        viewModelScope.launch {
            lunchesRepository.deleteLunch(lunch)
        }
    }

    // --- 5. Factory ---
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
