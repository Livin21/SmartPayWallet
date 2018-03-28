/*
 * MIT License
 *
 * Copyright (c) 2018.  Livin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.lmntrx.android.smartpaywallet.payment.nfc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NdefRecord.createMime
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.lmntrx.android.library.livin.missme.ProgressDialog
import com.lmntrx.android.smartpaywallet.R
import com.lmntrx.android.smartpaywallet.payment.Wallet
import com.lmntrx.android.smartpaywallet.payment.WalletActivity

class BeamPayActivity : AppCompatActivity(), NfcAdapter.CreateNdefMessageCallback {

    private var mNfcAdapter: NfcAdapter? = null
    private var wallet: Wallet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beam_pay)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.show()



        Wallet.getWallet(this, object : Wallet.OnWalletFetchCompletedListener {
            override fun onComplete(wallet: Wallet?) {

                this@BeamPayActivity.wallet = wallet
                progressDialog.dismiss()

                if (mNfcAdapter == null){
                    Toast.makeText(this@BeamPayActivity, "This device doesn't support NFC. Switching to QR Cde mode.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@BeamPayActivity, WalletActivity::class.java)
                    startActivity(intent)
                    finish()
                }


                mNfcAdapter?.setNdefPushMessageCallback(this@BeamPayActivity, this@BeamPayActivity)

            }

            override fun onError(s: String?) {

            }
        })

    }



    override fun createNdefMessage(event: NfcEvent?): NdefMessage {

        val text = "Beam me up, Android!\n\n" +
                "Beam Time: " + System.currentTimeMillis()

        return NdefMessage(arrayOf(
                createMime("application/vnd.com.lmntrx.android.smartpaywallet", text.toByteArray())
                ,NdefRecord.createApplicationRecord("com.lmntrx.android.smartpaywallet")

        ))

    }

    override fun onResume() {
        super.onResume()
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            processIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // onResume gets called after this to handle the intent
        setIntent(intent)
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    fun processIntent(intent: Intent) {
        val rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES)
        // only one message sent during the beam
        val msg = rawMsgs[0] as NdefMessage
        // record 0 contains the MIME type, record 1 is the AAR, if present
        Toast.makeText(this, String(msg.records[0].payload), Toast.LENGTH_LONG).show()
    }
}
