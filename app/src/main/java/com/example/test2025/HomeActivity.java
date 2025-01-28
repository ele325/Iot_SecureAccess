package com.example.test2025;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.WriterException;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;

public class HomeActivity extends AppCompatActivity {

    private EditText etLocation, etCode;
    private Button btnAdd, btnDelete, btnEdit, btnGenerateQRCode;
    private ImageView ivQRCode;
    private DatabaseReference doorsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialisation des vues
        etLocation = findViewById(R.id.location);
        etCode = findViewById(R.id.code);
        btnAdd = findViewById(R.id.btn_add);
        btnDelete = findViewById(R.id.btn_delete);
        btnEdit = findViewById(R.id.btn_edit);
        btnGenerateQRCode = findViewById(R.id.btn_generate_code_qr);
        ivQRCode = findViewById(R.id.qr_code_image);

        // Référence Firebase
        doorsRef = FirebaseDatabase.getInstance().getReference("Doors");

        // Ajouter une porte
        btnAdd.setOnClickListener(view -> addDoor());

        // Supprimer une porte
        btnDelete.setOnClickListener(view -> deleteDoor());

        // Modifier une porte
        btnEdit.setOnClickListener(view -> editDoor());

        // Générer un code QR
        btnGenerateQRCode.setOnClickListener(view -> generateQRCode());
    }

    private void addDoor() {
        String location = etLocation.getText().toString().trim();
        String code = etCode.getText().toString().trim();

        if (TextUtils.isEmpty(location) || TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        String doorId = doorsRef.push().getKey(); // Génère un ID unique
        Map<String, String> doorData = new HashMap<>();
        doorData.put("location", location);
        doorData.put("code", code);

        doorsRef.child(doorId).setValue(doorData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Porte ajoutée avec succès", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteDoor() {
        String code = etCode.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Veuillez entrer le code", Toast.LENGTH_SHORT).show();
            return;
        }

        doorsRef.orderByChild("code").equalTo(code)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                child.getRef().removeValue();
                            }
                            Toast.makeText(HomeActivity.this, "Porte supprimée avec succès", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HomeActivity.this, "Porte introuvable", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void editDoor() {
        String code = etCode.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        doorsRef.orderByChild("code").equalTo(code)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                child.getRef().child("location").setValue(location);
                            }
                            Toast.makeText(HomeActivity.this, "Porte modifiée avec succès", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HomeActivity.this, "Porte introuvable", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generateQRCode() {
        String code = etCode.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Veuillez entrer un code", Toast.LENGTH_SHORT).show();
            return;
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            // Génération du QR code
            Bitmap bitmap = toBitmap(qrCodeWriter.encode(code, BarcodeFormat.QR_CODE, 400, 400));
            ivQRCode.setImageBitmap(bitmap); // Affichage du QR code dans l'ImageView

            if (bitmap != null) {
                // Démarrer l'activité DashboardActivity
                Intent intent = new Intent(HomeActivity.this, DashboardActivity.class);
                startActivity(intent);
                // Optionnel : pour terminer l'activité actuelle
            }

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la génération du QR code", Toast.LENGTH_SHORT).show();
        }
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
