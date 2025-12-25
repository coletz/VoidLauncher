package dev.coletz.voidlauncher.utils

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
import dev.coletz.voidlauncher.models.support.VoiceSearchLanguage
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList


class SpeechRecognizerManager private constructor(
    private val owner: Fragment,
    private val language: VoiceSearchLanguage?
): RecognitionListener {

    companion object {
        private var weakInstances: MutableMap<VoiceSearchLanguage?, WeakReference<SpeechRecognizerManager>> = mutableMapOf()

        fun getOrCreate(owner: Fragment, language: VoiceSearchLanguage?): SpeechRecognizerManager {
            val instance = weakInstances[language]?.get()
            if (instance != null) {
                return instance
            }
            val newInstance = SpeechRecognizerManager(owner, language)
            weakInstances[language] = WeakReference(newInstance)
            return newInstance
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

    private var speechResultListener: ((List<String>) -> Boolean)? = null

    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(owner.requireContext())

    private val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        .also {
            if (language?.id != null) {
                it.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.id)
            }
        }

    init {
        speechRecognizer.setRecognitionListener(this)
    }

    fun toggleMic(requestPermissionLauncher: ActivityResultLauncher<String>? = null) {
        if (isListening) {
            killInstances()
        } else {
            startMic(requestPermissionLauncher)
        }
    }

    private fun startMic(requestPermissionLauncher: ActivityResultLauncher<String>? = null) {
        checkPermission(requestPermissionLauncher) {
            speechRecognizer.startListening(speechRecognizerIntent)
        }
    }

    private fun killInstances() {
        speechRecognizer.destroy()
        weakInstances[language] = WeakReference(null)
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
        isListening = false
        speechResultListener?.invoke(emptyList())
    }

    override fun onError(errorCode: Int) {
        isListening = false
        "onError -> $errorCode".debug("SRMAN")
        speechResultListener?.invoke(emptyList())
    }

    override fun onResults(bundle: Bundle?) {
        "onResults".debug("SRMAN")
        val data: ArrayList<String> = bundle?.getStringArrayList(RESULTS_RECOGNITION) ?: return
        data.debugEach("SRMAN")
        if (speechResultListener?.invoke(data) == true) {
            killInstances()
        }
    }

    override fun onPartialResults(bundle: Bundle?) {
        "onPartialResults".debug("SRMAN")
        val data: ArrayList<String> = bundle?.getStringArrayList(RESULTS_RECOGNITION) ?: return
        data.debugEach("SRMAN")
        if (speechResultListener?.invoke(data) == true) {
            killInstances()
        }
    }

    override fun onEvent(p0: Int, bundle: Bundle?) {}

    fun setSpeechResultListener(speechResultListener: (List<String>) -> Boolean): SpeechRecognizerManager = apply {
        this.speechResultListener = speechResultListener
    }

}