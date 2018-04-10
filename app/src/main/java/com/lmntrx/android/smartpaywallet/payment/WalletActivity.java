package com.lmntrx.android.smartpaywallet.payment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.zxing.WriterException;
import com.lmntrx.android.smartpaywallet.Preferences;
import com.lmntrx.android.smartpaywallet.R;
import com.lmntrx.android.smartpaywallet.payment.qrcode.QRCodeHandler;

public class WalletActivity extends AppCompatActivity {

    TextView balanceTextView;
    TextView addressTextView;

    Wallet wallet;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        wallet = new Wallet();

        balanceTextView = findViewById(R.id.balanceTextView);
        addressTextView = findViewById(R.id.walletAddressTextView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Updating Wallet...");
        progressDialog.show();

        attachWallet();

    }

    private void attachWallet() {
        FirebaseFirestore.getInstance().collection("wallets").document(Preferences.INSTANCE.getDocumentReference(this))
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot document, FirebaseFirestoreException e) {
                        progressDialog.dismiss();
                        if (document.getId().equals(Preferences.INSTANCE.getDocumentReference(WalletActivity.this))) {
                            wallet.setDocumentReference(document.getId());
                            wallet.setAddress(document.getString("address"));
                            wallet.setBalance(document.getDouble("balance"));
                            balanceTextView.setText(String.valueOf(document.getDouble("balance")));
                            addressTextView.setText(document.getString("address"));
                            setPrivateKeyQRCode();
                        }
                    }
                });
    }

    private void setPrivateKeyQRCode() {
        ImageView privateKeyImageView = findViewById(R.id.privateKeyImageView);
        try {
            privateKeyImageView.setImageBitmap(QRCodeHandler.generateQRCode(wallet.getAddress(), getScreenRes()));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    public int getScreenRes() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.densityDpi;
    }

    public void rechargeWallet(View view) {
        final EditText editText = new EditText(this);
        editText.setHint("Enter amount");
        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        new AlertDialog.Builder(this)
                .setTitle("Recharge Wallet")
                .setView(editText)
                .setPositiveButton("Buy Credits", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        final ProgressDialog progressDialog = new ProgressDialog(WalletActivity.this);
                        try {
                            Double rechargeAmount = Double.parseDouble(editText.getText().toString());
                            if (rechargeAmount > 0 && rechargeAmount <= 10000) {
                                dialogInterface.dismiss();
                                progressDialog.setMessage("Please Wait...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                Wallet.recharge(wallet, rechargeAmount, new Wallet.OnRechargeComplete() {
                                    @Override
                                    public void onComplete(Double newBalance) {
                                        progressDialog.dismiss();
                                        //balanceTextView.setText(String.valueOf(newBalance));
                                        Toast.makeText(WalletActivity.this, "Wallet Recharged", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(WalletActivity.this, "Recharge amount should be greater than 0 and less than 10000", Toast.LENGTH_LONG).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(WalletActivity.this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

    }
}

