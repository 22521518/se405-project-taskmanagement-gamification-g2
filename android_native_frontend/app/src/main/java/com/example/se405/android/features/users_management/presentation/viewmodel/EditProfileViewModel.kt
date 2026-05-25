package com.example.se405.android.features.users_management.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.se405.android.core.authentication.data.AuthRepository
import kotlinx.coroutines.launch

class EditProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {
    var displayName by mutableStateOf("")
    var email by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)

    fun initData(initialName: String, initialEmail: String) {
        displayName = initialName
        email = initialEmail
    }

    fun update(onBack: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            authRepository.updateProfile(displayName, email).onSuccess {
                isSuccess = true
                onBack() // Quay lại sau khi lưu thành công
            }
            isLoading = false
        }
    }
}