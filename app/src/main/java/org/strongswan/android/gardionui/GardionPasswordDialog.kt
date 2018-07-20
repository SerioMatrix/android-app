package org.strongswan.android.gardionui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.gardion_dialog_password_fragment.*
import org.strongswan.android.R

class GardionPasswordDialog: DialogFragment() {

    interface GardionPasswordDialogListener {
        fun onFisnishEditDialog(inputText: String)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView: View? = inflater?.inflate(R.layout.gardion_dialog_password_fragment, container)
        return rootView
    }

    override fun onStart() {
        super.onStart()
        password_dialog_unlock_button.setOnClickListener { takeTypedValueAndDIsmissDIalog() }
    }

    private fun takeTypedValueAndDIsmissDIalog() {
        val listener: GardionPasswordDialogListener = activity as GardionPasswordDialogListener
        listener.onFisnishEditDialog(password_dialog_editText.text.toString())
        this.dismiss()
    }
}