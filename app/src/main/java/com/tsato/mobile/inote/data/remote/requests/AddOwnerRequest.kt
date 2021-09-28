package com.tsato.mobile.inote.data.remote.requests

data class AddOwnerRequest(
    val owner: String,
    val noteId: String
)