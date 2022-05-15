package com.example.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import kotlinx.coroutines.*
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL
import java.util.*
import kotlin.random.*


class MainActivity : AppCompatActivity() {
    private var getBtn: Button? = null
    private var saveBtn: Button? = null
    private var urlField: TextView? = null
    private var bar: ProgressBar? = null

    private var mImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val context = this
        var urlImage: URL? = null

        getBtn = findViewById(R.id.button_new_pic);
        saveBtn = findViewById(R.id.button_save_pic)
        urlField = findViewById(R.id.text_view)
        var bitmap: Bitmap? = null

        val mImageView = findViewById<ImageView>(R.id.image_view)
        val rand = Random(System.currentTimeMillis().toInt())

        getBtn?.setOnClickListener {
            it.isEnabled = false
            bar?.visibility = View.VISIBLE

            val randomValue = rand.nextInt(10000, 99999)
            urlImage = URL("https://www.thiswaifudoesnotexist.net/example-$randomValue.jpg")
            println(urlImage)

            val result: Deferred<Bitmap?> = GlobalScope.async {
                urlImage?.toBitmap()
            }

            GlobalScope.launch(Dispatchers.Main) {
                bitmap = result.await()
                bitmap?.apply {
                    val savedUri: Uri? = saveToInternalStorage(context)
                    mImageView.setImageURI(savedUri)
                }

                urlField?.text = urlImage.toString()
                it.isEnabled = true
                bar?.visibility = View.INVISIBLE
            }
        }

        saveBtn?.setOnClickListener {
            it.isEnabled = false
            bar?.visibility = View.VISIBLE

            GlobalScope.launch(Dispatchers.Main) {
                mSaveMediaToStorage(bitmap)
                it.isEnabled = true
                bar?.visibility = View.INVISIBLE
            }
        }
    }

    private fun getPic(bitmap: Bitmap?) {
        val filename = "anime_girl_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mSaveMediaToStorage(bitmap: Bitmap?) {
        val filename = "anime_girl_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }
}

fun URL.toBitmap(): Bitmap? {
    return try {
        BitmapFactory.decodeStream(openStream())
    } catch (e: IOException) {
        null
    }
}

fun Bitmap.saveToInternalStorage(context: Context): Uri? {
    val wrapper = ContextWrapper(context)
    var file = wrapper.getDir("images", Context.MODE_PRIVATE)
    file = File(file, "${UUID.randomUUID()}.jpg")

    return try {
        val stream: OutputStream = FileOutputStream(file)
        compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
        Uri.parse(file.absolutePath)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}