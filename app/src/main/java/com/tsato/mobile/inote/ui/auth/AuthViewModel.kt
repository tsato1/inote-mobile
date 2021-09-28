package com.tsato.mobile.inote.ui.auth

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsato.mobile.inote.repositories.NoteRepository
import com.tsato.mobile.inote.util.Resource
import kotlinx.coroutines.launch

class AuthViewModel @ViewModelInject constructor(
    private val repository: NoteRepository
): ViewModel() {

    private val _registerStatus = MutableLiveData<Resource<String>>()
    val registerStatus: LiveData<Resource<String>> = _registerStatus

    fun register(email: String, password: String, repeatedPassword: String) {
        _registerStatus.postValue(Resource.loading(null))

        if (email.isEmpty() || password.isEmpty() || repeatedPassword.isEmpty()) {
            _registerStatus.postValue(Resource.error("Please fill out all the field", null))
            return
        }

        if (password != repeatedPassword) {
            _registerStatus.postValue(Resource.error("The passwords don't match", null))
            return
        }

        viewModelScope.launch {
            val result = repository.register(email, password)
            _registerStatus.postValue(result)
        }
    }

}