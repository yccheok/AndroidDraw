package com.divyanshu.androiddraw

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.divyanshu.draw.activity.DrawingActivity
import com.divyanshu.draw.activity.DrawingActivity.Companion.INTENT_EXTRA_DRAWING_INFO
import com.divyanshu.draw.model.DrawingInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

private const val REQUEST_CODE_DRAW = 101
class MainActivity : AppCompatActivity() {

    lateinit var adapter: DrawAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        adapter = DrawAdapter(this, getFilesPath())
        findViewById<RecyclerView>(R.id.recycler_view).adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab_add_draw).setOnClickListener {
            val intent = Intent(this, DrawingActivity::class.java)

            intent.putExtra(INTENT_EXTRA_DRAWING_INFO, generateDrawingInfo())

            startActivityForResult(intent, REQUEST_CODE_DRAW)
        }
    }

    private fun generateDrawingInfo(): DrawingInfo {
        val path = File(getExternalFilesDir(null), "Android Draw")
        path.mkdirs()
        Log.e("path", path.toString())
        val fileName = UUID.randomUUID().toString() + ".png"
        val file = File(path, fileName)

        return DrawingInfo(
                0,
                fileName,
                file.absolutePath,
                0,
                0,
                0
        )
    }
    
    private fun getFilesPath(): ArrayList<String>{
        val resultList = ArrayList<String>()
        val path = File(getExternalFilesDir(null), "Android Draw");
        path.mkdirs()
        val imageList = path.listFiles()
        imageList?.forEach {
            resultList.add(it.absolutePath)
        }
        return resultList
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && resultCode == Activity.RESULT_OK) {
            when(requestCode){
                REQUEST_CODE_DRAW -> {
                    val imageInfo = data.getParcelableExtra<DrawingInfo>(DrawingActivity.INTENT_EXTRA_DRAWING_INFO)
                    if (imageInfo != null) {
                        updateRecyclerView(Uri.fromFile(File(imageInfo.path)))
                    }

                }
            }
        }
    }

    private fun updateRecyclerView(uri: Uri) {
        adapter.addItem(uri)
    }
}