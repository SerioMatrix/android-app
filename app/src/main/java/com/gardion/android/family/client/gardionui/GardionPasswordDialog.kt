package com.gardion.android.family.client.gardionui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.gardion_dialog_password_fragment.*
import com.gardion.android.family.client.R
import android.view.inputmethod.InputMethodManager
import com.gardion.android.family.client.utils.GardionUtils


class GardionPasswordDialog: DialogFragment() {

    interface GardionPasswordDialogListener {
        fun onFinishEditDialog(inputText: String)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView: View? = inflater?.inflate(R.layout.gardion_dialog_password_fragment, container)
        return rootView

    }

    override fun onStart() {
        super.onStart()
        GardionUtils.forceKeyboardOpen(activity)
        password_dialog_unlock_button.setOnClickListener { takeTypedValueAndDismissDialog() }
        password_dialog_cancel.setOnClickListener { cancelGoBack() }
    }

    private fun takeTypedValueAndDismissDialog() {
        val listener: GardionPasswordDialogListener = activity as GardionPasswordDialogListener
        listener.onFinishEditDialog(password_dialog_editText.text.toString())
        this.dismiss()
    }

    private fun cancelGoBack() {
        this.dismiss()
    }



}