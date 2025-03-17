package com.example.test2025;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test2025.models.Door;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private EditText etLocation, etCode;
    private Button btnAdd, btnDelete, btnEdit, btnGenerateQRCode;
    private ImageView ivQRCode;
    private Spinner spinnerStatus;  // Spinner pour le statut

    private Spinner spinnerDoors;
    private List<String> doorIds = new ArrayList<>();
    private ArrayAdapter<String> doorAdapter;
    private List<String> doorCodes = new ArrayList<>(); // Track existing codes
    private Button btnManage;



    @SuppressLint("MissingInflatedId")
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

        spinnerDoors = findViewById(R.id.spinner_doors);
        doorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        doorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoors.setAdapter(doorAdapter);
        btnManage = findViewById(R.id.btn_manage);
        btnManage.setEnabled(false);

        spinnerDoors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean hasSelection = position >= 0 && position < doorIds.size();
                btnDelete.setEnabled(hasSelection);
                btnEdit.setEnabled(hasSelection);
                btnManage.setEnabled(hasSelection); // Add this line
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                btnDelete.setEnabled(false);
                btnEdit.setEnabled(false);
                btnManage.setEnabled(false); // Add this line
            }
        });

        fetchDoors();

        // Ajout porte
        btnAdd.setOnClickListener(view -> addDoor());

        // Supprimer porte
        btnDelete.setOnClickListener(view -> deleteDoor());

        // Edit porte
        btnEdit.setOnClickListener(view -> onEditButtonClick(view));

        btnManage.setOnClickListener(v -> manageDoor());

        // Générer QR code
        btnGenerateQRCode.setOnClickListener(view -> generateQRCode());
        btnDelete.setEnabled(false);
        btnEdit.setEnabled(false);
    }

    private void manageDoor() {
        int selectedPosition = spinnerDoors.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= doorIds.size()) {
            Toast.makeText(this, "Please select a door first", Toast.LENGTH_SHORT).show();
            return;
        }

        String doorId = doorIds.get(selectedPosition);
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("SELECTED_DOOR_ID", doorId);
        startActivity(intent);
    }
    private void fetchDoors() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference doorsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("doors");

            doorsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> doorList = new ArrayList<>();
                    doorIds.clear();
                    doorCodes.clear();

                    for (DataSnapshot doorSnapshot : snapshot.getChildren()) {
                        String doorId = doorSnapshot.getKey();
                        String location = doorSnapshot.child("location").getValue(String.class);
                        String code = doorSnapshot.child("code").getValue(String.class);

                        doorList.add(location);
                        doorIds.add(doorId);
                        doorCodes.add(code);
                    }

                    doorAdapter.clear();
                    doorAdapter.addAll(doorList);
                    doorAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this,
                            "Error fetching doors: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

