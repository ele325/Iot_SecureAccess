package com.example.test2025;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private Button btnOpenDoor, btnCloseDoor, btnScanQR;
    private DatabaseReference getDoorReference() {
        return FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("doors")
                .child(selectedDoorId);
    }    private String userId;
    private String selectedDoorId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        selectedDoorId = getIntent().getStringExtra("SELECTED_DOOR_ID");

        // Initialiser Firebase
        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();
        if (loggedUser != null) {
            userId = loggedUser.getUid();  // Récupérer l'ID de l'utilisateur connecté
        }

        // Associer les boutons
        btnOpenDoor = findViewById(R.id.btnOn);
        btnCloseDoor = findViewById(R.id.btnOff);
        btnScanQR = findViewById(R.id.btnScanQR);

        // Initialiser Firebase Reference pour l'utilisateur

        // Listener pour le bouton "Ouvrir la porte"
        btnOpenDoor.setOnClickListener(v -> openDoor());

        // Listener pour le bouton "Fermer la porte"
        btnCloseDoor.setOnClickListener(v -> closeDoor());

        // Listener pour le bouton "Scanner QR Code"
        btnScanQR.setOnClickListener(v -> scanQRCode());

        // Récupérer les informations de la porte et les afficher
        getDoorInfo();
    }

    // Ouvrir la porte
    private void openDoor() {
        getDoorReference().child("status").setValue("open")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateDoorUI("open");
                    } else {
                        Toast.makeText(DashboardActivity.this,
                                "Failed to open door",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
//    private void openDoor() {
//        getDoorReference().child("status").setValue("open")
//                .addOnCompleteListener(task -> {
//                    // Keep existing UI update logic
//                });
//    }
    // Fermer la porte

    private void closeDoor() {
        getDoorReference().child("status").setValue("closed")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateDoorUI("closed");
                    } else {
                        Toast.makeText(DashboardActivity.this,
                                "Failed to close door",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
//    private void closeDoor() {
//        getDoorReference().child("status").setValue("closed")
//                .addOnCompleteListener(task -> {
//                    // Keep existing UI update logic
//                });
//    }


    private void scanQRCode() {
        Log.d("DashboardActivity", "Initiating QR code scan");
        Log.d("tofeha", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan the QR code");
        integrator.setCameraId(0); // Use rear camera
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("DashboardActivity", "onActivityResult called");
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedCode = result.getContents();
                Log.d("DashboardActivity", "Scanned QR code: " + scannedCode);
                validateScannedQRCode(scannedCode);
            } else {
                Log.w("DashboardActivity", "No QR code detected");
                Toast.makeText(this, "No QR code detected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w("DashboardActivity", "Scan result is null");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    // Validate the scanned code
    private void validateScannedQRCode(String scannedCode) {
        if (selectedDoorId == null) {
            Toast.makeText(this, "No door selected", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference doorRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("doors")
                .child(selectedDoorId);

        doorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String validCode = snapshot.child("code").getValue(String.class);
                String storedQR = snapshot.child("qrCode").getValue(String.class);
                String currentStatus = snapshot.child("status").getValue(String.class);

                if (scannedCode.equals(validCode) || scannedCode.equals(storedQR)) {
                    if ("closed".equals(currentStatus)) {
                        openDoor();
                        Toast.makeText(DashboardActivity.this,
                                "Access granted. Door opened",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DashboardActivity.this,
                                "Door is already open",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DashboardActivity.this,
                            "Invalid QR code",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this,
                        "Validation failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void validateScannedQRCode(String scannedCode) {
//        if (selectedDoorId == null) {
//            Toast.makeText(this, "No door selected", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        DatabaseReference doorRef = FirebaseDatabase.getInstance().getReference("users")
//                .child(userId)
//                .child("doors")
//                .child(selectedDoorId);
//
//        doorRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String validCode = snapshot.child("code").getValue(String.class);
//                String storedQR = snapshot.child("qrCode").getValue(String.class);
//
//                // Check both the current code and active QR code
//                if (scannedCode.equals(validCode) || scannedCode.equals(storedQR)) {
//                    toggleDoorState();
//                    Toast.makeText(DashboardActivity.this, "Access granted", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(DashboardActivity.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(DashboardActivity.this, "Validation failed", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    // New toggle method for door state
    private void toggleDoorState() {
        DatabaseReference statusRef = getDoorReference().child("status");
        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentStatus = snapshot.getValue(String.class);
                String newStatus = "open".equals(currentStatus) ? "closed" : "open";

                statusRef.setValue(newStatus)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                updateDoorUI(newStatus);
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "State change failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateDoorUI(String status) {
        runOnUiThread(() -> {
            if ("open".equals(status)) {
                btnOpenDoor.setEnabled(false);
                btnCloseDoor.setEnabled(true);
                Toast.makeText(this, "Door opened", Toast.LENGTH_SHORT).show();
            } else {
                btnCloseDoor.setEnabled(false);
                btnOpenDoor.setEnabled(true);
                Toast.makeText(this, "Door closed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Méthode pour récupérer les informations de la porte et les afficher
    private void getDoorInfo() {
        getDoorReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String doorLocation = snapshot.child("location").getValue(String.class);
                String doorStatus = snapshot.child("status").getValue(String.class);

                // Update UI based on doorStatus
                if ("open".equals(doorStatus)) {
                    btnOpenDoor.setEnabled(false);
                    btnCloseDoor.setEnabled(true);
                } else {
                    btnCloseDoor.setEnabled(false);
                    btnOpenDoor.setEnabled(true);
                }

                Toast.makeText(DashboardActivity.this,
                        "Managing: " + doorLocation,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }
    private void changeDoorStatus(String newStatus) {
        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();
        if (loggedUser != null) {
            String userId = loggedUser.getUid(); // Récupérer l'ID de l'utilisateur connecté

            // Mettre à jour le statut de la porte
            Map<String, Object> statusData = new HashMap<>();
            statusData.put("door_status", newStatus); // Modifier le statut de la porte

            // Mettre à jour les informations sous /users/userId directement
            FirebaseDatabase.getInstance().getReference("users").child(userId).updateChildren(statusData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(DashboardActivity.this, "Door status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DashboardActivity.this, "Error while updating door status", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    // Méthode pour envoyer les données utilisateur
    private void sendUserData(String cin, String firstname, String lastname, String location) {
        // Créer un nouvel utilisateur sous le chemin /users/{cin}
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(cin);

        // Créer les données à stocker
        User user = new User(cin, "1623", firstname, lastname, location, "open");

        // Enregistrer les données dans Firebase
        userRef.setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DashboardActivity.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DashboardActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Classe représentant un utilisateur
    public static class User {
        public String cin;
        public String codeNum;
        public String firstname;
        public String lastname;
        public String location;
        public String status;

        public User() {
            // Constructor required for Firebase
        }

        public User(String cin, String codeNum, String firstname, String lastname, String location, String status) {
            this.cin = cin;
            this.codeNum = codeNum;
            this.firstname = firstname;
            this.lastname = lastname;
            this.location = location;
            this.status = status;
        }
    }
}
