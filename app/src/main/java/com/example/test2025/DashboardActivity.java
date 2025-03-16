package com.example.test2025;

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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class DashboardActivity extends AppCompatActivity {

    private Button btnOpenDoor, btnCloseDoor, btnScanQR;
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

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
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

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
        // Mise à jour de l'état de la porte dans Firebase
        databaseReference.child(userId).child("door_status").setValue("open").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Mettre à jour l'UI pour indiquer que la porte est ouverte
                Toast.makeText(DashboardActivity.this, "Door opened", Toast.LENGTH_SHORT).show();
                btnOpenDoor.setEnabled(false);  // Désactiver le bouton Ouvrir
                btnCloseDoor.setEnabled(true);  // Activer le bouton Fermer
            } else {
                Toast.makeText(DashboardActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fermer la porte
    private void closeDoor() {
        // Mise à jour de l'état de la porte dans Firebase
        databaseReference.child(userId).child("door_status").setValue("closed").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Mettre à jour l'UI pour indiquer que la porte est fermée
                Toast.makeText(DashboardActivity.this, "Door closed", Toast.LENGTH_SHORT).show();
                btnCloseDoor.setEnabled(false);  // Désactiver le bouton Fermer
                btnOpenDoor.setEnabled(true);    // Activer le bouton Ouvrir
            } else {
                Toast.makeText(DashboardActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Scanner le QR code
    private void scanQRCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan the QR code");
        integrator.setCameraId(0); // Utiliser la caméra arrière
        integrator.setBeepEnabled(true); // Activer le bip sonore
        integrator.setBarcodeImageEnabled(false); // Désactiver l'image du code barre
        integrator.initiateScan(); // Commencer le scan
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Traiter le résultat du scan QR Code
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedData = result.getContents();
                Log.d("QRCode", "Scanned result: " + scannedData); // Afficher dans les logs

                // Vérifier si le code QR est valide en le comparant avec Firebase
                validateQRCode(scannedData);
            } else {
                Toast.makeText(this, "No QR code detected", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void validateQRCode(String scannedData) {
        // Accéder à la base de données Firebase pour vérifier le QR code
        databaseReference.child(userId).child("door_code").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String validQRCode = snapshot.getValue(String.class);

                Log.d("QRCode", "Scanned: " + scannedData);
                Log.d("QRCode", "Valid QR code from DB: " + validQRCode);

                if (validQRCode != null && scannedData.trim().equals(validQRCode.trim())) {
                    // Si le code scanné est valide, ouvrir la porte
                    openDoor();
                    Toast.makeText(DashboardActivity.this, "Valid QR code - Door Opened", Toast.LENGTH_SHORT).show();
                } else {
                    // Si le code scanné est invalide
                    Toast.makeText(DashboardActivity.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }

    // Méthode pour récupérer les informations de la porte et les afficher
    private void getDoorInfo() {
        databaseReference.child(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String doorLocation = snapshot.child("door_location").getValue(String.class);
                String doorStatus = snapshot.child("door_status").getValue(String.class);

                // Afficher les informations dans le log ou dans l'interface
                Log.d("Door Info", "Location: " + doorLocation + ", Status: " + doorStatus);

                // Adapter l'UI selon le statut de la porte
                if ("open".equals(doorStatus)) {
                    btnOpenDoor.setEnabled(false);  // Désactiver le bouton Ouvrir
                    btnCloseDoor.setEnabled(true);  // Activer le bouton Fermer
                } else if ("closed".equals(doorStatus)) {
                    btnCloseDoor.setEnabled(false); // Désactiver le bouton Fermer
                    btnOpenDoor.setEnabled(true);   // Activer le bouton Ouvrir
                }

                // Optionnel: Afficher un Toast avec les infos
                Toast.makeText(DashboardActivity.this, "Door Location: " + doorLocation + ", Status: " + doorStatus, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
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
