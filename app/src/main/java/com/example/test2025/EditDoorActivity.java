//package com.example.test2025;
//
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//public class EditDoorActivity extends AppCompatActivity {
//    private EditText etEditCode, etEditLocation;
//    private Button btnSave;
//    private DatabaseReference doorsRef;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_edit_door);
//
//        // Initialize views
//        etEditCode = findViewById(R.id.etEditCode);
//        etEditLocation = findViewById(R.id.etEditLocation);
//        btnSave = findViewById(R.id.btnSave);
//
//        // Initialize Firebase reference
//        doorsRef = FirebaseDatabase.getInstance().getReference("doors");
//
//        btnSave.setOnClickListener(v -> {
//            String code = etEditCode.getText().toString().trim();
//            String location = etEditLocation.getText().toString().trim();
//
//            // Check for empty fields
//            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(location)) {
//                Toast.makeText(EditDoorActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Search for the door with the current code
//            doorsRef.orderByChild("code").equalTo(code)
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (snapshot.exists()) {
//                                // If the door exists, get the door ID and update the code and location
//                                for (DataSnapshot child : snapshot.getChildren()) {
//                                    String doorId = getIntent().getStringExtra("DOOR_ID");
//
//                                    // Update the code and location of the door
//                                    child.getRef().child("code").setValue(code); // Change the code
//                                    child.getRef().child("location").setValue(location); // Change the location
//
//                                    // Confirmation message
//                                    Toast.makeText(EditDoorActivity.this, "Door updated successfully", Toast.LENGTH_SHORT).show();
//                                    finish(); // Close the activity
//                                }
//                            } else {
//                                // If no door is found
//                                Toast.makeText(EditDoorActivity.this, "Door not found", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                            // Display an error if the read fails
//                            Toast.makeText(EditDoorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        });
//    }
//}


package com.example.test2025;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

public class EditDoorActivity extends AppCompatActivity {
    private EditText etEditCode, etEditLocation;
    private Button btnSave;
    private DatabaseReference doorRef;
    private String doorId;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_door);

        // Initialize views
        etEditCode = findViewById(R.id.etEditCode);
        etEditLocation = findViewById(R.id.etEditLocation);
        btnSave = findViewById(R.id.btnSave);

        // Get current user and door ID
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        doorId = getIntent().getStringExtra("DOOR_ID");

        if (currentUser == null || doorId == null) {
            Toast.makeText(this, "Error: Invalid door selection", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase reference
        doorRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUser.getUid())
                .child("doors")
                .child(doorId);

        // Load existing door data
        loadDoorData();

        btnSave.setOnClickListener(v -> updateDoor());
    }

    private void loadDoorData() {
        doorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String currentCode = snapshot.child("code").getValue(String.class);
                    String currentLocation = snapshot.child("location").getValue(String.class);

                    etEditCode.setText(currentCode);
                    etEditLocation.setText(currentLocation);
                } else {
                    Toast.makeText(EditDoorActivity.this, "Door not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditDoorActivity.this, "Error loading door: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateDoor() {
        String newCode = etEditCode.getText().toString().trim();
        String newLocation = etEditLocation.getText().toString().trim();

        if (TextUtils.isEmpty(newCode) || TextUtils.isEmpty(newLocation)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newCode.matches("\\d{4}")) {
            Toast.makeText(this, "Code must be 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update door data directly using the reference
        doorRef.child("code").setValue(newCode);
        doorRef.child("location").setValue(newLocation)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditDoorActivity.this, "Door updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditDoorActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}