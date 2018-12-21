package com.divyanshu.draw.activity

import android.arch.lifecycle.ViewModelProviders
import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import com.divyanshu.draw.R
import com.divyanshu.draw.model.DrawingViewModel
import com.divyanshu.draw.model.DrawingViewModelFactory
import kotlinx.android.synthetic.main.activity_drawing.*
import kotlinx.android.synthetic.main.color_palette_view.*
import java.io.ByteArrayOutputStream

class DrawingActivity : AppCompatActivity() {
    companion object {
        @JvmField val INTENT_EXTRA_BITMAP = "INTENT_EXTRA_BITMAP"
        @JvmField val INTENT_EXTRA_FILEPATH = "INTENT_EXTRA_FILEPATH"

        @JvmStatic
        fun isSupported(context: Context, width: Int, height: Int): Boolean {
            val resources = context.resources
            val w = resources.getDisplayMetrics().widthPixels;
            val h = resources.getDisplayMetrics().heightPixels;
            val resource = resources.getIdentifier("status_bar_height", "dimen", "android")
            var statusBarHeight = 0
            if (resource > 0) {
                statusBarHeight = context.resources.getDimensionPixelSize(resource)
            }

            return (width == w && height == (h - statusBarHeight)) || (width == (h - statusBarHeight) && height == w)
        }
    }

    private inner class BitmapObserver : Observer<Bitmap> {
        override fun onChanged(bitmap: Bitmap?) {
            draw_view.backgroundBitmap = bitmap
        }
    }

    private lateinit var drawingViewModel: DrawingViewModel
    private val bitmapObserver = BitmapObserver()

    override fun finish() {
        val bitmap = draw_view.getBitmapIfModified()

        if (bitmap != null) {
            val bStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream)
            val byteArray = bStream.toByteArray()
            val returnIntent = Intent()
            returnIntent.putExtra(INTENT_EXTRA_BITMAP, byteArray)
            setResult(Activity.RESULT_OK,returnIntent)
        }

        draw_view.savePaintOptions()

