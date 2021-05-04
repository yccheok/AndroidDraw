package com.divyanshu.draw.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.divyanshu.draw.R
import com.divyanshu.draw.databinding.ActivityDrawingBinding
import com.divyanshu.draw.model.DrawingViewModel
import com.divyanshu.draw.model.DrawingViewModelFactory
import com.divyanshu.draw.model.ImageInfo
import com.divyanshu.draw.widget.CircleView
import com.divyanshu.draw.widget.DrawView

class DrawingActivity : AppCompatActivity(), CancelOrDeleteDialogListener, CancelOrSaveDialogListener {

    companion object {
        @JvmField val INTENT_EXTRA_ORIGINAL_FILEPATH = "INTENT_EXTRA_ORIGINAL_FILEPATH"
        @JvmField val INTENT_EXTRA_IMAGE_INFO = "INTENT_EXTRA_IMAGE_INFO"

        @JvmField val RESULT_DELETE_OK = Activity.RESULT_FIRST_USER + 1;
        const val CANCEL_OR_DELETE_DIALOG_FRAGMENT = "CANCEL_OR_DELETE_DIALOG_FRAGMENT"
        const val CANCEL_OR_SAVE_DIALOG_FRAGMENT = "CANCEL_OR_SAVE_DIALOG_FRAGMENT"

        @JvmStatic
        fun isSupported(context: Context, width: Int, height: Int): Boolean {
            val resources = context.resources
            val w = resources.getDisplayMetrics().widthPixels;
            val h = resources.getDisplayMetrics().heightPixels;
            return (width == w && height == h) || (width == h && height == w)
        }
    }

    private inner class BitmapObserver : Observer<Bitmap> {
        override fun onChanged(bitmap: Bitmap?) {
            draw_view.backgroundBitmap = bitmap
        }
    }

    private inner class ImageInfoObserver : Observer<ImageInfo> {
        override fun onChanged(imageInfo: ImageInfo?) {
            if (imageInfo == null) {
                return
            }

            val returnIntent = Intent()

            returnIntent.putExtra(INTENT_EXTRA_IMAGE_INFO, imageInfo)
            if (originalFilepath != null) {
                returnIntent.putExtra(INTENT_EXTRA_ORIGINAL_FILEPATH, originalFilepath)
            }
            setResult(Activity.RESULT_OK, returnIntent)

            draw_view.savePaintOptions()
            super@DrawingActivity.finish()
        }
    }

    private lateinit var drawingViewModel: DrawingViewModel
    private var originalFilepath: String? = null
    private val bitmapObserver = BitmapObserver()
    private val imageInfoObserver = ImageInfoObserver()

    private lateinit var binding: ActivityDrawingBinding
    private lateinit var draw_view: DrawView
    private lateinit var image_draw_eraser: ImageView
    private lateinit var image_draw_width: ImageView
    private lateinit var image_draw_opacity: ImageView
    private lateinit var image_draw_color: ImageView
    private lateinit var circle_view_width: CircleView
    private lateinit var circle_view_opacity: CircleView
    private lateinit var seekBar_width: SeekBar
    private lateinit var seekBar_opacity: SeekBar
    private lateinit var draw_tools: ConstraintLayout
    private lateinit var image_close_drawing: ImageView
    private lateinit var image_done_drawing: ImageView
    private lateinit var image_draw_undo: ImageView
    private lateinit var image_draw_redo: ImageView
    private lateinit var draw_color_palette: LinearLayout
    private lateinit var image_color_black: ImageView
    private lateinit var image_color_red: ImageView
    private lateinit var image_color_yellow: ImageView
    private lateinit var image_color_green: ImageView
    private lateinit var image_color_blue: ImageView
    private lateinit var image_color_pink: ImageView
    private lateinit var image_color_brown: ImageView

    override fun onSave() {
        val imageInfo = intent.getParcelableExtra<ImageInfo>(INTENT_EXTRA_IMAGE_INFO)

        val bitmap = draw_view.getRotatedBitmapIfModified()

        if (imageInfo != null && bitmap != null) {
            drawingViewModel.imageInfoLiveData.observe(this, imageInfoObserver)
            drawingViewModel.saveAsync(imageInfo, bitmap)
        }
    }

    override fun onCancel() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_discard)
    }

    override fun onDelete() {
        val returnIntent = Intent()
        returnIntent.putExtra(INTENT_EXTRA_ORIGINAL_FILEPATH, originalFilepath)
        setResult(RESULT_DELETE_OK, returnIntent)
        super.finish()
    }

    fun done() {
        if (draw_view.isModified()) {
            onSave()
        } else {
            draw_view.savePaintOptions()
            super.finish()
        }
    }

    override fun finish() {
        if (draw_view.isModified()) {
            val cancelOrSaveDialogFragment = CancelOrSaveDialogFragment.newInstance()
            cancelOrSaveDialogFragment.show(supportFragmentManager, CANCEL_OR_SAVE_DIALOG_FRAGMENT)
        } else {
            draw_view.savePaintOptions()
            super.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDrawingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        draw_view = binding.drawView
        image_draw_eraser = binding.imageDrawEraser
        image_draw_width = binding.imageDrawWidth
        image_draw_opacity = binding.imageDrawOpacity
        image_draw_color = binding.imageDrawColor
        circle_view_width = binding.circleViewWidth
        circle_view_opacity = binding.circleViewOpacity
        seekBar_width = binding.seekBarWidth
        seekBar_opacity = binding.seekBarOpacity
        draw_tools = binding.drawTools
        image_close_drawing = binding.imageCloseDrawing
        image_done_drawing = binding.imageDoneDrawing
        image_draw_undo = binding.imageDrawUndo
        image_draw_redo = binding.imageDrawRedo
        draw_color_palette = binding.drawColorPalette.linearLayout
        image_color_black = binding.drawColorPalette.imageColorBlack
        image_color_red = binding.drawColorPalette.imageColorRed
        image_color_yellow = binding.drawColorPalette.imageColorYellow
        image_color_green = binding.drawColorPalette.imageColorGreen
        image_color_blue = binding.drawColorPalette.imageColorBlue
        image_color_pink = binding.drawColorPalette.imageColorPink
        image_color_brown = binding.drawColorPalette.imageColorBrown

        this.originalFilepath = intent.getStringExtra(INTENT_EXTRA_ORIGINAL_FILEPATH)

        drawingViewModel = ViewModelProvider(
                this,
                DrawingViewModelFactory(originalFilepath)
        ).get(DrawingViewModel::class.java)

        if (originalFilepath != null) {
            drawingViewModel.bitmapLiveData.observe(this, bitmapObserver)
        }

        image_close_drawing.setOnClickListener {
            if (originalFilepath == null) {
                // Cancel or save.
                finish()
            } else {
                val cancelOrDeleteDialogFragment = CancelOrDeleteDialogFragment.newInstance()
                cancelOrDeleteDialogFragment.show(supportFragmentManager, CANCEL_OR_DELETE_DIALOG_FRAGMENT)
            }
        }

        image_done_drawing.setOnClickListener {
            done()
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
