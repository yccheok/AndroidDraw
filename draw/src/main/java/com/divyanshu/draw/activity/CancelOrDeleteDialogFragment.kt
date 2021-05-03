package com.divyanshu.draw.activity

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog

class CancelOrDeleteDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(): CancelOrDeleteDialogFragment {
            return CancelOrDeleteDialogFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arrayAdapter = com.divyanshu.draw.activity.CancelOrDeleteArrayAdapter(requireContext())

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
                .setAdapter(arrayAdapter) { dialog, which ->
                    val activity = activity
                    if (activity is CancelOrDeleteDialogListener) {
                        if (which == 0) {
                            (activity as CancelOrDeleteDialogListener).onCancel()
                        } else if (which == 1) {
                            (activity as CancelOrDeleteDialogListener).onDelete()
                        }
                    }
                }

        return alertDialogBuilder.create()
    }
}