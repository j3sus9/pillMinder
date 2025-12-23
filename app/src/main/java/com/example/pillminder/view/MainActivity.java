package com.example.pillminder.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillminder.R;
import com.example.pillminder.adapter.MedicamentoAdapter;
import com.example.pillminder.model.Medicamento;
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
            public void onTomarClick(Medicamento medicamento) {
                if (medicamento.getStockTotal() >= medicamento.getDosis()) {
                    pillViewModel.tomarMedicamento(medicamento);
                    Toast.makeText(MainActivity.this, "Dosis registrada de " + medicamento.getNombre(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "¡Stock insuficiente!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onBorrarClick(Medicamento medicamento) {
                mostrarDialogoConfirmacion(medicamento);
            }

            @Override
            public void onEditarClick(Medicamento medicamento) {
                // Próxima mejora: Abrir PillFormActivity pasando el objeto para editar
                Toast.makeText(MainActivity.this, "Función de editar próximamente", Toast.LENGTH_SHORT).show();
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