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


    private var mTask: ImageAsyncTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        selectImageBtn.setOnClickListener {
            val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && null != data) {
            if (mTask?.status == AsyncTask.Status.RUNNING) {
                mTask?.cancel(true)
            }
            mTask = ImageAsyncTask(this)
            mTask!!.execute(data.data)
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
        private var mPrimaryColor = PrimaryColor()
        private var mColor: Int = 0;

        override fun doInBackground(vararg data: Uri?): Bitmap {
            var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(mContext.get()?.contentResolver, data[0])
            mColor = mPrimaryColor.generate(bitmap)
            return bitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            super.onPostExecute(bitmap)
            mContext.get()?.imageView?.setImageBitmap(bitmap)
            mContext.get()?.colorView?.setBackgroundColor(mColor)
        }
    }
}
