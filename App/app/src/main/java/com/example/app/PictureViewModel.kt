package com.example.app

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL
import java.util.*
import kotlin.random.Random

class MainActivityViewModel : ViewModel() {
    var mImage: Bitmap? = null
    var urlImage: URL? = null

    private val rand = Random(System.currentTimeMillis().toInt())

    fun getPicture(context: MainActivity, mImageView: ImageView?, urlField: TextView?, btn: Button?): Bitmap? {
        btn?.isEnabled = false
        var bitmap: Bitmap? = null

        val randomValue = rand.nextInt(10000, 99999)
        urlImage = URL("https://www.thiswaifudoesnotexist.net/example-$randomValue.jpg")
        urlField?.text = "thiswaifudoesnotexist.net/example-$randomValue.jpg"

        val result: Deferred<Bitmap?> = GlobalScope.async {
            urlImage?.toBitmap()
        }

        GlobalScope.launch(Dispatchers.Main) {
            bitmap = result.await()
            mImageView?.setImageURI(bitmap?.save(context))
            btn?.isEnabled = true
        }
        return bitmap
    }

    fun savePicture(context: MainActivity, bitmap: Bitmap?) {
        val filename = "anime_girl_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
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
        }
        Toast.makeText(context, context.getString(R.string.save_text), Toast.LENGTH_SHORT).show()
    }
}

fun URL.toBitmap(): Bitmap? {
    return try {
        BitmapFactory.decodeStream(openStream())
    } catch (e: IOException) {
        null
    }
}

fun Bitmap.save(context: Context): Uri? {
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