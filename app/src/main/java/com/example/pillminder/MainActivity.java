package com.example.pillminder;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Importaciones de Firebase
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicializar Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 2. Crear un medicamento de prueba (OBJETO)
        // "user123" es un ID inventado. Más adelante usaremos el real del Login.
        Medicamento nuevoMedicamento = new Medicamento("Paracetamol", "08:00", 20, "user123");

        // 3. Subirlo a la nube ("medicamentos" es el nombre de la colección/tabla)
        db.collection("medicamentos")
                .add(nuevoMedicamento)
                .addOnSuccessListener(documentReference -> {
                    // ESTO OCURRE SI TODO SALE BIEN
                    Toast.makeText(this, "Guardado en la Nube con éxito!", Toast.LENGTH_LONG).show();
                    Log.d("FIREBASE", "ID del documento guardado: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // ESTO OCURRE SI FALLA (ej. sin internet)
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_LONG).show();
                    Log.e("FIREBASE", "Error añadiendo documento", e);
                });
    }
}