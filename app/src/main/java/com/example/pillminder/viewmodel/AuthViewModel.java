package com.example.pillminder.viewmodel; // Asegúrate de que el paquete 'viewmodel' exista

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.pillminder.data.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

// El ViewModel extiende la clase base de AndroidX para ser consciente del ciclo de vida
public class AuthViewModel extends ViewModel {

    // Referencia a la capa de datos
    private final AuthRepository authRepository;

    // 1. LiveData del Usuario: La Activity lo observará para saber si debe navegar al Home
    // Usamos LiveData de solo lectura para exponerlo, y que la Activity no pueda modificar el valor.
    private final LiveData<FirebaseUser> userLiveData;

    // 2. LiveData de Error: La Activity lo observará para mostrar un Toast o mensaje de error
    private final LiveData<String> errorLiveData;


    // Constructor
    public AuthViewModel() {
        // Inicializa el repositorio
        this.authRepository = new AuthRepository();

        // Obtiene las referencias a los LiveData del repositorio
        // El repositorio se encarga de cambiar los valores de estos LiveData.
        this.userLiveData = authRepository.getUserLiveData();
        this.errorLiveData = authRepository.getErrorLiveData();
    }

    /**
     * Inicia el proceso de registro.
     * El resultado se publica en 'userLiveData' o 'errorLiveData'.
     */
    public void register(String email, String password) {
        authRepository.register(email, password);
    }

    /**
     * Inicia el proceso de inicio de sesión.
     * El resultado se publica en 'userLiveData' o 'errorLiveData'.
     */
    public void login(String email, String password) {
        authRepository.login(email, password);
    }

    // ------------------------------------------------------------------
    // Getters para que la View (LoginActivity) pueda suscribirse (observe)
    // ------------------------------------------------------------------

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    // Opcional, pero útil para cerrar sesión desde MainActivity/Ajustes
    public void logout() {
        authRepository.logout();
    }
}