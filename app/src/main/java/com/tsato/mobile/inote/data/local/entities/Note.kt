package com.tsato.mobile.inote.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import java.util.*

@Entity(tableName = "notes")
data class Note(
    val title: String,
    val content: String,
    val date: Long,
    val owners: List<String>,
    val color: String,
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
    @Expose(deserialize = false, serialize = false) // this val will be ignored in Retrofit communication
    var isSynced: Boolean = false
)
