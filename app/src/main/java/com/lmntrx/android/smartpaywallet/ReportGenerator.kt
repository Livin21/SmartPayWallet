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

package com.lmntrx.android.smartpaywallet

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.lmntrx.android.library.livin.missme.ProgressDialog
import com.lmntrx.android.smartpaywallet.payment.Transaction
import kotlinx.android.synthetic.main.activity_report_generator.*

class ReportGenerator : AppCompatActivity() {

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_generator)


        progressDialog = ProgressDialog(this).apply {
            setMessage("Please Wait...")
            setCancelable(false)
        }

        progressDialog?.show()

        FirebaseFirestore.getInstance().collection("wallets").document(
                Preferences.getDocumentReference(this)
        ).collection("transactions").get().addOnCompleteListener {
            progressDialog?.dismiss()
            if (it.isSuccessful) {
                if (it.result.documents.size > 0) {
                    Toast.makeText(this, "${it.result.documents.size} ", Toast.LENGTH_LONG).show()
                    it.result.documents.forEach {
                        val transaction = Transaction()
                        transaction.setFromAddress(it.getString("fromAddress"))
                        transaction.setToAddress(it.getString("toAddress"))
                        transaction.setAmount(it.getDouble("amount"))
                        transaction.setWhen(it.getLong("when"))
                        reportTextView.text = reportTextView.text.toString().plus(
                                transaction.print() + "\n\n\n"
                        )
                    }
                } else reportTextView.text = getString(R.string.no_transactions_yet)
            } else {
                Toast.makeText(this, "Something Happened: ${it.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }


    }

    override fun onBackPressed() {
        progressDialog?.onBackPressed { super.onBackPressed() }
    }
}
