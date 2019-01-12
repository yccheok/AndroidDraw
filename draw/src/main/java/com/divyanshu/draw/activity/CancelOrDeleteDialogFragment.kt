package com.divyanshu.draw.activity

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog

class CancelOrDeleteDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(): CancelOrDeleteDialogFragment {
            return CancelOrDeleteDialogFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arrayAdapter = com.divyanshu.draw.activity.CancelOrDeleteArrayAdapter(context)

        val alertDialogBuilder = AlertDialog.Builder(activity!!)
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