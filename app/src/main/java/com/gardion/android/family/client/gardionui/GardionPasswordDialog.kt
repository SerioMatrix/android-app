package com.gardion.android.family.client.gardionui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.gardion_dialog_password_fragment.*
import com.gardion.android.family.client.R

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
        password_dialog_unlock_button.setOnClickListener { takeTypedValueAndDismissDIalog() }
    }

    private fun takeTypedValueAndDismissDIalog() {
        val listener: GardionPasswordDialogListener = activity as GardionPasswordDialogListener
        listener.onFinishEditDialog(password_dialog_editText.text.toString())
        this.dismiss()
    }
}