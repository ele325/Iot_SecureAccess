package com.example.test2025;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {
    private TextView goToSignUp, goToForgotPassword;
    private EditText email, password;
    private Button btnSignIn;
    private CheckBox rememberMe;
    private String emailString, passwordString;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Déclaration des variables
        goToSignUp = findViewById(R.id.go_to_sign_up);
        goToForgotPassword = findViewById(R.id.go_to_forgot_password);
        email = findViewById(R.id.email_sign_in);
        password = findViewById(R.id.password_sign_in);
        rememberMe = findViewById(R.id.remember_me);
        btnSignIn = findViewById(R.id.btn_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("access_control");

        // Actions
        goToSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        goToForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));

        btnSignIn.setOnClickListener(v -> {
            emailString = email.getText().toString().trim();
            passwordString = password.getText().toString().trim();

            // Vérifier si les champs sont vides
            if (emailString.isEmpty()) {
                email.setError("Email cannot be empty");
                return;
            }

            if (!isValidEmail(emailString)) {
                email.setError("Invalid email format");
                return;
            }

            if (passwordString.isEmpty()) {
                password.setError("Password cannot be empty");
                return;
            }

            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            // Tentative de connexion
            firebaseAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Enregistrer l'utilisateur dans Firebase sous "access_control"
                    FirebaseUser loggedUser = firebaseAuth.getCurrentUser();
                    if (loggedUser != null) {
                        String userId = loggedUser.getUid();
                        saveUserToFirebase(userId, loggedUser.getEmail());
                    }
                } else {
                    Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        });
    }

    private void saveUserToFirebase(String userId, String email) {
        // Récupérer des informations par défaut ou à partir du formulaire
        String cin = "15261416"; // CIN par défaut
        String codeNum = "1623"; // CodeNum par défaut
        String firstname = "Ella"; // Prénom par défaut
        String lastname = "Ayouni"; // Nom par défaut
        String location = "sfax"; // Location par défaut
        String phone = "98765432"; // Numéro de téléphone par défaut
        String status = "open"; // Statut par défaut

        // Création d'une entrée pour l'utilisateur dans Firebase sous "/Users/{userId}"
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("cin", cin);  // Récupérer le CIN
        userMap.put("codeNum", codeNum);  // Récupérer le codeNum
        userMap.put("firstname", firstname);  // Récupérer le prénom
        userMap.put("lastname", lastname);  // Récupérer le nom de famille
        userMap.put("location", location);  // Récupérer la localisation
        userMap.put("phone", phone);  // Récupérer le téléphone
        userMap.put("status", status);  // Récupérer le statut

        // Enregistrer l'utilisateur sous "Users/{userId}"
        databaseReference.child("Users").child(userId).setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "User data saved", Toast.LENGTH_SHORT).show();
                // Rediriger vers l'activité principale
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Error saving user data", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private boolean isValidEmail(String email) {
        // Vérification de la validité de l'email
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
