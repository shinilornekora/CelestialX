package com.example.celestialx.data.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.camera.video.Recording
import androidx.camera.view.PreviewView
import com.example.celestialx.R

class CameraHelper(private val context: Context) {

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var previewView: PreviewView? = null

    fun takePhoto() {
        log("Attempting to take a photo.")

        val imageCapture = imageCapture ?: run {
            errorLog("ImageCapture is not initialized")
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        log("Basic settings set.")

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    errorLog("Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    log(msg)
                }
            }
        )

        log("Photo process completed.")
    }

    fun captureVideo() {
        log("Toggling the video state!")

        val videoCapture = this.videoCapture ?: run {
            errorLog("Video capture does not exist.")
            return
        }

        val curRecording = recording

        if (curRecording != null) {
            curRecording.stop()
            recording = null
            log("Video is stopped.")

            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PermissionChecker.PERMISSION_GRANTED) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            log(msg)
                        } else {
                            recording?.close()
                            recording = null
                            errorLog("Video capture ends with error: ${recordEvent.error}")
                        }
                    }
                }
            }
    }

    fun startCamera(viewFinder: PreviewView) {
        previewView = viewFinder
        log("Starting the camera.")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView?.surfaceProvider)
                }
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST, FallbackStrategy.higherQualityOrLowerThan(Quality.SD)))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)
            imageCapture = ImageCapture.Builder().build()

            log("All basic variables set, time to bind.")

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as AppCompatActivity, cameraSelector, preview, imageCapture, videoCapture
                )
                log("Camera has started.")
            } catch (exc: Exception) {
                errorLog("Use case binding failed")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        previewView?.let {
            startCamera(it)
        }
    }

    companion object {
        private const val TAG = "CameraHelper"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        fun log(message: String) {
            Log.d(TAG, message)
        }

        fun errorLog(message: String) {
            Log.e(TAG, message)
        }
    }
}
