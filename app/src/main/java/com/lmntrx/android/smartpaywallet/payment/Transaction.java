package com.lmntrx.android.smartpaywallet.payment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lmntrx.android.smartpaywallet.Preferences;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

/***
 * Created by Livin Mathew <livin@acoustike.com> on 9/3/18.
 */


public class Transaction implements Serializable {
    private String fromAddress;
    private String toAddress;
    private Long when;
    private Double amount;

    private Wallet toWallet;

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
        Wallet.getWallet(fromAddress, new Wallet.OnWalletFetchCompletedListener() {
            @Override
            public void onComplete(Wallet wallet) {
            }

            @Override
            public void onError(String s) {
                Log.d("Wallet Fetch", s);
            }
        });
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
        Wallet.getWallet(toAddress, new Wallet.OnWalletFetchCompletedListener() {
            @Override
            public void onComplete(Wallet wallet) {
                toWallet = wallet;
            }

            @Override
            public void onError(String s) {
                Log.d("Wallet Fetch", s);
            }
        });
    }

    public void setWhen(Long when) {
        this.when = when;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    void execute(final Context context, final Wallet.OnTransactionCompleteListener onTransactionCompleteListener) {

        Wallet.getWallet(toAddress, new Wallet.OnWalletFetchCompletedListener() {
            @Override
            public void onComplete(Wallet wallet) {
                final HashMap<String, Object> toWalletMap = new HashMap<>();
                toWalletMap.put("balance",wallet.getBalance() + amount);
                toWalletMap.put("address", toAddress);
                toWalletMap.put("timestamp", wallet.getTimestamp());

                Wallet.getWallet(fromAddress, new Wallet.OnWalletFetchCompletedListener() {
                    @Override
                    public void onComplete(Wallet wallet) {
                        HashMap<String, Object> fromWalletMap = new HashMap<>();
                        fromWalletMap.put("balance",wallet.getBalance() - amount);
                        fromWalletMap.put("address", fromAddress);
                        fromWalletMap.put("timestamp", wallet.getTimestamp());

                        // Deduct amount from sender's wallet
                        FirebaseFirestore.getInstance().collection("wallets")
                                .document(Preferences.INSTANCE.getDocumentReference(context))
                                .update(fromWalletMap);

                        // Add amount to receiver's wallet
                        FirebaseFirestore.getInstance().collection("wallets")
                                .document(toWallet.getDocumentReference())
                                .update(toWalletMap);

                    }

                    @Override
                    public void onError(String s) {
                        Log.e("Sender's wallet", s);
                    }
                });

            }

            @Override
            public void onError(String s) {
                Log.e("Receiver's wallet", s);
            }
        });

        // Add transaction details to transactions table
        FirebaseFirestore.getInstance().collection("wallets")
                .document(Preferences.INSTANCE.getDocumentReference(context))
                .collection("transactions")
                .add(this)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            onTransactionCompleteListener.onComplete();
                        } else {
                            onTransactionCompleteListener.onError("Transaction couldn't be completed");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onTransactionCompleteListener.onError(e.getMessage());
                    }
                });
    }

    public String print() {

        String printable = "";

        if (toAddress != null){
            printable = printable.concat("To Address: " + toAddress);
        }

        if (fromAddress != null){
            printable = printable.concat("\nFrom Address: " + fromAddress);
        }

        if (amount != null){
            printable = printable.concat("\nAmount: " + amount);
        }

        if (when != null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(when);
            printable = printable.concat("\nTime: " +
                    calendar.get(Calendar.HOUR) + ":" +
                    calendar.get(Calendar.MINUTE) + ":" +
                    calendar.get(Calendar.SECOND) + " " +
                    calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                    calendar.get(Calendar.MONTH) + "/" +
                    calendar.get(Calendar.YEAR)

            );
        }
        return printable;
    }

    static class Builder {

        Transaction transaction;

        Builder() {
            transaction = new Transaction();
        }

        Builder amount(double billAmount) {
            transaction.setAmount(billAmount);
            return this;
        }

        Builder toAddress(String toAddress) {
            transaction.setToAddress(toAddress);
            return this;
        }

        Builder fromAddress(String fromAddress) {
            transaction.setFromAddress(fromAddress);
            return this;
        }

        Transaction build() {
            transaction.setWhen(System.currentTimeMillis());
            return transaction;
        }
    }
}
