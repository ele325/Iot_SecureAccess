package com.example.test2025;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanQRCodeActivity extends AppCompatActivity {

    private DatabaseReference qrCodeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code);

        // Initializing Firebase reference to check valid QR codes
        qrCodeRef = FirebaseDatabase.getInstance().getReference("validQRCode");

        // Launching QR Code scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan the QR Code");
        integrator.setCameraId(0);  // Use the rear camera
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("test","-------------------------------------------------------------");
        // Get the result from the QR code scan
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String scannedCode = result.getContents().trim();  // Remove any leading/trailing spaces
                Log.d("QRCode", "Scanned code: " + scannedCode);  // Log the scanned QR code
                checkQRCodeInFirebase(scannedCode);  // Verify the scanned code in Firebase
            }
        }
    }

    private void checkQRCodeInFirebase(String scannedCode) {
        // Search for the scanned QR code in Firebase
        Log.i("vvvvvvv","///////////////////");
        qrCodeRef.orderByValue().equalTo(scannedCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(ScanQRCodeActivity.this, "Valid QR Code!", Toast.LENGTH_SHORT).show();
                            // Perform necessary actions after a valid scan
                        } else {
                            Toast.makeText(ScanQRCodeActivity.this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ScanQRCodeActivity.this, "Error checking the QR code", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
