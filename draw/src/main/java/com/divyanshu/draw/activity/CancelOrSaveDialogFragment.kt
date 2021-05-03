package com.divyanshu.draw.activity

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog

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
        val byteArray = arguments?.getByteArray(INTENT_EXTRA_BYTE_ARRAY)

        val arrayAdapter = CancelOrSaveArrayAdapter(requireContext())

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
                .setAdapter(arrayAdapter) { dialog, which ->
                    val activity = activity
                    if (activity is CancelOrSaveDialogListener) {
                        if (which == 0) {
                            (activity as CancelOrSaveDialogListener).onCancel()
                        } else if (which == 1) {
                            if (byteArray != null) {
                                (activity as CancelOrSaveDialogListener).onSave(byteArray)
                            }
                        }
                    }
                }

        return alertDialogBuilder.create()
    }
}