//    private void fetchDoors() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            DatabaseReference doorsRef = FirebaseDatabase.getInstance()
//                    .getReference("users")
//                    .child(user.getUid())
//                    .child("doors");
//
//            doorsRef.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    List<String> doorList = new ArrayList<>();
//                    doorIds.clear();
//
//                    for (DataSnapshot doorSnapshot : snapshot.getChildren()) {
//                        String doorId = doorSnapshot.getKey();
//                        String location = doorSnapshot.child("location").getValue(String.class);
//                        doorList.add(location);
//                        doorIds.add(doorId);
//                    }
//
//                    doorAdapter.clear();
//                    doorAdapter.addAll(doorList);
//                    doorAdapter.notifyDataSetChanged();
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Toast.makeText(HomeActivity.this, "Error fetching doors: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }


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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference doorsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("doors");

            // Check if code already exists
            if (doorCodes.contains(code)) {
                Toast.makeText(this, "Door code already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            String doorId = doorsRef.push().getKey();
            Map<String, Object> doorData = new HashMap<>();
            doorData.put("location", location);
            doorData.put("code", code);
            doorData.put("status", "open");

            doorsRef.child(doorId).updateChildren(doorData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            doorCodes.add(code); // Add to local cache
                            Toast.makeText(this, "Door added", Toast.LENGTH_SHORT).show();
                            fetchDoors();
                        } else {
                            Toast.makeText(this, "Add failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

//    private void addDoor() {
//        String location = etLocation.getText().toString().trim();
//        String code = etCode.getText().toString().trim();
//
//        // Vérifier si les champs sont vides
//        if (TextUtils.isEmpty(location)) {
//            etLocation.setError("Cannot be empty");
//            return;
//        }
//        if (TextUtils.isEmpty(code)) {
//            etCode.setError("Cannot be empty");
//            return;
//        }
//
//        // Vérifier si le code contient exactement 4 chiffres
//        if (!code.matches("\\d{4}")) {
//            Toast.makeText(this, "The code must be exactly 4 digits", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Récupérer l'ID de l'utilisateur
//        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (loggedUser != null) {
//            String userId = loggedUser.getUid(); // Récupérer l'ID de l'utilisateur connecté
//
//            // Créer ou mettre à jour les informations de la porte sous l'utilisateur
//            Map<String, Object> doorData = new HashMap<>();
//            doorData.put("location", location);
//            doorData.put("status", "open"); // Statut de la porte
//            doorData.put("door_code", code); // Code de la porte
//            doorData.put("door_status", "open"); // Statut de la porte
//
//            // Mettre à jour les informations sous /users/userId directement
//            FirebaseDatabase.getInstance().getReference("users").child(userId).updateChildren(doorData)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(HomeActivity.this, "Door added successfully", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(HomeActivity.this, "Error while adding door", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }
//    }

    private void deleteDoor() {
        int selectedPosition = spinnerDoors.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= doorIds.size()) {
            Toast.makeText(this, "Please select a door first", Toast.LENGTH_SHORT).show();
            return;
        }

        String doorId = doorIds.get(selectedPosition);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference doorRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("doors")
                    .child(doorId);

            doorRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Door deleted", Toast.LENGTH_SHORT).show();
                    fetchDoors(); // Refresh list
                } else {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

//    private void deleteDoor() {
//        String doorName = etCode.getText().toString().trim(); // Utiliser doorName comme identifiant
//
//        if (TextUtils.isEmpty(doorName)) {
//            etCode.setError("This field cannot be empty");
//            return;
//        }
//
//        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (loggedUser != null) {
//            String userId = loggedUser.getUid(); // Récupérer l'ID de l'utilisateur connecté
//            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
//
//            userRef.child("door_code").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        snapshot.getRef().removeValue(); // Supprimer la porte
//                        Toast.makeText(HomeActivity.this, "Door deleted successfully", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(HomeActivity.this, "Door not found", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Toast.makeText(HomeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }

    public void onEditButtonClick(View view) {
        int selectedPosition = spinnerDoors.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= doorIds.size()) {
            Toast.makeText(this, "Please select a door first", Toast.LENGTH_SHORT).show();
            return;
        }

        String doorId = doorIds.get(selectedPosition);
        Intent intent = new Intent(this, EditDoorActivity.class);
        intent.putExtra("DOOR_ID", doorId);
        startActivity(intent);
    }


//    public void onEditButtonClick(View view) {
//        Intent intent = new Intent(HomeActivity.this, EditDoorActivity.class);
//        startActivity(intent);
//    }

    private void generateQRCode() {
        int selectedPosition = spinnerDoors.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= doorIds.size()) {
            Toast.makeText(this, "Please select a door first", Toast.LENGTH_SHORT).show();
            return;
        }

        String doorId = doorIds.get(selectedPosition);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid())
                .child("doors")
                .child(doorId)
                .child("code")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String code = snapshot.getValue(String.class);
                        if (code == null || !code.matches("\\d{4}")) {
                            Toast.makeText(HomeActivity.this, "Invalid door code", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            QRCodeWriter writer = new QRCodeWriter();
                            Bitmap bitmap = toBitmap(writer.encode(code, BarcodeFormat.QR_CODE, 400, 400));
                            ivQRCode.setImageBitmap(bitmap);
                            saveQRCodeInFirebase(doorId, code);
                        } catch (WriterException e) {
                            Toast.makeText(HomeActivity.this, "QR generation failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


//    private void generateQRCode() {
//        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();
//        Log.i("","/////////////////"+loggedUser);
//        if (loggedUser != null) {
//            String userId = loggedUser.getUid();
//            Log.i("UserId","////////////////-------/"+userId);
//            FirebaseDatabase.getInstance().getReference("users").child(userId).child("door_code")
//                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            String code = snapshot.getValue(String.class);
//                            if (code == null || TextUtils.isEmpty(code)) {
//                                Toast.makeText(HomeActivity.this, "No door code found. Add a door first.", Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                            if (!code.matches("\\d{4}")) {
//                                Toast.makeText(HomeActivity.this, "Stored code is invalid", Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                            QRCodeWriter qrCodeWriter = new QRCodeWriter();
//                            try {
//                                Bitmap bitmap = toBitmap(qrCodeWriter.encode(code, BarcodeFormat.QR_CODE, 400, 400));
//                                ivQRCode.setImageBitmap(bitmap);
//                                saveQRCodeInFirebase(code);
//                            } catch (WriterException e) {
//                                e.printStackTrace();
//                                Toast.makeText(HomeActivity.this, "Error generating QR code", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                            Toast.makeText(HomeActivity.this, "Error fetching code: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }
//    }
//    private void saveQRCodeInFirebase(String code) {
//        // Sauvegarder le QR code généré dans Firebase sous "validQRCode"
//        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();
//        Log.i("","/////////////////"+loggedUser);
//
//        String userId = loggedUser.getUid();
//        FirebaseDatabase.getInstance().getReference("users").child(userId).child("doors")
//                .setValue(code)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(HomeActivity.this, "QR Code saved in Firebase", Toast.LENGTH_SHORT).show();
//                        // Lancer l'activité Dashboard après la sauvegarde
//                        Intent intent = new Intent(HomeActivity.this, DashboardActivity.class);
//                        startActivity(intent);
//                    } else {
//                        Toast.makeText(HomeActivity.this, "Error saving the QR Code", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    private void saveQRCodeInFirebase(String doorId, String code) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference doorRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .child("doors")
                    .child(doorId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("qrCode", code); // Store the active QR code
            updates.put("lastGenerated", System.currentTimeMillis());

            doorRef.updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "QR Code updated", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

//    private void saveQRCodeInFirebase(String code) {
//        FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (loggedUser != null) {
//            String userId = loggedUser.getUid();
//
//            // Créez un objet Door avec les informations que vous souhaitez sauvegarder
//            Door newDoor = new Door("Location Example", code, "open"); // Replace with actual location and status
//
//            // Référence Firebase pour les portes sous l'utilisateur
//            DatabaseReference userDoorsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("doors");
//
//            // Vous pouvez générer une clé unique pour chaque porte
//            String doorId = userDoorsRef.push().getKey();
//
//            // Ajouter la porte sous le nœud "doors" de l'utilisateur
//            if (doorId != null) {
//                userDoorsRef.child(doorId).setValue(newDoor)
//                        .addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                Toast.makeText(HomeActivity.this, "QR Code saved in Firebase", Toast.LENGTH_SHORT).show();
//                                // Lancer l'activité Dashboard après la sauvegarde
//                                Intent intent = new Intent(HomeActivity.this, DashboardActivity.class);
//                                startActivity(intent);
//                            } else {
//                                Toast.makeText(HomeActivity.this, "Error saving the QR Code", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//            }
//        }
//    }


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
