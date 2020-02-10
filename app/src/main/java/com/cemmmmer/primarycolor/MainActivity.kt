package com.cemmmmer.primarycolor

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    private var mJop: Job? = null

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
            mJop = GlobalScope.launch(Dispatchers.Main) {
                var bitmap: Bitmap? = null
                var color = 0
                withContext(Dispatchers.IO) {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
                    color = PrimaryColor().generate(bitmap)
                }
                imageView.setImageBitmap(bitmap)
                colorView.setBackgroundColor(color)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        mJop?.cancel()
        imageView.setImageBitmap(null)
    }
}
