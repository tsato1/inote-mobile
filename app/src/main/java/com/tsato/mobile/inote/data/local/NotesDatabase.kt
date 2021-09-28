package com.tsato.mobile.inote.data.local

import androidx.room.Database
import androidx.room.TypeConverters

@Database(
    entities = [Note::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class NotesDatabase {

    abstract fun noteDao(): NoteDao

}