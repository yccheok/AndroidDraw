package com.divyanshu.draw.activity

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog

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