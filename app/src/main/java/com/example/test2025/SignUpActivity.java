package com.example.test2025;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private TextView goToSignIn;
    private EditText userName, email, cin, password, confirmPassword;
    private Button btnSignUp;
    private String userNameString, emailString, cinString, passwordString, confirmPasswordString;
    private static final String EMAIL_REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize UI components
        goToSignIn = findViewById(R.id.go_to_sign_in);
        userName = findViewById(R.id.user_name_sign_up);
        email = findViewById(R.id.email_sign_up);
        cin = findViewById(R.id.cin_sign_up);
        password = findViewById(R.id.password_sign_up);
        confirmPassword = findViewById(R.id.confirm_password_sign_up);
        btnSignUp = findViewById(R.id.btn_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        // Navigate to SignInActivity
        goToSignIn.setOnClickListener(v -> startActivity(new Intent(this, SignInActivity.class)));

        // Sign up user when the button is clicked
        btnSignUp.setOnClickListener(v -> {
            if (validate()) {
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                firebaseAuth.createUserWithEmailAndPassword(emailString, passwordString)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                sendEmailVerification();
                            } else {
                                Toast.makeText(this, "Sign-up failed", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
            }
        });
    }

    private void sendEmailVerification() {
        FirebaseUser loggedUser = firebaseAuth.getCurrentUser();
        if (loggedUser != null) {
            loggedUser.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendUserData();
                    Toast.makeText(this, "Registration done, please check your email", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, SignInActivity.class));
                    progressDialog.dismiss();
                    finish();
                } else {
                    Toast.makeText(this, "Email verification failed", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void sendUserData() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users");

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("cin", cinString);
            userMap.put("fullName", userNameString);
            userMap.put("email", emailString);

            // Generate initial door data using CIN
            String initialDoorCode = cinString.length() >= 4 ? cinString.substring(0, 4) : "0000";
            String initialDoorId = FirebaseDatabase.getInstance().getReference().push().getKey();

            // Create default door under "doors" node
            Map<String, Object> doorMap = new HashMap<>();
            doorMap.put("location", "Default Location");
            doorMap.put("code", initialDoorCode);
            doorMap.put("status", "open");

            userMap.put("doors/" + initialDoorId, doorMap);

            databaseReference.child(userId).updateChildren(userMap)
                    .addOnCompleteListener(saveTask -> {
                        if (saveTask.isSuccessful()) {
                            Toast.makeText(this, "User data saved successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

//    private void sendUserData() {
//        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference = firebaseDatabase.getReference("users");
//
//        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//        if (firebaseUser != null) {
//            String userId = firebaseUser.getUid();
//
//            Map<String, Object> userMap = new HashMap<>();
//            userMap.put("cin", cinString);
//            userMap.put("codeNum", "1623");
//            userMap.put("firstname", userNameString);
//            userMap.put("lastname", "Ayouni");
//            userMap.put("location", "sfax");
//            userMap.put("status", "open");
//            userMap.put("door_status", "open");
//            userMap.put("door_code", "XYZ123");
//
//            // Store data in /users/{userId}
//            databaseReference.child(userId).setValue(userMap)
//                    .addOnCompleteListener(saveTask -> {
//                        if (saveTask.isSuccessful()) {
//                            Toast.makeText(this, "User data saved successfully", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }
//    }

    private boolean validate() {
        boolean result = false;
        userNameString = userName.getText().toString().trim();
        emailString = email.getText().toString().trim();
        cinString = cin.getText().toString().trim();
        passwordString = password.getText().toString().trim();
        confirmPasswordString = confirmPassword.getText().toString().trim();

        if (userNameString.length() < 7) {
            userName.setError("Username is invalid");
        } else if (!isValidEmail(emailString)) {
            email.setError("Email is invalid");
        } else if (cinString.length() != 8) {
            cin.setError("CIN is invalid");
        } else if (passwordString.length() < 6) {
            password.setError("Password is invalid");
        } else if (!confirmPasswordString.equals(passwordString)) {
            confirmPassword.setError("Passwords do not match");
        } else {
            result = true;
        }
        return result;
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
