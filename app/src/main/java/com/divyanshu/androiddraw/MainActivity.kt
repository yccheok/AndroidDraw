package com.divyanshu.androiddraw

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.divyanshu.draw.activity.DrawingActivity
import com.divyanshu.draw.activity.DrawingActivity.Companion.INTENT_EXTRA_DRAWING_INFO
import com.divyanshu.draw.model.DrawingInfo
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

private const val REQUEST_CODE_DRAW = 101
private const val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102
class MainActivity : AppCompatActivity() {

    lateinit var adapter: DrawAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
        }else{
            adapter = DrawAdapter(this,getFilesPath())
            recycler_view.adapter = adapter
        }
        fab_add_draw.setOnClickListener {
            val intent = Intent(this, DrawingActivity::class.java)

            intent.putExtra(INTENT_EXTRA_DRAWING_INFO, generateDrawingInfo())

            startActivityForResult(intent, REQUEST_CODE_DRAW)
        }
    }

    private fun generateDrawingInfo(): DrawingInfo {
        val path = File(getExternalFilesDir(null), "Android Draw")
        path.mkdirs()
        Log.e("path", path.toString())
        val fileName = UUID.randomUUID().toString().toLowerCase() + ".png"
        val file = File(path, fileName)

        return DrawingInfo(
                0,
                fileName,
                file.absolutePath,
                0,
                null,
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
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    adapter = DrawAdapter(this,getFilesPath())
                    recycler_view.adapter = adapter
                }else{
                    finish()
                }
                return
            }
            else -> {}
        }
    }
    
    private fun updateRecyclerView(uri: Uri) {
        adapter.addItem(uri)
    }
}