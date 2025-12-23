package com.example.pillminder.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillminder.R;
import com.example.pillminder.adapter.MedicamentoAdapter;
import com.example.pillminder.model.Medicamento;
import com.example.pillminder.utils.FormatUtils;
import com.example.pillminder.viewmodel.AuthViewModel;
import com.example.pillminder.viewmodel.PillViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private PillViewModel pillViewModel;
    private RecyclerView rvMedicamentos;
    private FloatingActionButton fabAddPill;
    private MedicamentoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicialización de ViewModels
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        pillViewModel = new ViewModelProvider(this).get(PillViewModel.class);

        // 2. Inicialización de Vistas
        rvMedicamentos = findViewById(R.id.rv_medicamentos);
        fabAddPill = findViewById(R.id.fab_add_pill);

        // 3. Configuración del RecyclerView
        rvMedicamentos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicamentoAdapter();
        rvMedicamentos.setAdapter(adapter);

        // 4. Configuración de los Clics del Adaptador (Nueva Interfaz)
        adapter.setOnMedicamentoClickListener(new MedicamentoAdapter.OnMedicamentoClickListener() {
            @Override
            public void onTomarClick(Medicamento medicamento, String tomaId) {
                // Primero verificamos si hay stock, antes de preguntar
                if (medicamento.getStockTotal() >= medicamento.getDosis()) {
                    mostrarDialogoConfirmacionToma(medicamento, tomaId);
                } else {
                    Toast.makeText(MainActivity.this, "No queda stock suficiente para esta dosis", Toast.LENGTH_LONG).show();
                }
            }

            /**
             * Muestra un diálogo para confirmar que el usuario realmente quiere registrar la toma.
             */
            private void mostrarDialogoConfirmacionToma(Medicamento medicamento, String tomaId) {
                // Extraemos la hora del tomaId (ej: de "20231223_08:00" sacamos "08:00")
                String hora = tomaId.contains("_") ? tomaId.split("_")[1] : "";

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirmar toma")
                        .setMessage("¿Has tomado tu dosis de " + medicamento.getNombre() + " correspondiente a las " + hora + "?")
                        .setPositiveButton("SÍ, LA HE TOMADO", (dialog, which) -> {
                            pillViewModel.tomarMedicamento(medicamento, tomaId);
                        })
                        .setNegativeButton("NO", null)
                        .show();
            }

            @Override
            public void onBorrarClick(Medicamento medicamento) {
                mostrarDialogoConfirmacion(medicamento);
            }

            @Override
            public void onEditarClick(Medicamento medicamento) {
                Intent intent = new Intent(MainActivity.this, PillFormActivity.class);
                intent.putExtra("medicamento_editar", medicamento);
                startActivity(intent);
            }

            @Override
            public void onReponerClick(Medicamento med) {
                mostrarDialogoReponer(med);
            }


        });

        // 5. Observadores de Datos
        setupPillObserver();
        setupLogoutObserver();

        // 6. Listener para añadir pastillas
        fabAddPill.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PillFormActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Muestra un diálogo para añadir stock a un medicamento.
     */
    private void mostrarDialogoReponer(Medicamento med) {
        // Creamos un campo de texto para el número
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Ej: 30");

        // Margen para el EditText dentro del diálogo
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 60;
        params.rightMargin = 60;
        params.topMargin = 20;
        input.setLayoutParams(params);
        container.addView(input);

        // Usamos FormatUtils para que la pregunta sea gramaticalmente correcta
        String tipo = med.getTipoDosis();
        String interrogativo = FormatUtils.obtenerInterrogativo(tipo);
        String mensaje = interrogativo + " " + tipo.toLowerCase() + " quieres añadir a la cantidad actual?";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reponer " + med.getNombre())
                .setMessage(mensaje)
                .setView(container)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    String valor = input.getText().toString();
                    if (!valor.isEmpty()) {
                        int cantidadAñadida = Integer.parseInt(valor);
                        int nuevoStock = med.getStockTotal() + cantidadAñadida;

                        // Llamamos al ViewModel para actualizar en Firebase
                        pillViewModel.actualizarStock(med.getDocumentId(), nuevoStock);

                        // Formateamos la unidad para el mensaje de éxito (ej: "1 pastilla" o "30 pastillas")
                        String unidadFinal = FormatUtils.obtenerUnidadFormateada(cantidadAñadida, tipo);
                        Toast.makeText(this, "Se han añadido " + valor + " " + unidadFinal, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Muestra un diálogo de confirmación antes de borrar de Firebase.
     */
    private void mostrarDialogoConfirmacion(Medicamento medicamento) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Medicamento")
                .setMessage("¿Estás seguro de que quieres borrar " + medicamento.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    pillViewModel.deleteMedicamento(medicamento.getDocumentId());
                    Toast.makeText(MainActivity.this, "Medicamento eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setupPillObserver() {
        pillViewModel.getMedicamentos().observe(this, medicamentos -> {
            if (medicamentos != null) {
                // Pasamos una copia de la lista para que ListAdapter detecte el cambio
                adapter.submitList(new ArrayList<>(medicamentos));
            }
        });
    }

    private void setupLogoutObserver() {
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    // --- Menú de la barra superior (Logout) ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            authViewModel.logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}