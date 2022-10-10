package com.mysticraccoon.runningappyt.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mysticraccoon.runningappyt.R

class CancelTrackingDialog: DialogFragment() {

    private var yesListener: (()->Unit)? = null

    fun setYesListener(listener: () -> Unit){
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the run?")
            .setMessage("Are you sure you want to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                yesListener?.invoke()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

}