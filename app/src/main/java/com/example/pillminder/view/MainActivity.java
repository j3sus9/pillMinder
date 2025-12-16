package com.example.pillminder.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillminder.R;
import com.example.pillminder.adapter.MedicamentoAdapter;
import com.example.pillminder.model.Medicamento;
import com.example.pillminder.view.LoginActivity;
import com.example.pillminder.view.PillFormActivity;
import com.example.pillminder.viewmodel.AuthViewModel;
import com.example.pillminder.viewmodel.PillViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

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
        // Se inicializan aquí para que gestionen la lógica de la pantalla
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        pillViewModel = new ViewModelProvider(this).get(PillViewModel.class);

        // 2. Inicialización de Vistas
        rvMedicamentos = findViewById(R.id.rv_medicamentos);
        fabAddPill = findViewById(R.id.fab_add_pill);

        // 3. Configuración del RecyclerView
        rvMedicamentos.setLayoutManager(new LinearLayoutManager(this));

        // 3a. Inicializar y asignar el adaptador
        adapter = new MedicamentoAdapter();
        rvMedicamentos.setAdapter(adapter);

        // 3b. Asignar el listener para la acción "Tomada"
        adapter.setOnItemActionListener(medicamento -> {
            // Aquí llamarías a una nueva función en PillViewModel para registrar la toma
            // Por ejemplo: pillViewModel.recordPillTaken(medicamento.getDocumentId());
            Toast.makeText(MainActivity.this, "¡Tomaste " + medicamento.getNombre() + "!", Toast.LENGTH_SHORT).show();
        });

        // 4. Observador de Datos (MVVM Core)
        setupPillObserver();

        // 5. Listener para añadir pastillas
        fabAddPill.setOnClickListener(v -> {
            // Navegar a la Activity/Fragment del formulario para añadir una pastilla
            Intent intent = new Intent(MainActivity.this, PillFormActivity.class);
            startActivity(intent);
        });

        // 6. Observador de Logout
        setupLogoutObserver();
    }

    /**
     * Observa el LiveData de medicamentos del ViewModel.
     * Esta función se llama cada vez que se añade, modifica o elimina un medicamento en Firebase.
     */
    private void setupPillObserver() {
        pillViewModel.getMedicamentos().observe(this, medicamentos -> {
            if (medicamentos != null) {
                // Si la lista de medicamentos cambia, actualiza el RecyclerView
                adapter.submitList(medicamentos);

                // Muestra un Toast simple para confirmar que los datos llegaron
                Toast.makeText(this, "Medicamentos cargados: " + medicamentos.size(), Toast.LENGTH_SHORT).show();

                // Debugging: Muestra los nombres de las pastillas
                for (Medicamento m : medicamentos) {
                    System.out.println("Medicamento: " + m.getNombre() + " | ID: " + m.getDocumentId());
                }
            } else {
                Toast.makeText(this, "No se encontraron medicamentos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Observa el LiveData del usuario para gestionar el Logout (si se llama desde AuthViewModel).
     */
    private void setupLogoutObserver() {
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            // Cuando el usuario se convierte en 'null' (después de llamar a authViewModel.logout())
            if (firebaseUser == null) {
                // Navegar a la pantalla de Login
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    // ------------------------------------------------------------------
    // IMPLEMENTACIÓN DEL MENÚ DE OPCIONES (Para el botón de Cerrar Sesión)
    // ------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Usa el ID del elemento de menú
        if (item.getItemId() == R.id.action_logout) {
            // Lógica de Cerrar Sesión
            authViewModel.logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}