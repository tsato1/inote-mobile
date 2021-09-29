package com.tsato.mobile.inote.ui.notes

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.tsato.mobile.inote.data.local.entities.Note
import com.tsato.mobile.inote.repositories.NoteRepository
import com.tsato.mobile.inote.util.Event
import com.tsato.mobile.inote.util.Resource

class NotesViewModel @ViewModelInject constructor(
    private val repository: NoteRepository
): ViewModel() {

    private val _forceUpdate = MutableLiveData<Boolean>(false)

    private val _allNotes = _forceUpdate.switchMap {
        repository.getAllNotes().asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        MutableLiveData(Event(it)) // wrap the data(it) that we got from database around Event
    }
    val allNotes: LiveData<Event<Resource<List<Note>>>> = _allNotes

    fun syncAllNotes() = _forceUpdate.postValue(true)
}