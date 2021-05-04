package com.divyanshu.draw.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.*
import java.util.concurrent.Executors

class DrawingViewModel(private val filepath: String?): ViewModel() {
    companion object {
        private const val TAG = "DrawingViewModel"
    }

    private val executor = Executors.newSingleThreadExecutor()
    val bitmapLiveData: MutableLiveData<Bitmap> = MutableLiveData()
    val imageInfoLiveData: MutableLiveData<ImageInfo> = MutableLiveData()

    init {
        if (filepath != null) {
            executor.execute {
                val bitmap = BitmapFactory.decodeFile(filepath)
                bitmapLiveData.postValue(bitmap)
            }
        }
    }

    fun saveAsync(imageInfo: ImageInfo, bitmap: Bitmap) {
        executor.execute {
            val imageInfo = save(imageInfo, bitmap)
            if (imageInfo != null) {
                imageInfoLiveData.postValue(imageInfo)
            }
        }
    }

    private fun save(imageInfo: ImageInfo, bitmap: Bitmap): ImageInfo? {
        val file = File(imageInfo.path)
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
        val size: Long = File(imageInfo.path).length()
        
        return ImageInfo(
                imageInfo.directoryCode,
                imageInfo.name,
                imageInfo.path,
                size,
                imageWidth,
                imageHeight
        )
    }
}