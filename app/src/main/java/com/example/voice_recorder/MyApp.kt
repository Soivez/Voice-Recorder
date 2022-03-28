package com.example.voice_recorder

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.voice_recorder.activities.MainActivity
import com.example.voice_recorder.utils.AppDatabase
import com.example.voice_recorder.utils.Element
import com.vk.api.sdk.*
import com.vk.sdk.api.docs.DocsService
import com.vk.sdk.api.docs.dto.DocsGetResponse
import com.vk.sdk.api.docs.dto.DocsGetType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyApp : Application() {

    lateinit var db : AppDatabase
    lateinit var recordPath : String
    val dateFormat = SimpleDateFormat("dd:MM:yyyy-hh:mm:ss")

    override fun onCreate() {
        super.onCreate()
        VK.addTokenExpiredHandler(tokenTracker)
        instance = this
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "posts-database").build()
        recordPath = getExternalFilesDir("/")?.absolutePath!!
    }

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            MainActivity.startFrom(this@MyApp)
        }
    }

    fun fetchAudioFiles() : ArrayList<Element> {
        var list : ArrayList<Element> = arrayListOf()
        VK.execute(
            DocsService().docsGet(type = DocsGetType.AUDIO),
            object : VKApiCallback<DocsGetResponse> {
                override fun fail(error: Exception) {
                    Log.e(MyApp::class.java.toString(), error.toString())
                }
                override fun success(result: DocsGetResponse) {
//                    Log.wtf("Files", result.count.toString())
//                    result.items.forEach {
//                        Log.wtf("Files", it.title + " " + it.ext + " " + it.preview!!.audioMsg!!.duration)
//                    }
                    var cnt = 0
                     list = result.items.map{
                         Element(cnt++, it.title, dateFormat.format(Date(it.date * 1000L)),
                             it.preview!!.audioMsg!!.linkMp3)

                     } as ArrayList<Element>
                }
            })
        return list
    }

    fun saveAudioFile(element : Element) {
//        var uploadUrl : String = ""
//        VK.execute(
//            DocsService().docsGetUploadServer(),
//            object : VKApiCallback<BaseUploadServer> {
//                override fun fail(error: Exception) {
//                    Log.e(MyApp::class.java.toString(), error.toString())
//                }
//
//                override fun success(result: BaseUploadServer) {
//                    uploadUrl = result.uploadUrl
//                }
//
//            }
//        )
//
//        val fileUploadCall = VKHttpPostCall.Builder()
//            .url(uploadUrl)
//            .args("audio", Uri.fromFile(File("$recordPath/${element.file}.mp3")), element.title)
//            .build()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl(uploadUrl)
//            .addConverterFactory(MoshiConverterFactory.create())
//            .build()
//
//        val service = retrofit.create(VoiceElements::class.java)
//
//        val file = File("$recordPath/${element.file}.mp3")
//
//        val filePart : MultipartBody.Part =
//            MultipartBody.Part.createFormData("file", file.name, RequestBody.create("audio/*".toMediaTypeOrNull(), file));
//
//        val call = service.postElement(filePart);
        TODO("Post file to server")
    }


    companion object {
        lateinit var instance: MyApp
            private set
    }

}