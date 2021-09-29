package com.tsato.mobile.inote.repositories

import android.app.Application
import com.tsato.mobile.inote.data.local.NoteDao
import com.tsato.mobile.inote.data.local.entities.LocallyDeletedNoteId
import com.tsato.mobile.inote.data.local.entities.Note
import com.tsato.mobile.inote.data.remote.NoteApi
import com.tsato.mobile.inote.data.remote.requests.AccountRequest
import com.tsato.mobile.inote.data.remote.requests.DeleteNoteRequest
import com.tsato.mobile.inote.util.Resource
import com.tsato.mobile.inote.util.checkForInternetConnection
import com.tsato.mobile.inote.util.networdBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteApi: NoteApi,
    private val context: Application
) {

    suspend fun insertNote(note: Note) {
        val response = try {
            noteApi.addNote(note)
        }
        catch (e: Exception) {
            null
        }

        if (response != null && response.isSuccessful) {
            noteDao.insertNote(note.apply { isSynced = true })
        }
        else {
            noteDao.insertNote(note) // meaning isSynced is false
        }
    }

    suspend fun insertNotes(notes: List<Note>) {
        notes.forEach { insertNote(it) }
    }

    suspend fun deleteNote(noteId: String) {
        val response = try {
            noteApi.deleteNote(DeleteNoteRequest(noteId))
        }
        catch (e: Exception) {
            null
        }

        noteDao.deleteNote(noteId)

        if (response == null || !response.isSuccessful) {
            noteDao.insertLocallyDeletedNoteId(LocallyDeletedNoteId(noteId))
        }
        else {
            deleteLocallyDeletedNoteId(noteId)
        }
    }

    suspend fun deleteLocallyDeletedNoteId(deletedNoteId: String) {
        noteDao.deleteLocallyDeletedNoteId(deletedNoteId)
    }

    suspend fun getNoteById(noteId: String) = noteDao.getNoteById(noteId)

    fun getAllNotes(): Flow<Resource<List<Note>>> {
        return networdBoundResource(
            query = {
                noteDao.getAllNotes()
            },
            fetch = {
                noteApi.getNotes()
            },
            saveFetchedResult = { response ->
                response.body()?.let {
                    // insert notes in database
                    insertNotes(it)
                }
            },
            shouldFetch = {
                checkForInternetConnection(context) // fetch data from api as long as there is internet connection
                // todo check timestamp to fetch or not
            }
        )
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.login(AccountRequest(email, password))

            if (response.isSuccessful && response.body()!!.successful) {
                Resource.success(response.body()?.message)
            }
            else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        }
        catch (e: Exception) {
            Resource.error("Cannot connect to the server. Check your internet connection", null)
        }
    }

    suspend fun register(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.register(AccountRequest(email, password))

            if (response.isSuccessful && response.body()!!.successful) {
                Resource.success(response.body()?.message)
            }
            else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        }
        catch (e: Exception) {
            Resource.error("Cannot connect to the server. Check your internet connection", null)
        }
    }

}