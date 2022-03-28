package com.example.voice_recorder.utils

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.example.voice_recorder.MyApp
import com.example.voice_recorder.R
import com.example.voice_recorder.activities.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.*

class AudioService(private val lifecycle: Lifecycle) : Service(), MediaPlayer.OnPreparedListener {

    private var timeWhenPaused : Long = 0
    private var isRecording : Boolean = false
    private var playingButton : Button? = null
    private var playingChronometer : Chronometer? = null
    private var voicePlaying : Element? = null
    private var player : MediaPlayer? = null
    private var recorder : MediaRecorder? = null
    private var currentOutputFile : File? = null
    private val elementDao = MyApp.instance.db.elementDao()

    fun onClickListenerRecordButton(view: View, context: Context, adapt: UserAdapter) {
        val button : FloatingActionButton = view.findViewById(R.id.record_button)
        if (isRecording) {
            // STOP RECORDING
            stopRecording(context, adapt)
            isRecording = false
            button.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue))
        } else {
            // START RECORDING
            if (MainActivity.checkRecordingPermission(context)) {
                isRecording = true
                button.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
                startRecording()
            }
        }
    }


    private fun stopRecording(context: Context, adapt : UserAdapter) {
        val taskEditText = EditText(context)
        val recordPath = MyApp.instance.recordPath
        val fileName : String = MyApp.instance.dateFormat.format(Date())
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Save audio file")
            .setMessage("Enter the name of your recording, leave empty if you want it to be generated automatically")
            .setView(taskEditText)
            .setPositiveButton("Save") { _, _ ->
                recorder!!.stop()
                recorder!!.release()
                recorder = null
                val temp = taskEditText.text.toString()
                var resultFileName = fileName
                if (temp != "") {
                    resultFileName = temp
                }
                val time = Date()
                var path = "$recordPath/$resultFileName.mp3"
                if (File(path).exists()) {
                    path = "$recordPath/$resultFileName-" + MyApp.instance.dateFormat.format(Date()) + ".mp3"
                }
                Log.wtf("FILE", path)
                val element = Element(
                    adapt.itemCount + 1,
                    resultFileName,
                    time.toString(),
                    path
                )
                adapt.addElement(element)
                lifecycle.coroutineScope.launch(Dispatchers.IO) {
//                    if (MainActivity.isOnline(context)) {
//                        MyApp.instance.saveAudioFile(element)
//                    }
                    elementDao.insertALl(element)
                }
                currentOutputFile!!.renameTo(File(path))
            }
            .setNegativeButton("Delete") { _, _ ->
                recorder!!.stop()
                recorder!!.release()
                recorder = null
                currentOutputFile!!.delete()
            }
            .create()
        dialog.show()
    }

    private fun startRecording() {
        val fileName : String = MyApp.instance.dateFormat.format(Date())
        val recordPath = MyApp.instance.recordPath
        currentOutputFile = File("$recordPath/$fileName.mp3")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(currentOutputFile.toString())
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        }
        try {
            recorder!!.prepare()
        } catch (e: IOException) {
            Log.e("ERROR", "prepare() failed")
        }
        recorder!!.start()
    }

    private fun startPlaying(element: Element, button: Button, chronometer: Chronometer) {
        playingButton = button
        voicePlaying = element
        playingChronometer = chronometer
        changeButtonMode(playingButton!!)
        player = MediaPlayer().apply {
            setOnCompletionListener {
                    stopPlaying()

            }
            setDataSource(element.file)
            setOnPreparedListener(this@AudioService)
            prepareAsync()
        }
    }

    private fun stopPlaying() {
        playingChronometer!!.base = SystemClock.elapsedRealtime()
        playingChronometer!!.stop()
        player?.release()
        changeButtonMode(playingButton!!)
        player = null
        playingButton = null
        voicePlaying = null
        playingChronometer = null
        timeWhenPaused = 0

    }

    private fun pausePlaying() {
        timeWhenPaused = playingChronometer!!.base - SystemClock.elapsedRealtime()
        playingChronometer!!.stop()
        player!!.pause()
        changeButtonMode(playingButton!!)
    }

    private fun resumePlaying() {
        playingChronometer!!.base = SystemClock.elapsedRealtime() + timeWhenPaused
        playingChronometer!!.start()
        player!!.start()
        changeButtonMode(playingButton!!)
    }

    fun onClickListenerPlayButton(element: Element, button: Button, chronometer: Chronometer) {
        if (voicePlaying != null) {
            if (voicePlaying != element) {
                    stopPlaying()
                    startPlaying(element, button, chronometer)
            } else {
                if (player!!.isPlaying) {
                    pausePlaying()
                } else {
                    resumePlaying()
                }
            }
        } else {
            startPlaying(element, button, chronometer)
        }
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        playingChronometer!!.base = SystemClock.elapsedRealtime()
        playingChronometer!!.start()
        mediaPlayer.start()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        recorder?.release()
        recorder = null
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun changeButtonMode(button : Button) {
        if (button.tag == false) {
            button.setBackgroundResource(R.drawable.ic_pause_button)
            button.tag = true
        } else {
            button.setBackgroundResource(R.drawable.ic_play_button)
            button.tag = false
        }
    }


}