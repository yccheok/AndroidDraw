package com.divyanshu.draw.activity

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog

class CancelOrSaveDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(): CancelOrSaveDialogFragment {
            val cancelOrSaveDialogFragment = CancelOrSaveDialogFragment()
            return cancelOrSaveDialogFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arrayAdapter = CancelOrSaveArrayAdapter(requireContext())

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
                .setAdapter(arrayAdapter) { _, which ->
                    val activity = activity
                    if (activity is CancelOrSaveDialogListener) {
                        if (which == 0) {
                            (activity as CancelOrSaveDialogListener).onCancel()
                        } else if (which == 1) {
                            (activity as CancelOrSaveDialogListener).onSave()
                        }
                    }
                }

        return alertDialogBuilder.create()
    }
}