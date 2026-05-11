package com.example.marsphotos.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
// ※Repositoryなどはデータ担当の進捗に合わせて追加します

class LunchViewModel : ViewModel() {

    // --- 入力データ管理（ロジック担当の仕事） ---
    var nameInput by mutableStateOf("")
    var phoneInput by mutableStateOf("")
    var photoUriInput by mutableStateOf<String?>(null)
    var ratingInput by mutableStateOf(0f)

    // --- 入力チェックロジック ---
    val canSave: Boolean
        get() = nameInput.isNotBlank() &&
                photoUriInput != null &&
                phoneInput.all { it.isDigit() }

    // --- 更新用関数 ---
    fun updateName(newName: String) { nameInput = newName }
    fun updatePhone(newPhone: String) { phoneInput = newPhone }
}