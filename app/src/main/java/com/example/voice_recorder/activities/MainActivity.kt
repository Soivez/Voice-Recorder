package com.example.voice_recorder.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voice_recorder.MyApp
import com.example.voice_recorder.R
import com.example.voice_recorder.databinding.ActivityMainBinding
import com.example.voice_recorder.utils.AudioService
import com.example.voice_recorder.utils.Element
import com.example.voice_recorder.utils.UserAdapter
import kotlinx.android.synthetic.main.list_item.*
import kotlinx.coroutines.*
import java.util.*

private var elements: ArrayList<Element> = arrayListOf()

@SuppressLint("SimpleDateFormat")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapt: UserAdapter
    private lateinit var audioService: AudioService
    private val elementDao = MyApp.instance.db.elementDao()
    companion object {
        private const val recordingPermission : String = Manifest.permission.RECORD_AUDIO
        private const val PERMISSION_CODE : Int = 42

        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
            return false
        }
        fun checkRecordingPermission(context : Context): Boolean {
            return if (ContextCompat.checkSelfPermission(context, recordingPermission) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                requestPermissions(context as Activity, arrayOf(recordingPermission), PERMISSION_CODE)
                false
            }
        }
        fun startFrom(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }

    }



    private fun createRecyclerView() {
        val myRecyclerView: RecyclerView = findViewById(R.id.myRecyclerView)
        val viewManager = LinearLayoutManager(this)
        myRecyclerView.apply {
            layoutManager = viewManager
            adapter = UserAdapter(elements) { element: Element, button: Button, chronometer : Chronometer ->
                audioService.onClickListenerPlayButton(element, button, chronometer)
            }
            adapt = myRecyclerView.adapter as UserAdapter
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        audioService = AudioService(lifecycle)
        val view = binding.root
        setContentView(view)
        binding.recordButton .setOnClickListener {
            audioService.onClickListenerRecordButton(it, this, adapt)
        }
        binding.refreshButton.setOnClickListener {
            if (isOnline(this)) {
                binding.progressBar.isVisible = true
                lifecycle.coroutineScope.launch(Dispatchers.IO) {
                    val newElements = MyApp.instance.fetchAudioFiles()
                    elementDao.nukeTable()
                    elementDao.insertALl(*newElements.toTypedArray())
                    withContext(Dispatchers.Main) {
                        binding.progressBar.isVisible = false
                        adapt.setData(newElements)
                        Toast.makeText(
                            this@MainActivity,
                            "Данные успешно обновлены\n",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Не удалось подключиться к серверу, проверьте соединение",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.progressBar.isVisible = true
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            if (savedInstanceState == null) {
                elements = elementDao.getAll() as ArrayList<Element>
            }
            withContext(Dispatchers.Main) {
                binding.progressBar.isVisible = false
                createRecyclerView()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList("elements", elements)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        elements = savedInstanceState.getParcelableArrayList("elements")!!
    }


}