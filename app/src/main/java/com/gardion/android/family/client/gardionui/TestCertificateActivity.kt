package com.gardion.android.family.client.gardionui

import android.content.AsyncTaskLoader
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.security.KeyChain
import android.security.KeyChainException
import android.util.Log
import com.gardion.android.family.client.R
import kotlinx.android.synthetic.main.activity_test_certificate.*
import kotlinx.coroutines.experimental.*
import java.security.cert.X509Certificate


// not used at the moment, will later be used to check if certificate was removed by user


class TestCertificateActivity : AppCompatActivity() {

    private var job = Job()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_certificate)

        button_test_certificate.setOnClickListener { testCertificate(this) }
    }


    private fun testCertificate(context: Context) {
        var loader = UserCertificateLoader(this, "Gardion User")
        loader.execute()
    }


}


private class UserCertificateLoader(private val context: Context, private val aliasUserCertificate: String) : AsyncTask<Void, Void, X509Certificate>() {
    override fun doInBackground(vararg params: Void): X509Certificate? {
        var chain: Array<X509Certificate>? = null
        try {
            chain = KeyChain.getCertificateChain(context, aliasUserCertificate)
        } catch (e: KeyChainException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return if (chain != null && chain.size > 0) {
            chain[0]
        } else null
    }

    override fun onPostExecute(result: X509Certificate?) {
        if (result != null) {
            Log.d("GARDION_CERTIFICATE", "found, all OK")
            //mUserCertEntry = TrustedCertificateEntry(aliasUserCertificate, result)
        } else {    /* previously selected certificate is not here anymore */
            Log.d("GARDION_CERTIFICATE", "not found. we will reload certificate")
           reloadUserCertificate()
        }
    }

    private fun reloadUserCertificate() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

