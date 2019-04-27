package com.divyanshu.draw.activity

interface CancelOrSaveDialogListener {
    fun onCancel()
    fun onSave(byteArray: ByteArray)
}