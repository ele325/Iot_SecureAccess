package com.example.test2025;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class DashboardActivity extends AppCompatActivity {

    private Button btnOpenDoor, btnCloseDoor, btnScanQR;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialiser Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Doors");

        // Associer les boutons
        btnOpenDoor = findViewById(R.id.btnOn);
        btnCloseDoor = findViewById(R.id.btnOff);
        btnScanQR = findViewById(R.id.btnScanQR);

        // Listener pour le bouton "Ouvrir la porte"
        btnOpenDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDoor();
            }
        });

        // Listener pour le bouton "Fermer la porte"
        btnCloseDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDoor();
            }
        });

        // Listener pour le bouton "Scanner QR Code"
        btnScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQRCode();
            }
        });
    }

    private void openDoor() {
        databaseReference.child("doorStatus").setValue("open").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DashboardActivity.this, "Porte ouverte", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DashboardActivity.this, "Erreur lors de l'ouverture", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void closeDoor() {
        databaseReference.child("doorStatus").setValue("closed").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DashboardActivity.this, "Porte fermée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DashboardActivity.this, "Erreur lors de la fermeture", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scanQRCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scannez un code QR");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedData = result.getContents();
                validateQRCode(scannedData);
            } else {
                Toast.makeText(this, "Aucun code QR détecté", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void validateQRCode(String scannedData) {
        databaseReference.child("validQRCode").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String validQRCode = snapshot.getValue(String.class);
                if (scannedData.equals(validQRCode)) {
                    Toast.makeText(DashboardActivity.this, "QR Code valide", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DashboardActivity.this, "QR Code invalide", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erreur: " + error.getMessage());
            }
        });
    }
}
