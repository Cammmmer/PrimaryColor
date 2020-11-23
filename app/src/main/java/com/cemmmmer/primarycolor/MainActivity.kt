package com.cemmmmer.primarycolor

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        selectImageBtn.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && null != data) {
            launch {
                val result = generate(data)
                result?.first?.let {
                    colorView.setBackgroundColor(it)
                }
                result?.second?.let {
                    imageView.setImageBitmap(it)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private suspend fun generate(data: Intent): Pair<Int, Bitmap?>? = withContext(Dispatchers.IO) {
        try {
            val bitmap: Bitmap? = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
            // Core code
            val color = PrimaryColor().generate(bitmap)
            return@withContext Pair(color, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }


    override fun onDestroy() {
        super.onDestroy()
        imageView.setImageBitmap(null)
        cancel()
    }
}
