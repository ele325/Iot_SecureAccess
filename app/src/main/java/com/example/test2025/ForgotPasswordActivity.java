package com.example.test2025;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText email;
    private Button btnResetPassword, btnBack;
    private String emailString;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);
        email = findViewById(R.id.email_forgot);
        btnResetPassword =findViewById(R.id.btn_reset_password);
        btnBack =findViewById(R.id.back_to_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);



        btnBack.setOnClickListener(v ->{
            startActivity(new Intent(this, SignInActivity.class));

        });

        btnResetPassword.setOnClickListener(v -> {
            emailString = email.getText().toString();
            progressDialog.setMessage("please wait !");
            progressDialog.show();
            firebaseAuth.sendPasswordResetEmail(emailString).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, SignInActivity.class));
                    progressDialog.dismiss();
                    finish();

                }else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        });


    }
}