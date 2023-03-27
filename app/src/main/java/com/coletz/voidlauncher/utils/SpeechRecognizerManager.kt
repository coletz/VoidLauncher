package com.coletz.voidlauncher.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.RESULTS_RECOGNITION
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList


class SpeechRecognizerManager private constructor(private val owner: Fragment): RecognitionListener {

    companion object {
        private var weakInstance: WeakReference<SpeechRecognizerManager> = WeakReference(null)

        fun getOrCreate(owner: Fragment): SpeechRecognizerManager {
            val instance = weakInstance.get()
            if (instance == null) {
                weakInstance = WeakReference(SpeechRecognizerManager(owner))
            }
            return requireNotNull(weakInstance.get())
        }

        fun errorMissingPermission(context: Context) {
            AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage("Audio recording permission is mandatory for using microphone input")
                .setNeutralButton(android.R.string.ok, null)
                .show()
        }
    }

    private var isListening: Boolean = false

    private var speechResultListener: ((ArrayList<String>) -> Boolean)? = null

    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(owner.requireContext())

    private val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

    init {
        speechRecognizer.setRecognitionListener(this)
    }

    fun toggleMic(requestPermissionLauncher: ActivityResultLauncher<String>? = null) {
        if (isListening) {
            killInstance()
        } else {
            startMic(requestPermissionLauncher)
        }
    }

    private fun startMic(requestPermissionLauncher: ActivityResultLauncher<String>? = null) {
        checkPermission(requestPermissionLauncher) {
            speechRecognizer.startListening(speechRecognizerIntent)
        }
    }

    private fun killInstance() {
        speechRecognizer.destroy()
        weakInstance = WeakReference(null)
    }

    private fun checkPermission(requestPermissionLauncher: ActivityResultLauncher<String>? = null, ifPermissionAlreadyGranted: () -> Unit) {
        val permission = Manifest.permission.RECORD_AUDIO
        when {
            checkSelfPermission(owner.requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                ifPermissionAlreadyGranted()
            }
            shouldShowRequestPermissionRationale(owner.requireActivity(), permission) -> {
                AlertDialog.Builder(owner.requireContext())
                    .setTitle("Recording permission")
                    .setMessage("Audio recording permission is mandatory for using microphone input")
                    .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissionLauncher?.launch(permission) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            else -> {
                requestPermissionLauncher?.launch(permission)
            }
        }
    }

    override fun onReadyForSpeech(bundle: Bundle?) {
        isListening = true
        "onReadyForSpeech".debug("SRMAN")
    }

    override fun onBeginningOfSpeech() {
        "onBeginningOfSpeech".debug("SRMAN")
    }

    override fun onRmsChanged(p0: Float) {}

    override fun onBufferReceived(p0: ByteArray?) {}

    override fun onEndOfSpeech() {
        "onEndOfSpeech".debug("SRMAN")
    }

    override fun onError(errorCode: Int) {
        isListening = false
        "onError -> $errorCode".debug("SRMAN")
    }

    override fun onResults(bundle: Bundle?) {
        "onResults".debug("SRMAN")
        val data: ArrayList<String> = bundle?.getStringArrayList(RESULTS_RECOGNITION) ?: return
        data.debugEach("SRMAN")
        if (speechResultListener?.invoke(data) == true) {
            killInstance()
        }
    }

    override fun onPartialResults(bundle: Bundle?) {
        "onPartialResults".debug("SRMAN")
        val data: ArrayList<String> = bundle?.getStringArrayList(RESULTS_RECOGNITION) ?: return
        data.debugEach("SRMAN")
        if (speechResultListener?.invoke(data) == true) {
            killInstance()
        }
    }

    override fun onEvent(p0: Int, bundle: Bundle?) {}

    fun setSpeechResultListener(speechResultListener: (ArrayList<String>) -> Boolean): SpeechRecognizerManager = apply {
        this.speechResultListener = speechResultListener
    }

}