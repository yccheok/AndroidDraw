package com.divyanshu.draw.activity

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog

class CancelOrSaveDialogFragment : DialogFragment() {
    companion object {
        const val INTENT_EXTRA_BYTE_ARRAY = "INTENT_EXTRA_BYTE_ARRAY"

        fun newInstance(byteArray: ByteArray): CancelOrSaveDialogFragment {
            val cancelOrSaveDialogFragment = CancelOrSaveDialogFragment()
            val arguments = Bundle()
            arguments.putByteArray(INTENT_EXTRA_BYTE_ARRAY, byteArray)
            cancelOrSaveDialogFragment.arguments = arguments
            return cancelOrSaveDialogFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments
        val byteArray = arguments!!.getByteArray(INTENT_EXTRA_BYTE_ARRAY)

        val arrayAdapter = com.divyanshu.draw.activity.CancelOrSaveArrayAdapter(context)

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
                .setAdapter(arrayAdapter) { dialog, which ->
                    val activity = activity
                    if (activity is CancelOrSaveDialogListener) {
                        if (which == 0) {
                            (activity as CancelOrSaveDialogListener).onCancel()
                        } else if (which == 1) {
                            (activity as CancelOrSaveDialogListener).onSave(byteArray)
                        }
                    }
                }

        return alertDialogBuilder.create()
    }
}