package com.divyanshu.draw.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import java.io.*
import java.util.concurrent.Executors

class DrawingViewModel(private val filepath: String?): ViewModel() {
    companion object {
        private const val TAG = "DrawingViewModel"
    }

    private val executor = Executors.newSingleThreadExecutor()
    val bitmapLiveData: MutableLiveData<Bitmap> = MutableLiveData()
    val drawingInfoLiveData: MutableLiveData<DrawingInfo> = MutableLiveData()

    init {
        if (filepath != null) {
            executor.execute {
                val bitmap = BitmapFactory.decodeFile(filepath)
                bitmapLiveData.postValue(bitmap)
            }
        }
    }

    fun saveAsync(drawingInfo: DrawingInfo, bitmap: Bitmap) {
        executor.execute {
            val _drawingInfo = save(drawingInfo, bitmap)
            // _drawingInfo can be null.
            drawingInfoLiveData.postValue(_drawingInfo)
        }
    }

    private fun save(drawingInfo: DrawingInfo, bitmap: Bitmap): DrawingInfo? {
        val file = File(drawingInfo.path)
        var outputStream: OutputStream? = null

        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "", e)
            return null
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "", e)
            }

            // Required?
            bitmap.recycle()
        }

        val imageWidth = bitmap.width
        val imageHeight = bitmap.height

        if (imageWidth <= 0 || imageHeight <= 0) {
            file.delete()
            return null
        }

        // Get image size in byte.
        val size: Long = File(drawingInfo.path).length()

        return DrawingInfo(
                drawingInfo.directoryCode,
                drawingInfo.name,
                drawingInfo.path,
                size,
                imageWidth,
                imageHeight
        )
    }
}