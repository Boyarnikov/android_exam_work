package com.example.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import kotlinx.coroutines.*
import androidx.lifecycle.ViewModelProvider


class MainActivity : AppCompatActivity() {
    private var getBtn: Button? = null
    private var saveBtn: Button? = null
    private var urlField: TextView? = null
    private var mImageView : ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val context = this
        val picViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        getBtn = findViewById(R.id.button_new_pic)
        saveBtn = findViewById(R.id.button_save_pic)
        urlField = findViewById(R.id.text_view)
        mImageView = findViewById(R.id.image_view)

        var bitmap: Bitmap? = null

        getBtn?.setOnClickListener {
            bitmap = picViewModel.getPicture(context, mImageView, urlField, getBtn)
        }

        saveBtn?.setOnClickListener {
            it.isEnabled = false

            GlobalScope.launch(Dispatchers.Main) {
                picViewModel.savePicture(context, bitmap)
                it.isEnabled = true
            }
        }
    }
}