        super.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_drawing)

        val filepath = intent.getStringExtra(INTENT_EXTRA_FILEPATH);
        if (filepath != null) {
            drawingViewModel = ViewModelProviders.of(
                    this,
                    DrawingViewModelFactory(filepath)
            ).get(DrawingViewModel::class.java)

            drawingViewModel.bitmapLiveData.observe(this, bitmapObserver)
        }

        image_close_drawing.setOnClickListener {
            super.finish()
            overridePendingTransition(0, R.anim.slide_discard)
        }

        image_done_drawing.setOnClickListener {
            finish()
        }

        setUpDrawTools()

        colorSelector()

        setPaintAlpha()

        setPaintWidth()
    }

    private fun setUpDrawTools() {
        circle_view_opacity.setCircleRadius(100f)
        image_draw_eraser.setOnClickListener {
            draw_view.toggleEraser()
            toggleDrawTools(draw_tools,false)
            image_draw_eraser.isSelected = draw_view.isEraserOn
            image_draw_width.isSelected = false
            image_draw_opacity.isSelected = false
            image_draw_color.isSelected = false
        }
        image_draw_eraser.setOnLongClickListener {
            draw_view.clearCanvas()
            toggleDrawTools(draw_tools,false)
            true
        }
        image_draw_width.setOnClickListener {
            var showView = true
            if (draw_tools.translationY == (56).toPx){
                showView = true
            }else if (draw_tools.translationY == (0).toPx && seekBar_width.visibility == View.VISIBLE){
                showView = false
            }

            draw_view.offEraser()
            toggleDrawTools(draw_tools, showView)
            image_draw_eraser.isSelected = false
            image_draw_width.isSelected = showView
            image_draw_opacity.isSelected = false
            image_draw_color.isSelected = false

            circle_view_width.visibility = View.VISIBLE
            circle_view_opacity.visibility = View.GONE
            seekBar_width.visibility = View.VISIBLE
            seekBar_opacity.visibility = View.GONE
            draw_color_palette.visibility = View.GONE
        }
        image_draw_opacity.setOnClickListener {
            var showView = true
            if (draw_tools.translationY == (56).toPx){
                showView = true
            }else if (draw_tools.translationY == (0).toPx && seekBar_opacity.visibility == View.VISIBLE){
                showView = false
            }

            draw_view.offEraser()
            toggleDrawTools(draw_tools, showView)
            image_draw_eraser.isSelected = false
            image_draw_width.isSelected = false
            image_draw_opacity.isSelected = showView
            image_draw_color.isSelected = false
            
            circle_view_width.visibility = View.GONE
            circle_view_opacity.visibility = View.VISIBLE
            seekBar_width.visibility = View.GONE
            seekBar_opacity.visibility = View.VISIBLE
            draw_color_palette.visibility = View.GONE
        }
        image_draw_color.setOnClickListener {
            var showView = true
            if (draw_tools.translationY == (56).toPx){
                showView = true
            }else if (draw_tools.translationY == (0).toPx && draw_color_palette.visibility == View.VISIBLE){
                showView = false
            }

            draw_view.offEraser()
            toggleDrawTools(draw_tools, showView)
            image_draw_eraser.isSelected = false
            image_draw_width.isSelected = false
            image_draw_opacity.isSelected = false
            image_draw_color.isSelected = showView
            
            circle_view_width.visibility = View.GONE
            circle_view_opacity.visibility = View.GONE
            seekBar_width.visibility = View.GONE
            seekBar_opacity.visibility = View.GONE
            draw_color_palette.visibility = View.VISIBLE
        }
        image_draw_undo.setOnClickListener {
            draw_view.undo()
            toggleDrawTools(draw_tools,false)
        }
        image_draw_redo.setOnClickListener {
            draw_view.redo()
            toggleDrawTools(draw_tools,false)
        }
    }

    private fun toggleDrawTools(view: View, showView: Boolean = true) {
        if (showView){
            view.animate().translationY((0).toPx)
        }else{
            view.animate().translationY((56).toPx)
        }
    }

    private fun getColorCompat(colorResourceId: Int): Int {
        return ResourcesCompat.getColor(resources, colorResourceId,null)
    }

    private fun colorSelector() {
        image_color_black.setOnClickListener {
            val color = getColorCompat(R.color.color_black)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_black)
        }
        image_color_red.setOnClickListener {
            val color = getColorCompat(R.color.color_red)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_red)
        }
        image_color_yellow.setOnClickListener {
            val color = getColorCompat(R.color.color_yellow)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_yellow)
        }
        image_color_green.setOnClickListener {
            val color = getColorCompat(R.color.color_green)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_green)
        }
        image_color_blue.setOnClickListener {
            val color = getColorCompat(R.color.color_blue)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_blue)
        }
        image_color_pink.setOnClickListener {
            val color = getColorCompat(R.color.color_pink)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_pink)
        }
        image_color_brown.setOnClickListener {
            val color =  getColorCompat(R.color.color_brown)
            draw_view.setColor(color)
            circle_view_opacity.setColor(color)
            circle_view_width.setColor(color)
            scaleColorView(image_color_brown)
        }

        // Set alpha component to 0xFF.
        val color = draw_view.getColor() or (0xFF_00_00_00).toInt()
        if (color == getColorCompat(R.color.color_black)) {
            image_color_black.performClick()
        } else if (color == getColorCompat(R.color.color_red)) {
            image_color_red.performClick()
        } else if (color == getColorCompat(R.color.color_yellow)) {
            image_color_yellow.performClick()
        } else if (color == getColorCompat(R.color.color_green)) {
            image_color_green.performClick()
        } else if (color == getColorCompat(R.color.color_blue)) {
            image_color_blue.performClick()
        } else if (color == getColorCompat(R.color.color_pink)) {
            image_color_pink.performClick()
        } else if (color == getColorCompat(R.color.color_brown)) {
            image_color_brown.performClick()
        }
    }

    private fun scaleColorView(view: View) {
        //reset scale of all views
        image_color_black.scaleX = 1f
        image_color_black.scaleY = 1f

        image_color_red.scaleX = 1f
        image_color_red.scaleY = 1f

        image_color_yellow.scaleX = 1f
        image_color_yellow.scaleY = 1f

        image_color_green.scaleX = 1f
        image_color_green.scaleY = 1f

        image_color_blue.scaleX = 1f
        image_color_blue.scaleY = 1f

        image_color_pink.scaleX = 1f
        image_color_pink.scaleY = 1f

        image_color_brown.scaleX = 1f
        image_color_brown.scaleY = 1f

        //set scale of selected view
        view.scaleX = 1.5f
        view.scaleY = 1.5f
    }

    private fun setPaintWidth() {
        seekBar_width.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                draw_view.setStrokeWidth(progress.toFloat())
                circle_view_width.setCircleRadius(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBar_width.progress = draw_view.getStrokeWidth().toInt()
    }

    private fun setPaintAlpha() {
        seekBar_opacity.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                draw_view.setAlpha(progress)
                circle_view_opacity.setAlpha(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBar_opacity.progress = draw_view.getAlphaAsProgress()
    }

    private val Int.toPx: Float
        get() = (this * Resources.getSystem().displayMetrics.density)
}
