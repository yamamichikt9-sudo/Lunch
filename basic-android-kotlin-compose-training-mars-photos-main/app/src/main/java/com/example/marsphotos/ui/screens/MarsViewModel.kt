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
import com.example.marsphotos.data.LunchesRepository // ランチ用のリポジトリに変更
import com.example.marsphotos.data.LunchEntity
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * UI state for the Home screen
 */
sealed interface MarsUiState {
    data class Success(val photos: List<LunchEntity>) : MarsUiState
    object Error : MarsUiState
    object Loading : MarsUiState
}

class MarsViewModel(private val lunchesRepository: LunchesRepository) : ViewModel() {

    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    init {
        getLunchPhotos()
    }

    /**
     * データベースからランチのリストを取得して UI 状態を更新する
     */
    fun getLunchPhotos() {
        viewModelScope.launch {
            marsUiState = MarsUiState.Loading
            try {
                // flowをcollectしてリストを取得する
                lunchesRepository.getAllLunchesStream().collect { items ->
                    marsUiState = MarsUiState.Success(items.reversed())
                }
            } catch (e: IOException) {
                marsUiState = MarsUiState.Error
            } catch (e: Exception) {
                marsUiState = MarsUiState.Error
            }
        }
    }

    // HomeScreenからretryActionとして呼ばれるため、古い名前の関数も残すか、
    // HomeScreen側の呼び出しを getLunchPhotos に変えてください
    fun getMarsPhotos() = getLunchPhotos()

    /**
     * Factory for [MarsViewModel]
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                // 取得先を lunchesRepository に変更
                val lunchesRepository = application.container.lunchesRepository
                MarsViewModel(lunchesRepository = lunchesRepository)
            }
        }
    }
}