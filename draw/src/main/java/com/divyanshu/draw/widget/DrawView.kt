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
    private var mPath = MyPath()
    private var mPaintOptions = PaintOptions()

    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f
    private var mIsSaving = false
    private var mIsStrokeWidthBarEnabled = false

    var backgroundBitmap: Bitmap? = null
        set(value) {
            if (value == null) {
                field = value
                invalidate()
                return
            }

            val isRotateRequired = isRotateRequired(context, value.width, value.height)

            if (isRotateRequired) {
                val matrix = Matrix()

                val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                val rotation = display.rotation
                if (rotation == Surface.ROTATION_270) {
                    matrix.postRotate(90f)
                } else {
                    matrix.postRotate(-90f)
                }
                field = Bitmap.createBitmap(value, 0, 0, value.width, value.height, matrix, true)
            } else {
                field = value
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
        val resource = resources.getIdentifier("status_bar_height", "dimen", "android")
        var statusBarHeight = 0
        if (resource > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resource)
        }

        if (width == w && height == (h - statusBarHeight)) {
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

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        mIsSaving = true
        draw(canvas)
        mIsSaving = false
        return bitmap
    }

    fun getBitmapIfModified(): Bitmap? {
        if (mPaths.isEmpty()) {
            return null
        }
        return getBitmap()
    }

    fun addPath(path: MyPath, options: PaintOptions) {
        mPaths[path] = options
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (backgroundBitmap != null) {
            // Reset alpha value.
            mPaint.alpha = 255
            canvas.drawBitmap(backgroundBitmap, 0f, 0f, mPaint)
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