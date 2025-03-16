package com.example.test2025;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private EditText etLocation, etCode;
    private Button btnAdd, btnDelete, btnEdit, btnGenerateQRCode;
    private ImageView ivQRCode;
    private Spinner spinnerStatus;  // Spinner pour le statut

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialiser les vues
        etLocation = findViewById(R.id.location);
        etCode = findViewById(R.id.code);
        btnAdd = findViewById(R.id.btn_add);
        btnDelete = findViewById(R.id.btn_delete);
        btnEdit = findViewById(R.id.btn_edit);
        btnGenerateQRCode = findViewById(R.id.btn_generate_code_qr);
        ivQRCode = findViewById(R.id.qr_code_image);
        spinnerStatus = findViewById(R.id.spinner_status);  // Spinner pour le statut

        // Ajout porte
        btnAdd.setOnClickListener(view -> addDoor());

        // Supprimer porte
        btnDelete.setOnClickListener(view -> deleteDoor());

        // Edit porte
        btnEdit.setOnClickListener(view -> onEditButtonClick(view));

        // Générer QR code
        btnGenerateQRCode.setOnClickListener(view -> generateQRCode());
    }

    private void addDoor() {
        String location = etLocation.getText().toString().trim();
        String code = etCode.getText().toString().trim();

        // Vérifier si les champs sont vides
        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Cannot be empty");
            return;
        }
        if (TextUtils.isEmpty(code)) {
            etCode.setError("Cannot be empty");
            return;
        }

        // Vérifier si le code contient exactement 4 chiffres
        if (!code.matches("\\d{4}")) {
            Toast.makeText(this, "The code must be exactly 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer l'ID de l'utilisateur
        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();
        if (loggedUser != null) {
            String userId = loggedUser.getUid(); // Récupérer l'ID de l'utilisateur connecté

            // Créer une nouvelle carte avec les données de la porte
            Map<String, Object> doorData = new HashMap<>();
            doorData.put("location", location);
            doorData.put("code", code); // Utiliser le code comme code
            doorData.put("status", spinnerStatus.getSelectedItem().toString()); // Utiliser le statut dynamique sélectionné
            doorData.put("qr_code", code); // Utiliser le code comme qr_code

            // Générer un ID pour la porte
            String doorId = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("doors").push().getKey();

            // Enregistrer les données sous l'utilisateur et sous "doors" dans Firebase
            if (doorId != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("doors").child(doorId);
                userRef.setValue(doorData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(HomeActivity.this, "Door added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Error while adding door", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    private void deleteDoor() {
        String doorName = etCode.getText().toString().trim(); // Utiliser doorName comme identifiant

        if (TextUtils.isEmpty(doorName)) {
            etCode.setError("This field cannot be empty");
            return;
        }

        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();
        if (loggedUser != null) {
            String userId = loggedUser.getUid(); // Récupérer l'ID de l'utilisateur connecté
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.child("door_code").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        snapshot.getRef().removeValue(); // Supprimer la porte
                        Toast.makeText(HomeActivity.this, "Door deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Door not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onEditButtonClick(View view) {
        Intent intent = new Intent(HomeActivity.this, EditDoorActivity.class);
        startActivity(intent);
    }

    private void generateQRCode() {
        String code = etCode.getText().toString().trim();

        // Vérifier si le code est vide
        if (TextUtils.isEmpty(code)) {
            etCode.setError("This field cannot be empty");
            return;
        }

        // Vérifier si le code contient exactement 4 chiffres
        if (!code.matches("\\d{4}")) {
            Toast.makeText(this, "The code must be exactly 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            // Générer le QR code
            Bitmap bitmap = toBitmap(qrCodeWriter.encode(code, BarcodeFormat.QR_CODE, 400, 400));
            ivQRCode.setImageBitmap(bitmap); // Afficher le QR code dans l'ImageView

            if (bitmap != null) {
                // Sauvegarder le QR code généré dans Firebase
                saveQRCodeInFirebase(code);
            }

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating the QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCodeInFirebase(String code) {
        // Sauvegarder le QR code généré dans Firebase sous "validQRCode"
        FirebaseDatabase.getInstance().getReference("validQRCode")
                .setValue(code)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(HomeActivity.this, "QR Code saved in Firebase", Toast.LENGTH_SHORT).show();
                        // Lancer l'activité Dashboard après la sauvegarde
                        Intent intent = new Intent(HomeActivity.this, DashboardActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(HomeActivity.this, "Error saving the QR Code", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap toBitmap(com.google.zxing.common.BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bitmap;
    }
}
