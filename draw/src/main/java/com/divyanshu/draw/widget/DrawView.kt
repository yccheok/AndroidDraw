package com.divyanshu.draw.widget

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.WindowManager
import java.util.*


class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val LIB_NAME = "com.divyanshu.androiddraw"
    private val UID = "069e4deb-69e5-405b-ac3c-8031629d3260"
    private val COLOR_KEY = "COLOR_KEY"
    private val STROKE_WIDTH_KEY = "STROKE_WIDTH_KEY"
    private val ALPHA_KEY = "ALPHA_KEY"

    private var mPaths = LinkedHashMap<MyPath, PaintOptions>()

    private var mLastPaths = LinkedHashMap<MyPath, PaintOptions>()
    private var mUndonePaths = LinkedHashMap<MyPath, PaintOptions>()

    private var mPaint = Paint()
    private var mPaintForGetBitmap = Paint()
    private var mPath = MyPath()
    private var mPaintOptions = PaintOptions()

    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f
    private var mIsStrokeWidthBarEnabled = false

    private var mRotateAngle = 0f

    private var mCroppedBackgroundBitmap: Bitmap? = null
    
    var backgroundBitmap: Bitmap? = null
        set(value) {
            if (value == null) {
                field = value
                mRotateAngle = 0f
                invalidate()
                return
            }

            val isRotateRequired = isRotateRequired(context, value.width, value.height)

            if (isRotateRequired) {
                val matrix = Matrix()

                val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                val rotation = display.rotation
                if (rotation == Surface.ROTATION_270) {
                    mRotateAngle = 90f
                    matrix.postRotate(mRotateAngle)

                } else {
                    mRotateAngle = -90f
                    matrix.postRotate(mRotateAngle)
                }
                field = Bitmap.createBitmap(value, 0, 0, value.width, value.height, matrix, true)
            } else {
                field = value
                mRotateAngle = 0f
            }
            invalidate()
        }

    var isEraserOn = false
        private set

    init {
        loadPaintOptions()

        mPaint.apply {
            color = mPaintOptions.color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mPaintOptions.strokeWidth
            isAntiAlias = true
        }
    }

    private fun isRotateRequired(context: Context, width: Int, height: Int): Boolean {
        val resources = context.resources
        val w = resources.getDisplayMetrics().widthPixels;
        val h = resources.getDisplayMetrics().heightPixels;
        if (width == w && height == h) {
            return false
        }
        return true
    }
    
    private fun p(context: Context): SharedPreferences {
        /*
         * Always use application context.
         */
        return context.applicationContext.getSharedPreferences(genPreferenceFilename(), Context.MODE_PRIVATE)
    }

    private fun genPreferenceFilename(): String {
        return String.format("%s_%s", LIB_NAME, UID)
    }

    private fun loadPaintOptions() {
        val p = p(context)
        mPaintOptions.color = p.getInt(COLOR_KEY, Color.BLACK)
        mPaintOptions.strokeWidth = p.getFloat(STROKE_WIDTH_KEY, 8f)
        mPaintOptions.alpha = p.getInt(ALPHA_KEY, 255)
    }

    fun savePaintOptions() {
        val p = p(context)
        val e = p.edit()
        e.putInt(COLOR_KEY, mPaintOptions.color)
        e.putFloat(STROKE_WIDTH_KEY, mPaintOptions.strokeWidth)
        e.putInt(ALPHA_KEY, mPaintOptions.alpha)
        e.apply()
    }

    fun undo() {
        if (mPaths.isEmpty() && mLastPaths.isNotEmpty()) {
            mPaths = mLastPaths.clone() as LinkedHashMap<MyPath, PaintOptions>
            mLastPaths.clear()
            invalidate()
            return
        }
        if (mPaths.isEmpty()) {
            return
        }
        val lastPath = mPaths.values.lastOrNull()
        val lastKey = mPaths.keys.lastOrNull()

        mPaths.remove(lastKey)
        if (lastPath != null && lastKey != null) {
            mUndonePaths[lastKey] = lastPath
        }
        invalidate()
    }

    fun redo() {
        if (mUndonePaths.keys.isEmpty()) {
            return
        }

        val lastKey = mUndonePaths.keys.last()
        addPath(lastKey, mUndonePaths.values.last())
        mUndonePaths.remove(lastKey)
        invalidate()
    }

    fun getColor(): Int {
        return mPaintOptions.color
    }

    fun setColor(newColor: Int) {
        @ColorInt
        val alphaColor = ColorUtils.setAlphaComponent(newColor, mPaintOptions.alpha)
        mPaintOptions.color = alphaColor
        if (mIsStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun getAlphaAsProgress(): Int {
        return (mPaintOptions.alpha*100)/255
    }

    fun setAlpha(progress: Int) {
        val alpha = (progress*255)/100
        mPaintOptions.alpha = alpha
        setColor(mPaintOptions.color)
    }

    fun getStrokeWidth(): Float {
        return mPaintOptions.strokeWidth
    }

    fun setStrokeWidth(newStrokeWidth: Float) {
        mPaintOptions.strokeWidth = newStrokeWidth
        if (mIsStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    // TODO: https://stackoverflow.com/questions/53909032/optimized-way-to-perform-canvas-draw-on-child-region-of-a-master-bitmap
    private fun getBitmap(): Bitmap {
        val resources = context.resources
        val screenWidth = resources.getDisplayMetrics().widthPixels;
        val screenHeight = resources.getDisplayMetrics().heightPixels;

        // Creating a larger master bitmap.
        val masterBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        val masterCanvas = Canvas(masterBitmap)
        masterCanvas.drawColor(Color.WHITE)

        // Creating a smaller child bitmap.
        val childBitmap = Bitmap.createBitmap(
                masterBitmap,
                (screenWidth-width) shr 1,
                (screenHeight-height) shr 1,
                width,
                height
        )

        // Perform drawing on smaller child bitmap.
        val childCanvas = Canvas(childBitmap)
        draw(childCanvas)

        // Copy the smaller child bitmap back to larger master bitmap.
        masterCanvas.drawBitmap(
                childBitmap,
                ((screenWidth-width) shr 1).toFloat(),
                ((screenHeight-height) shr 1).toFloat(),
                mPaintForGetBitmap
        )

        if (mRotateAngle == 0f) {
            return masterBitmap
        }

        val matrix = Matrix()
        matrix.postRotate(-mRotateAngle)
        return Bitmap.createBitmap(masterBitmap, 0, 0, masterBitmap.width, masterBitmap.height, matrix, true)
    }

    fun getRotatedBitmapIfModified(): Bitmap? {
        if (mPaths.isEmpty()) {
            return null
        }
        
        var bitmap = getBitmap()
        return bitmap
    }

    fun addPath(path: MyPath, options: PaintOptions) {
        mPaths[path] = options
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (backgroundBitmap != null) {
            if (mCroppedBackgroundBitmap == null) {
                val bitmap = backgroundBitmap;
                if (bitmap != null) {
                    mCroppedBackgroundBitmap = Bitmap.createBitmap(
                            bitmap,
                            (bitmap.width - width) shr 1,
                            (bitmap.height - height) shr 1,
                            width,
                            height
                    )
                }
            }

            if (mCroppedBackgroundBitmap != null) {
                // Reset alpha value.
                mPaint.alpha = 255
                canvas.drawBitmap(mCroppedBackgroundBitmap, 0f, 0f, mPaint)
            }
        }

        for ((key, value) in mPaths) {
            changePaint(value)
            canvas.drawPath(key, mPaint)
        }

        changePaint(mPaintOptions)
        canvas.drawPath(mPath, mPaint)
    }

    private fun changePaint(paintOptions: PaintOptions) {
        mPaint.color = if (paintOptions.isEraserOn) Color.WHITE else paintOptions.color
        mPaint.strokeWidth = paintOptions.strokeWidth
    }

    fun clearCanvas() {
        mLastPaths = mPaths.clone() as LinkedHashMap<MyPath, PaintOptions>
        mPath.reset()
        mPaths.clear()
        invalidate()
    }

    private fun actionDown(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mCurX = x
        mCurY = y
    }

    private fun actionMove(x: Float, y: Float) {
        mPath.quadTo(mCurX, mCurY, (x + mCurX) / 2, (y + mCurY) / 2)
        mCurX = x
        mCurY = y
    }

    private fun actionUp() {
        mPath.lineTo(mCurX, mCurY)

        // draw a dot on click
        if (mStartX == mCurX && mStartY == mCurY) {
            mPath.lineTo(mCurX, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY)
        }

        mPaths[mPath] = mPaintOptions
        mPath = MyPath()
        mPaintOptions = PaintOptions(mPaintOptions.color, mPaintOptions.strokeWidth, mPaintOptions.alpha, mPaintOptions.isEraserOn)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = x
                mStartY = y
                actionDown(x, y)
                mUndonePaths.clear()
            }
            MotionEvent.ACTION_MOVE -> actionMove(x, y)
            MotionEvent.ACTION_UP -> actionUp()
        }

        invalidate()
        return true
    }

    fun toggleEraser() {
        isEraserOn = !isEraserOn
        mPaintOptions.isEraserOn = isEraserOn
        invalidate()
    }

    fun offEraser() {
        isEraserOn = false
        mPaintOptions.isEraserOn = isEraserOn
        invalidate()
    }
}