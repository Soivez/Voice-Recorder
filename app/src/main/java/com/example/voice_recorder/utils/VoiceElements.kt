package com.example.voice_recorder.utils

import com.example.voice_recorder.utils.Element
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface VoiceElements {

        @GET("/posts/")
        suspend fun listElements() : Response<List<Element>>

        @Multipart
        @POST("/posts/")
        suspend fun postElement(@Part multipart: MultipartBody.Part) : Response<Element>

        @DELETE("posts/{id}")
        suspend fun deleteElement(@Path("id") id : Int) : Response<Element>
}