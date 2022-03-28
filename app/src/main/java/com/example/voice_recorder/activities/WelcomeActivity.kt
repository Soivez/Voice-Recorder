package com.example.voice_recorder.activities

import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.voice_recorder.R
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.exceptions.VKAuthException
import com.vk.api.sdk.auth.VKScope


class WelcomeActivity: AppCompatActivity() {

    private lateinit var authLauncher: ActivityResultLauncher<Collection<VKScope>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (VK.isLoggedIn()) {
            MainActivity.startFrom(this)
            finish()
            return
        }
        setContentView(R.layout.activity_welcome)
        
        authLauncher = VK.login(this) { result : VKAuthenticationResult ->
            when (result) {
                is VKAuthenticationResult.Success -> onLogin()
                is VKAuthenticationResult.Failed -> onLoginFailed(result.exception)
            }
        }

        val loginBtn = findViewById<Button>(R.id.accept_button)
        loginBtn.setOnClickListener {
            authLauncher.launch(arrayListOf(VKScope.DOCS))
        }
    }

    private fun onLogin() {
        MainActivity.startFrom(this@WelcomeActivity)
        finish()
    }

    private fun onLoginFailed(exception: VKAuthException) {
        if (!exception.isCanceled) {
            val descriptionResource =
                if (exception.webViewError == WebViewClient.ERROR_HOST_LOOKUP) "Connection error"
                else "Unknown error"
            AlertDialog.Builder(this@WelcomeActivity)
                .setMessage(descriptionResource)
                .setPositiveButton(R.string.vk_retry) { _, _ ->
                    authLauncher.launch(arrayListOf(VKScope.WALL, VKScope.PHOTOS))
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

}