package com.example.pillminder.view;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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
import com.example.pillminder.receiver.AlarmReceiver;
import com.example.pillminder.viewmodel.PillViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PillFormActivity extends AppCompatActivity {

    private PillViewModel pillViewModel;
    private EditText etNombre, etDosis, etHora, etCantidadTotal;
    private Button btnSave;
    private Spinner spinnerTipoDosis;
    private List<String> listaHoras = new ArrayList<>();
    private String medicamentoId = null;
    private String usuarioIdOriginal = null;
    private boolean editando = false;
    private Medicamento medAEditarOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_form);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nuevo Medicamento");
        }

        pillViewModel = new ViewModelProvider(this).get(PillViewModel.class);

        etNombre = findViewById(R.id.et_nombre_medicamento);
        etDosis = findViewById(R.id.et_dosis_cantidad);
        etCantidadTotal = findViewById(R.id.et_cantidad_total);
        etHora = findViewById(R.id.et_hora_toma);
        btnSave = findViewById(R.id.btn_save_pill);
        spinnerTipoDosis = findViewById(R.id.spinner_tipo_dosis);

        // Verificar permisos de notificación para Android 13+ (Tiramisu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        setupSpinner();
        setupTimePicker();

        medAEditarOriginal = (Medicamento) getIntent().getSerializableExtra("medicamento_editar");
        if (medAEditarOriginal != null) {
            configurarModoEdicion(medAEditarOriginal);
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
                            Toast.makeText(this, "Esta hora ya está en la lista", Toast.LENGTH_SHORT).show();
                        }
                    }, horaActual, minutoActual, true); // true = formato 24h
            timePickerDialog.show();
        });

        etHora.setOnLongClickListener(v -> {
            if (listaHoras.isEmpty()) {
                return true; // No hacemos nada si está vacía
            }

            String[] horasArray = listaHoras.toArray(new String[0]);

            new AlertDialog.Builder(this)
                    .setTitle("Eliminar una toma")
                    .setItems(horasArray, (dialog, which) -> {
                        String horaBorrada = listaHoras.get(which);
                        listaHoras.remove(which);
                        actualizarTextoHoras();
                        Toast.makeText(this, "Hora " + horaBorrada + " eliminada", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .setNeutralButton("Borrar Todo", (dialog, which) -> {
                        listaHoras.clear();
                        actualizarTextoHoras();
                    })
                    .show();

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Editar " + med.getNombre());
        }

        etNombre.setText(med.getNombre());
        etDosis.setText(String.valueOf(med.getDosis()));
        etCantidadTotal.setText(String.valueOf(med.getStockTotal()));

        if (med.getHorasToma() != null) {
            listaHoras = new ArrayList<>(med.getHorasToma());
            actualizarTextoHoras();
        }

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

            if (medicamentoId != null) {
                // Estamos EDITANDO
                cancelAlarms(medAEditarOriginal);
                medicamento.setDocumentId(medicamentoId);
                medicamento.setUsuarioId(usuarioIdOriginal);
                pillViewModel.updateMedicamento(medicamento);
                scheduleAlarms(medicamento);
                Toast.makeText(this, "Medicamento actualizado con éxito", Toast.LENGTH_SHORT).show();
            } else {
                // Estamos CREANDO
                String newId = UUID.randomUUID().toString();
                medicamento.setDocumentId(newId);
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    medicamento.setUsuarioId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }
                pillViewModel.addMedicamento(medicamento);
                scheduleAlarms(medicamento);
                Toast.makeText(this, "Medicamento guardado con éxito", Toast.LENGTH_SHORT).show();
            }

            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "La dosis y cantidad deben ser números válidos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleAlarms(Medicamento medicamento) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (medicamento.getHorasToma() == null || medicamento.getDocumentId() == null) {
            return;
        }

        for (String hora : medicamento.getHorasToma()) {
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("medicamento_nombre", medicamento.getNombre());
            intent.putExtra("medicamento_id", medicamento.getDocumentId());

            String[] parts = hora.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            intent.putExtra("hora_original", hour);
            intent.putExtra("minuto_original", minute);
            // --------------------------------------------------------------------

            int requestCode = (medicamento.getDocumentId() + hora).hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Si la hora ya pasó hoy, programar para mañana
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void cancelAlarms(Medicamento medicamento) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (medicamento.getHorasToma() == null || medicamento.getDocumentId() == null) {
            return;
        }

        for (String hora : medicamento.getHorasToma()) {
            Intent intent = new Intent(this, AlarmReceiver.class);
            int requestCode = (medicamento.getDocumentId() + hora).hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
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
