package com.example.pam

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
	companion object {
		const val CONTENT_TITLE = "PAM"
		const val CONTENT_TEXT = "It will disappear after 10 seconds"
		const val REQUEST_CODE_PERMISSIONS = 1000
		val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
	}

	private var notificationId = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		createNotificationChannel()
		setContentView(R.layout.activity_main)
	}

	fun sendNotification(view: View) {
		val builder = NotificationCompat.Builder(this, getString(R.string.channel_id))
			.setContentTitle(CONTENT_TITLE)
			.setContentText(CONTENT_TEXT)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setTimeoutAfter(10000)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
		with(NotificationManagerCompat.from(this)) {
			notify(notificationId, builder.build())
		}
		++notificationId
	}

	private fun createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val name = getString(R.string.channel_name)
			val descriptionText = getString(R.string.channel_description)
			val importance = NotificationManager.IMPORTANCE_HIGH

			NotificationChannel(getString(R.string.channel_id), name, importance).apply {
				description = descriptionText
			}.also { channel ->
				getSystemService(NotificationManager::class.java).apply {
					createNotificationChannel(channel)
				}
			}
		}
	}

	fun searchGoogle(view: View) {
		search_box.text.toString().also { searchValue ->
			if (searchValue.isNotBlank())
				Intent(
					Intent.ACTION_VIEW,
					Uri.parse("http://www.google.com/search?q=$searchValue")
				).also {
					startActivity(it)
				}
		}
	}

	fun openCamera(view: View?) {
		if (allPermissionsGranted()) {
			openCameraActivity()
		} else {
			ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
		}
	}

	private fun openCameraActivity() {
		val radioGroup = findViewById<RadioGroup>(R.id.camera_radio_group)
		val selectedCamera = when (radioGroup.checkedRadioButtonId) {
			R.id.back_camera_radio_button -> Cameras.BACK
			R.id.front_camera_radio_button -> Cameras.FRONT
			else -> return
		}

		Intent(this, CameraActivity::class.java).apply {
			putExtra(CameraActivity.CAMERA_SELECTOR, selectedCamera)
		}.also {
			startActivity(it)
		}
	}

	private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
		ContextCompat.checkSelfPermission(
			baseContext, it
		) == PackageManager.PERMISSION_GRANTED
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			REQUEST_CODE_PERMISSIONS -> {
				if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
					openCameraActivity()
				} else {
					Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
}
