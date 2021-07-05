package com.divyanshu.draw.model

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.*
import java.security.MessageDigest
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

        val checksum = checksumInBase64(drawingInfo.path)

        return DrawingInfo(
                drawingInfo.directoryCode,
                drawingInfo.name,
                drawingInfo.path,
                size,
                checksum,
                imageWidth,
                imageHeight
        )
    }

    @Throws(Exception::class)
    private fun createChecksum(path: String): ByteArray {
        var fis: InputStream? = null
        return try {
            fis = FileInputStream(path)
            val buffer = ByteArray(8*1024)
            val complete: MessageDigest = MessageDigest.getInstance("MD5")
            var numRead: Int
            while (fis.read(buffer).also { numRead = it } != -1) {
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead)
                }
            }
            complete.digest()
        } finally {
            fis?.close()
        }
    }

    private fun checksumInBase64(path: String): String? {
        return try {
            val b = createChecksum(path)
            Base64.encodeToString(b, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            null
        }
    }
}