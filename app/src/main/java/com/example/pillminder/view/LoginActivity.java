package com.example.pillminder.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView; // Para el TextView de registrarse
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pillminder.view.MainActivity; // Tu Activity principal
import com.example.pillminder.R;
import com.example.pillminder.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Inicialización de Vistas (Conexión con activity_login.xml)
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);

        // 2. Inicialización del ViewModel (MVVM)
        // Esto le dice a Android que queremos usar el ViewModel y gestionará su ciclo de vida
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 3. Observadores de LiveData
        setupObservers();

        // 4. Listeners para la interacción del usuario
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    // ----------------------------------------------------------------------
    // MÉTODOS DE LÓGICA DE LA VISTA
    // ----------------------------------------------------------------------

    /**
     * Configura los observadores del LiveData expuesto por el ViewModel.
     * Esta es la esencia de MVVM en la View.
     */
    private void setupObservers() {
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                // Éxito: vamos a la main
                progressBar.setVisibility(View.GONE); // <--- Asegúrate de ocultarlo aquí
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        authViewModel.getErrorLiveData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                // ERROR: Ocultamos el progreso y mostramos por qué falló
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Recoge los datos de los EditText y pide al ViewModel que inicie sesión.
     */
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_empty_credentials), Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar barra de progreso
        progressBar.setVisibility(View.VISIBLE);

        // Llamada al ViewModel (Lógica de negocio encapsulada)
        authViewModel.login(email, password);
    }

    /**
     * Recoge los datos de los EditText y pide al ViewModel que registre.
     */
    private void attemptRegister() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validación básica (Firebase Auth requiere mínimo 6 caracteres en la contraseña)
        if (email.isEmpty() || password.isEmpty() || password.length() < 6) {
            Toast.makeText(this, getString(R.string.register_invalid_credentials), Toast.LENGTH_LONG).show();
            return;
        }

        // Mostrar barra de progreso
        progressBar.setVisibility(View.VISIBLE);

        // Llamada al ViewModel (Lógica de negocio encapsulada)
        authViewModel.register(email, password);
    }
}