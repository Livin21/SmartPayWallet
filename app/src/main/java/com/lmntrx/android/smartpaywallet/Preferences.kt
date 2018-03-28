package com.lmntrx.android.smartpaywallet


import android.content.Context

object Preferences {


    fun getAuthToken(context: Context): String {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("API_AUTH_TOKEN", "")
    }

    fun saveAuthToken(context: Context, token: String) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString("API_AUTH_TOKEN", token).apply()
    }

    fun getPrivateKey(context: Context): String {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("WALLET_PRIVATE_KEY", "")
    }

    fun savePrivateKey(context: Context, key: String) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString("WALLET_PRIVATE_KEY", key).apply()
    }

    fun saveDocumentReference(context: Context, documentReferenceId: String) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString("DOCUMENT_REFERENCE_ID", documentReferenceId).apply()
    }


    fun getDocumentReference(context: Context): String {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString("DOCUMENT_REFERENCE_ID", "")
    }
}
