package com.tsato.mobile.inote.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tsato.mobile.inote.data.local.entities.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    @Query("DELETE FROM notes WHERE isSynced = 1")
    suspend fun deleteAllSyncedNotes()

    // LiveData is syncronous by default. don't make it suspend fun
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observeNoteById(noteId: String): LiveData<Note>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): Note?

    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getAllUnsyncedNotes(): List<Note>

}