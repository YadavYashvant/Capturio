package com.example.cameray

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.cameray.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var cameraController: LifecycleCameraController

    //private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if(!hasPermissions(baseContext)) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        }else{
            startCamera()
        }

        viewBinding.captureBtn.setOnClickListener {

            viewBinding.animationview.playAnimation()


            viewBinding.captureBtn.animate().apply {
                duration = 500
                translationX(40f)
                translationX(40f)
            }.withEndAction{
                viewBinding.captureBtn.animate().apply {
                    duration = 500
                    translationX(-40f)
                    translationX(-40f)
                }.start()
            }

            takePhoto()

        }

        viewBinding.animationview.setOnClickListener {

            viewBinding.animationview.playAnimation()

            takePhoto()
        }

    }

    private fun startCamera() {
        val preview: PreviewView = viewBinding.Preview
        cameraController = LifecycleCameraController(baseContext)
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        preview.controller = cameraController

        viewBinding.flipbtn.setOnClickListener {

            //Button flip animation
            viewBinding.flipbtn.animate().apply {
                duration = 1000
                rotationYBy(180f)
            }.withEndAction {
                viewBinding.flipbtn.animate().apply {
                    duration = 1000
                    translationZ(34f)
                }.start()
            }

            if(cameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            }
            else{
                cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }
        }
    }

    private fun takePhoto() {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.UK)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Capturio-Images")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues)
            .build()

        cameraController.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object: ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}",exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    //val msg = "Photo captured at: ${output.savedUri}"
                    //Toast.makeText(baseContext,msg, Toast.LENGTH_SHORT).show()
                    //Log.d(TAG,msg)
                }
            }
        )
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->

            var permissionGranted = true
            permissions.entries.forEach {
                if(it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if(!permissionGranted) {
                Toast.makeText(this,"Please grant permissions",Toast.LENGTH_LONG).show()
            }
            else{
                startCamera()
            }

        }

    companion object {
        private const val TAG = "CameraY"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun hasPermissions(context: Context) = Companion.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

}