package com.tsato.mobile.inote.data.remote

import com.tsato.mobile.inote.data.remote.requests.AccountRequest
import com.tsato.mobile.inote.data.remote.requests.AddOwnerRequest
import com.tsato.mobile.inote.data.remote.requests.DeleteNoteRequest
import com.tsato.mobile.inote.data.remote.responses.SimpleResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NoteApi {

    @POST("/register")
    suspend fun register(@Body registerRequest: AccountRequest): Response<SimpleResponse>

    @POST("/login")
    suspend fun login(@Body loginRequest: AccountRequest): Response<SimpleResponse>

    @POST("/addNote")
    suspend fun addNote(@Body note: Note): Response<ResponseBody>

    @POST("/deleteNote")
    suspend fun deleteNote(@Body deleteNoteRequest: DeleteNoteRequest): Response<ResponseBody>

    @POST("/addOwnerToNote")
    suspend fun addOwnerToNote(@Body addOwnerRequest: AddOwnerRequest): Response<SimpleResponse>

    @GET("/getNotes")
    suspend fun getNotes(): Response<List<Note>>

}