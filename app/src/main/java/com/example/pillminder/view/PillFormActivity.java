package com.example.pillminder.view;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pillminder.R;
import com.example.pillminder.model.Medicamento;
import com.example.pillminder.viewmodel.PillViewModel;

import java.util.Calendar;
import java.util.Locale;

public class PillFormActivity extends AppCompatActivity {

    private PillViewModel pillViewModel;
    private EditText etNombre, etDosis, etHora;
    private Button btnSave;
    private Spinner spinnerTipoDosis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_form);

        // 1. Configurar barra superior
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nueva Pastilla");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pillViewModel = new ViewModelProvider(this).get(PillViewModel.class);

        // 2. Inicializar vistas
        etNombre = findViewById(R.id.et_nombre_medicamento);
        etDosis = findViewById(R.id.et_dosis_cantidad);
        etHora = findViewById(R.id.et_hora_toma);
        btnSave = findViewById(R.id.btn_save_pill);
        spinnerTipoDosis = findViewById(R.id.spinner_tipo_dosis);

        // 3. Configurar el Spinner (ml, pastillas, etc.)
        setupSpinner();

        // 4. Configurar el Selector de Hora (Reloj)
        setupTimePicker();

        etHora.setFocusable(false);
        etHora.setOnClickListener(v -> showTimePickerDialog());

        btnSave.setOnClickListener(v -> savePill());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipos_dosis_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDosis.setAdapter(adapter);
    }

    private void setupTimePicker() {
        // Evitamos que salga el teclado al tocar el campo de hora
        etHora.setFocusable(false);
        etHora.setClickable(true);

        etHora.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hora = calendar.get(Calendar.HOUR_OF_DAY);
            int minuto = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        // Formatear hora a HH:mm (ej. 08:05)
                        String horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        etHora.setText(horaFormateada);
                    }, hora, minuto, true);
            timePickerDialog.show();
        });
    }

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
        String tipoDosis = spinnerTipoDosis.getSelectedItem().toString();

        if (nombre.isEmpty() || dosisStr.isEmpty() || horaToma.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int dosisCantidad = Integer.parseInt(dosisStr);

            // Creamos el objeto. Nota: Asegúrate de que el constructor de Medicamento
            // acepte estos parámetros o usa los setters.
            Medicamento nuevoMedicamento = new Medicamento(
                    nombre,
                    horaToma,
                    dosisCantidad,
                    30, // Stock inicial por defecto
                    null
            );

            // Asignamos el tipo de dosis (ml / pastillas) seleccionado en el Spinner
            nuevoMedicamento.setTipoDosis(tipoDosis);

            pillViewModel.addMedicamento(nuevoMedicamento);

            Toast.makeText(this, nombre + " guardado con éxito.", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "La dosis debe ser un número válido.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTimePickerDialog() {
        // Obtener la hora actual para mostrarla por defecto
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = calendar.get(java.util.Calendar.MINUTE);

        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    // Formatear la hora para que siempre tenga dos dígitos (ej: 09:05)
                    String selectedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    etHora.setText(selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }
}