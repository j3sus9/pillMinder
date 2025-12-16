package com.example.pillminder.view;

import android.os.Bundle;
import android.view.MenuItem; // Necesario para la flecha de volver
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.pillminder.R;
import com.example.pillminder.model.Medicamento;
import com.example.pillminder.viewmodel.PillViewModel;

public class PillFormActivity extends AppCompatActivity {

    private PillViewModel pillViewModel;
    private EditText etNombre, etDosis, etHora;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_form);

        // Configurar la barra superior con título y flecha de volver
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nueva Pastilla");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pillViewModel = new ViewModelProvider(this).get(PillViewModel.class);

        etNombre = findViewById(R.id.et_nombre_medicamento);
        etDosis = findViewById(R.id.et_dosis_cantidad);
        etHora = findViewById(R.id.et_hora_toma);
        btnSave = findViewById(R.id.btn_save_pill);

        btnSave.setOnClickListener(v -> savePill());
    }

    // Método para que funcione la flecha de volver
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePill() {
        String nombre = etNombre.getText().toString().trim();
        String dosisStr = etDosis.getText().toString().trim();
        String horaToma = etHora.getText().toString().trim();

        if (nombre.isEmpty() || dosisStr.isEmpty() || horaToma.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int dosisCantidad = Integer.parseInt(dosisStr);

            // -----------------------------------------------------------
            // SOLUCIÓN: Creamos el medicamento con userId en NULL.
            // NO comprobamos nada aquí. El ViewModel se encargará.
            // -----------------------------------------------------------
            Medicamento nuevoMedicamento = new Medicamento(
                    nombre,
                    horaToma,
                    dosisCantidad,
                    30,
                    null // <--- Pasamos null deliberadamente
            );

            pillViewModel.addMedicamento(nuevoMedicamento);

            Toast.makeText(this, nombre + " guardado con éxito.", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "La dosis debe ser un número válido.", Toast.LENGTH_SHORT).show();
        }
    }
}