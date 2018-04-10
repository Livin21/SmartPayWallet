package com.lmntrx.android.smartpaywallet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lmntrx.android.library.livin.missme.ProgressDialog
import com.lmntrx.android.smartpaywallet.payment.Wallet

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build()))
                            .build(),
                    Companion.SIGN_IN_REQUEST_CONST)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == Companion.SIGN_IN_REQUEST_CONST) {
            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this,"SignIn Success", Toast.LENGTH_LONG).show()
                createAndSaveWallet()
            } else {
                if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Request Cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "SigIn Failed. Please check your internet connection.", Toast.LENGTH_LONG).show()
                }
                val builder = AlertDialog.Builder(this)
                        .setMessage("Retry?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build()))
                                            .build(),
                                    Companion.SIGN_IN_REQUEST_CONST)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Exit") { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                builder.create().show()
            }
        }
    }

    private fun createAndSaveWallet() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.show()
        val db = FirebaseFirestore.getInstance()
        if (Preferences.getDocumentReference(this).isEmpty()) {
            db.collection("wallets")
                    .add(Wallet.createWallet(this))
                    .addOnSuccessListener { documentReference ->
                        progressDialog.dismiss()
                        Log.d("WALLET", "Success")
                        Preferences.saveDocumentReference(this, documentReference.id)
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        e -> e.printStackTrace()
                        progressDialog.dismiss()
                    }
        }
    }

    companion object {
        private const val SIGN_IN_REQUEST_CONST = 123
    }
}
