package com.example.cameray

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.cameray.databinding.ActivityMainBinding

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
    }

    private fun startCamera() {
        val preview: PreviewView = viewBinding.Preview
        cameraController = LifecycleCameraController(baseContext)
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        preview.controller = cameraController
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