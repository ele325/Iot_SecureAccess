package com.example.test2025;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    //Declaration des variables
    private TextView goToSignUp, goToForgotPassword;
    private EditText email, password;
    private Button btnSignIn;
    private CheckBox rememberMe;
    private String emailString, passwordString;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // Affectation des variables
        goToSignUp = findViewById(R.id.go_to_sign_up);
        goToForgotPassword = findViewById(R.id.go_to_forgot_password);
        email = findViewById(R.id.email_sign_in);
        password = findViewById(R.id.password_sign_in);
        rememberMe = findViewById(R.id.remember_me);
        btnSignIn = findViewById(R.id.btn_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        // actions
        goToSignUp.setOnClickListener(v -> {

            // to do after click to text view
            startActivity(new Intent(this, SignUpActivity.class));
        });

        goToForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        btnSignIn.setOnClickListener(v -> {
            emailString = email.getText().toString().trim();
            passwordString = password.getText().toString().trim();
            progressDialog.setMessage("please wait !");
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(task ->
            {
                if (task.isSuccessful()) {
                    checkEmailVerification();

                } else {
                    Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

            });
        });


        

    }

    private void checkEmailVerification() {
        FirebaseUser loggedUser = firebaseAuth.getCurrentUser();
        if (loggedUser != null) {
            if (loggedUser.isEmailVerified()) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }else
            {
                loggedUser.sendEmailVerification();
                loggedUser.sendEmailVerification();
                Toast.makeText(this, "Please verified email", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                firebaseAuth.signOut();
            }
        }
    }

    // Declaration des methodes
}