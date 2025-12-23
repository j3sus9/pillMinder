package com.example.pillminder.view;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pillminder.R;
import com.example.pillminder.model.Medicamento;
import com.example.pillminder.viewmodel.PillViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PillFormActivity extends AppCompatActivity {

    private PillViewModel pillViewModel;
    private EditText etNombre, etDosis, etHora, etCantidadTotal;
    private Button btnSave;
    private Spinner spinnerTipoDosis;
    private List<String> listaHoras = new ArrayList<>();
    private String medicamentoId = null;
    private String usuarioIdOriginal = null;
    private boolean editando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_form);

        // 1. Configurar barra superior con flecha de volver
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nuevo Medicamento");
        }

        pillViewModel = new ViewModelProvider(this).get(PillViewModel.class);

        // 2. Inicializar vistas
        etNombre = findViewById(R.id.et_nombre_medicamento);
        etDosis = findViewById(R.id.et_dosis_cantidad);
        etCantidadTotal = findViewById(R.id.et_cantidad_total);
        etHora = findViewById(R.id.et_hora_toma);
        btnSave = findViewById(R.id.btn_save_pill);
        spinnerTipoDosis = findViewById(R.id.spinner_tipo_dosis);

        // 3. Configuración inicial
        setupSpinner();
        setupTimePicker();

        // 4. Comprobar si estamos en MODO EDICIÓN
        Medicamento medAEditar = (Medicamento) getIntent().getSerializableExtra("medicamento_editar");
        if (medAEditar != null) {
            configurarModoEdicion(medAEditar);
            editando = true;
        }

        btnSave.setOnClickListener(v -> savePill());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipos_dosis_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDosis.setAdapter(adapter);
    }

    private void setupTimePicker() {
        etHora.setFocusable(false);
        etHora.setClickable(true);

        etHora.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int horaActual = calendar.get(Calendar.HOUR_OF_DAY);
            int minutoActual = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        String horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        if (!listaHoras.contains(horaFormateada)) {
                            listaHoras.add(horaFormateada);
                            Collections.sort(listaHoras);
                            actualizarTextoHoras();
                        } else {
                            Toast.makeText(this, "Esta hora ya ha sido añadida", Toast.LENGTH_SHORT).show();
                        }
                    }, horaActual, minutoActual, true);
            timePickerDialog.show();
        });

        etHora.setOnLongClickListener(v -> {
            listaHoras.clear();
            etHora.setText("");
            Toast.makeText(this, "Horarios reiniciados", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void actualizarTextoHoras() {
        if (listaHoras.isEmpty()) {
            etHora.setText("");
            return;
        }
        etHora.setText(String.join(", ", listaHoras));
    }

    private void configurarModoEdicion(Medicamento med) {
        medicamentoId = med.getDocumentId();
        usuarioIdOriginal = med.getUsuarioId();

        // Cambiar título de la barra superior
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Editar " + med.getNombre());
        }

        etNombre.setText(med.getNombre());
        etDosis.setText(String.valueOf(med.getDosis()));
        etCantidadTotal.setText(String.valueOf(med.getStockTotal()));

        // Cargar horas existentes
        if (med.getHorasToma() != null) {
            listaHoras = new ArrayList<>(med.getHorasToma());
            actualizarTextoHoras();
        }

        // Seleccionar tipo en Spinner
        ArrayAdapter adapter = (ArrayAdapter) spinnerTipoDosis.getAdapter();
        int position = adapter.getPosition(med.getTipoDosis());
        if (position >= 0) spinnerTipoDosis.setSelection(position);

        btnSave.setText("ACTUALIZAR CAMBIOS");
    }

    private void savePill() {
        String nombre = etNombre.getText().toString().trim();
        String dosisStr = etDosis.getText().toString().trim();
        String cantidadStr = etCantidadTotal.getText().toString().trim();

        if (nombre.isEmpty() || dosisStr.isEmpty() || cantidadStr.isEmpty() || listaHoras.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos y añada al menos una hora.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int dosisCantidad = Integer.parseInt(dosisStr);
            int cantidadTotal = Integer.parseInt(cantidadStr);

            Medicamento medicamento = new Medicamento();
            medicamento.setNombre(nombre);
            medicamento.setDosis(dosisCantidad);
            medicamento.setTipoDosis(spinnerTipoDosis.getSelectedItem().toString());
            medicamento.setHorasToma(new ArrayList<>(listaHoras));
            medicamento.setStockTotal(cantidadTotal);
            medicamento.setUsuarioId(usuarioIdOriginal);

            if (medicamentoId != null) {
                // Estamos EDITANDO
                medicamento.setDocumentId(medicamentoId);
                pillViewModel.updateMedicamento(medicamento);
                Toast.makeText(this, "Medicamento actualizado con éxito", Toast.LENGTH_SHORT).show();
            } else {
                // Estamos CREANDO
                pillViewModel.addMedicamento(medicamento);
                Toast.makeText(this, "Medicamento guardado con éxito", Toast.LENGTH_SHORT).show();
            }

            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "La dosis y cantidad deben ser números válidos.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (editando)
                mostrarDialogoConfirmacion();
            else
                finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Salir sin guardar")
                .setMessage("¿Estás seguro de que quieres salir sin guardar? Perderás cualquier modificación que hayas realizado.")
                .setPositiveButton("Salir", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}