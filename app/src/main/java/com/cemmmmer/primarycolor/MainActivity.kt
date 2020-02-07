package com.cemmmmer.primarycolor

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import java.lang.ref.SoftReference


class MainActivity : AppCompatActivity() {


    private lateinit var mTask: ImageAsyncTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        selectImageBtn.setOnClickListener {
            val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)

        }
        mTask = ImageAsyncTask(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && null != data) {
            mTask.execute(data.data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        if (mTask?.status == AsyncTask.Status.RUNNING) {
            mTask?.cancel(true)
        }
        super.onDestroy()
    }

    class ImageAsyncTask(activity: MainActivity) : AsyncTask<Uri, Void, Bitmap>() {
        private var mContext: SoftReference<MainActivity> = SoftReference(activity)


        override fun doInBackground(vararg data: Uri?): Bitmap {
            return MediaStore.Images.Media.getBitmap(mContext.get()?.contentResolver, data[0])
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            super.onPostExecute(bitmap)
            mContext.get()?.imageView?.setImageBitmap(bitmap)
        }
    }
}
