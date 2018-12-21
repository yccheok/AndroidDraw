package com.divyanshu.draw.model

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.concurrent.Executors

class DrawingViewModel(private val filepath: String): ViewModel() {
    private val executor = Executors.newSingleThreadExecutor()
    val bitmapLiveData: MutableLiveData<Bitmap> = MutableLiveData()

    init {
        executor.execute {
            val bitmap = BitmapFactory.decodeFile(filepath)
            bitmapLiveData.postValue(bitmap)
        }
    }
}