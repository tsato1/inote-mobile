package com.tsato.mobile.inote.ui.addeditnote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsato.mobile.inote.data.local.entities.Note
import com.tsato.mobile.inote.repositories.NoteRepository
import com.tsato.mobile.inote.util.Event
import com.tsato.mobile.inote.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val repository: NoteRepository
): ViewModel() {

    // using Event because we want to load the data from database once
    // if we don't use Event, data will be reloaded from database upon configuration change
    //      and the change the user is making will be lost
    private val _note = MutableLiveData<Event<Resource<Note>>>()
    val note: LiveData<Event<Resource<Note>>> = _note

    // GlobalScope because we don't want to limit the process of insertNote() within the
    // lifetiem of this viewmodel, but within the app lifetime.
    fun insertNote(note: Note) = GlobalScope.launch {
        repository.insertNote(note)
    }

    // returns the note from database
    fun getNoteById(noteId: String) = viewModelScope.launch {
        _note.postValue(Event(Resource.loading(null)))

        val note = repository.getNoteById(noteId)
        note?.let {
            _note.postValue(Event(Resource.success(it)))
        } ?: _note.postValue(Event(Resource.error("Note not found", null)))
    }
}