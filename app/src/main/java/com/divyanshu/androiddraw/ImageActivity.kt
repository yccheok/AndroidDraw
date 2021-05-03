package com.divyanshu.androiddraw

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val path = intent.getStringExtra(IMAGE_PATH)
        Glide.with(this).load(path).into(findViewById(R.id.image_view))
    }
}
