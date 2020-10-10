package com.example.pam

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import java.io.File

class PhotoActivity : AppCompatActivity() {
	companion object {
		const val IMAGE_URI = "com.example.pam.IMAGE_URI"
	}

	private var imageUri: Uri? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photo)

		imageUri = intent.extras?.get(IMAGE_URI) as? Uri
		if (imageUri != null) {
			val imageView = findViewById<ImageView>(R.id.taken_photo)
			imageView.setImageURI(imageUri)
		} else {
			finish()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		if (imageUri != null) {
			val file = File(imageUri.toString())
			file.delete()
		}
	}
}