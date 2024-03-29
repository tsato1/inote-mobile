package com.tsato.mobile.inote.ui.notesdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsato.mobile.inote.repositories.NoteRepository
import com.tsato.mobile.inote.util.Event
import com.tsato.mobile.inote.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository
): ViewModel() {

    private val _addOwnerStatus = MutableLiveData<Event<Resource<String>>>()
    val addOwnerStatus: LiveData<Event<Resource<String>>> = _addOwnerStatus

    fun addOwnerToNote(owner: String, noteId: String) {
        _addOwnerStatus.postValue(Event(Resource.loading(null)))

        if (owner.isEmpty() || noteId.isEmpty()) {
            _addOwnerStatus.postValue(Event(Resource.error("The owner can't be empty", null)))
            return
        }

        viewModelScope.launch {
            val result = repository.addOwnerToNote(owner, noteId)
            _addOwnerStatus.postValue(Event(result))
        }
    }

    fun observeNoteById(noteId: String) = repository.observeNoteById(noteId)
}