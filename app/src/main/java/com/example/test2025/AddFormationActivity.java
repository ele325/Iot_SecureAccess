package com.example.test2025; // Remplace par ton package

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddFormationActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Création du layout principal
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        // Création des éléments
        TextView title = new TextView(this);
        title.setText("Ajouter une Formation");
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);

        EditText titreInput = new EditText(this);
        titreInput.setHint("Titre de la formation");

        EditText descriptionInput = new EditText(this);
        descriptionInput.setHint("Description");

        EditText dateInput = new EditText(this);
        dateInput.setHint("Date (YYYY-MM-DD)");

        Button btnAjouter = new Button(this);
        btnAjouter.setText("Ajouter Formation");
        btnAjouter.setBackgroundColor(Color.BLUE);
        btnAjouter.setTextColor(Color.WHITE);

        // Ajout des éléments au layout
        layout.addView(title);
        layout.addView(titreInput);
        layout.addView(descriptionInput);
        layout.addView(dateInput);
        layout.addView(btnAjouter);

        // Ajout du layout dans le ScrollView
        scrollView.addView(layout);

        // Définir le layout comme l'affichage principal
        setContentView(scrollView);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();

        // Action du bouton
        btnAjouter.setOnClickListener(v -> {
            String titre = titreInput.getText().toString();
            String description = descriptionInput.getText().toString();
            String date = dateInput.getText().toString();
            String userId = "USER_ID_ICI"; // À remplacer par l'ID de l'utilisateur connecté

            ajouterFormation(titre, description, date, userId);
        });
    }

    public void ajouterFormation(String titre, String description, String date, String userId) {
        Map<String, Object> formation = new HashMap<>();
        formation.put("titre", titre);
        formation.put("description", description);
        formation.put("date", date);
        formation.put("createdBy", userId);

        db.collection("formations")
                .add(formation)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firebase", "Formation ajoutée avec ID : " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("Firebase", "Erreur lors de l'ajout", e);
                });
    }
}
