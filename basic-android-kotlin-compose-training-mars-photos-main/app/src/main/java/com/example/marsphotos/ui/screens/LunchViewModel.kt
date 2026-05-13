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


    var nameInput by mutableStateOf("")
        private set
    var addressInput by mutableStateOf("")
        private set
    var phoneNumberInput by mutableStateOf("")
        private set
<<<<<<< HEAD
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

=======
>>>>>>> 0413e73437bb721a9254bc0ed012bb39db1fc8d2
    var selectedGenre by mutableStateOf("和食")
        private set
    var ratingInput by mutableStateOf(0f)
        private set
    var commentInput by mutableStateOf("")
        private set
    var photoUriInput by mutableStateOf<String?>(null)
        private set


    val lunchList = androidx.compose.runtime.mutableStateListOf<LunchEntity>()

    init {
        viewModelScope.launch {

            lunchesRepository.getAllLunchesStream().collect { items ->
                lunchList.clear()
                lunchList.addAll(items.reversed())
            }
        }
    }


    val canSave: Boolean
        get() = nameInput.isNotBlank()


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


    fun deleteLunch(lunch: LunchEntity) {
        viewModelScope.launch {
            lunchesRepository.deleteLunch(lunch)
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
