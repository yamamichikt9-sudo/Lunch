package com.example.marsphotos.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.data.LunchEntity // 自分で作ったEntityをインポート
import com.example.marsphotos.data.LunchesRepository
import kotlinx.coroutines.launch

class LunchViewModel(
    private val lunchesRepository: LunchesRepository // 仲介役を連れてくる
) : ViewModel() {

    // --- 1. UIの状態管理（すべての入力項目を網羅） ---
    var nameInput by mutableStateOf("")
    var addressInput by mutableStateOf("")
    var phoneInput by mutableStateOf("")
    var selectedGenre by mutableStateOf("和食")
    var ratingInput by mutableStateOf(0f)
    var commentInput by mutableStateOf("")
    var photoUriInput by mutableStateOf<String?>(null)

    // --- 2. バリデーションロジック ---
    val canSave: Boolean
        get() = nameInput.isNotBlank() &&
                photoUriInput != null &&
                phoneInput.all { it.isDigit() } &&
                phoneInput.isNotBlank()

    // --- 3. UIからの更新用関数 ---
    fun updateName(newName: String) { nameInput = newName }
    fun updateAddress(newAddress: String) { addressInput = newAddress }
    fun updatePhone(newPhone: String) { phoneInput = newPhone }
    fun updateRating(newRating: Float) { ratingInput = newRating }
    fun updateGenre(newGenre: String) { selectedGenre = newGenre }
    fun updateComment(newComment: String) { commentInput = newComment }
    fun updatePhoto(uri: String) { photoUriInput = uri }

    // --- 4. 保存アクション（ロジックの核心） ---
    fun saveLunch() {
        if (!canSave) return // 念のためのガードロジック

        // UIのバラバラなデータを「LunchEntity」という1つの塊にまとめる（詰め替え作業）
        val newLunch = LunchEntity(
            name = nameInput,
            address = addressInput,
            phoneNumber = phoneInput,
            rating = ratingInput,
            category = selectedGenre,
            comment = commentInput,
            photoUrl = photoUriInput ?: "",
            date = System.currentTimeMillis() // 登録した瞬間の時刻を記録
        )

        // 非同期処理（コルーチン）でデータベースへ保存を依頼する
        viewModelScope.launch {
            // ここでデータ担当のRepositoryを呼ぶことになります！
            // 例: lunchRepository.insertLunch(newLunch)
            println("保存しました: $newLunch") // テスト用のログ
        }
    }
}