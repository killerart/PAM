package com.example.pam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
	companion object {
		const val CAMERA_SELECTOR = "com.example.pam.CAMERA_SELECTOR"
	}

	private var imageCapture: ImageCapture? = null
	private var cameraExecutor: ExecutorService? = null
	private lateinit var outputDirectory: File

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_camera)

		val selectedCamera = intent.extras?.get(CAMERA_SELECTOR) as? Cameras

		if (selectedCamera != null) {
			val cameraSelector = when (selectedCamera) {
				Cameras.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
				Cameras.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
			}
			outputDirectory = getOutputDirectory()
			startCamera(cameraSelector)
			cameraExecutor = Executors.newSingleThreadExecutor()
		} else {
			finish()
		}
	}

	private fun getOutputDirectory(): File {
		val mediaDir = externalMediaDirs.firstOrNull()?.let {
			File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
		}
		return if (mediaDir != null && mediaDir.exists())
			mediaDir else filesDir
	}

	private fun startCamera(cameraSelector: CameraSelector) {
		ProcessCameraProvider.getInstance(this).also { processCameraProviderFuture ->
			processCameraProviderFuture.addListener(Runnable {
				val cameraProvider: ProcessCameraProvider = processCameraProviderFuture.get()
				val preview = Preview.Builder()
					.build()
					.also {
						it.setSurfaceProvider(viewFinder.createSurfaceProvider())
					}
				imageCapture =
					ImageCapture.Builder().setTargetRotation(windowManager.defaultDisplay.rotation)
						.build()
				try {
					cameraProvider.unbindAll()

					cameraProvider.bindToLifecycle(
						this, cameraSelector, preview, imageCapture
					)

				} catch (exc: Exception) {
					Log.e("Camera", "Use case binding failed", exc)
				}

			}, ContextCompat.getMainExecutor(this))
		}
	}

	fun takePhoto(view: View?) {
		// Get a stable reference of the modifiable image capture use case
		val imageCapture = imageCapture ?: return

		// Create time-stamped output file to hold the image
		val photoFile = File(outputDirectory, System.currentTimeMillis().toString() + ".jpg")

		// Create output options object which contains file + metadata
		val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

		// Set up image capture listener, which is triggered after photo has
		// been taken
		imageCapture.takePicture(
			outputOptions,
			ContextCompat.getMainExecutor(this),
			object : ImageCapture.OnImageSavedCallback {
				override fun onError(exc: ImageCaptureException) {
					Log.e("Camera", "Photo capture failed: ${exc.message}", exc)
				}

				override fun onImageSaved(output: ImageCapture.OutputFileResults) {
					Intent(this@CameraActivity, PhotoActivity::class.java).apply {
						putExtra(PhotoActivity.IMAGE_URI, Uri.fromFile(photoFile))
					}.also {
						startActivity(it)
					}
				}
			}
		)
	}

	override fun onStop() {
		super.onStop()
		cameraExecutor?.shutdown()
	}

	override fun onDestroy() {
		super.onDestroy()
		cameraExecutor?.shutdown()
	}
}