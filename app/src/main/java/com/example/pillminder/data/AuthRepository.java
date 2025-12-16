package com.example.pillminder.data;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    private static final String TAG = "AuthRepository";

    // El objeto principal para la autenticación de Firebase
    private final FirebaseAuth firebaseAuth;

    // LiveData que usaremos internamente para cambiar el estado del usuario (Mutable)
    private final MutableLiveData<FirebaseUser> userLiveData;

    // LiveData para publicar mensajes de error específicos (Mutable)
    private final MutableLiveData<String> errorLiveData;

    public AuthRepository() {
        // Inicialización
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userLiveData = new MutableLiveData<>();
        this.errorLiveData = new MutableLiveData<>();

        // 1. Comprobar el estado inicial: ¿Hay un usuario logeado?
        if (firebaseAuth.getCurrentUser() != null) {
            // Si lo hay, lo publicamos inmediatamente para que la LoginActivity navegue al Home
            userLiveData.postValue(firebaseAuth.getCurrentUser());
        }
    }

    // ----------------------------------------------------
    // MÉTODOS DE AUTENTICACIÓN
    // ----------------------------------------------------

    /**
     * Registra un nuevo usuario en Firebase.
     * Publica el nuevo usuario o un mensaje de error en los LiveData correspondientes.
     */
    public void register(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Registro exitoso.");
                        // Si es exitoso, publicamos el usuario actual.
                        userLiveData.postValue(firebaseAuth.getCurrentUser());
                        errorLiveData.postValue(null); // Limpiar cualquier error anterior
                    } else {
                        Log.e(TAG, "Fallo en el registro.", task.getException());
                        // Si falla, publicamos el mensaje de error para el usuario.
                        errorLiveData.postValue(task.getException().getMessage());
                        userLiveData.postValue(null); // Aseguramos que el usuario no está logeado
                    }
                });
    }

    /**
     * Intenta iniciar sesión con email y contraseña.
     */
    public void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Inicio de sesión exitoso.");
                        userLiveData.postValue(firebaseAuth.getCurrentUser());
                        errorLiveData.postValue(null);
                    } else {
                        Log.e(TAG, "Fallo en el inicio de sesión.", task.getException());
                        errorLiveData.postValue(task.getException().getMessage());
                        userLiveData.postValue(null);
                    }
                });
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    public void logout() {
        firebaseAuth.signOut();
        // Publicamos 'null' para notificar a la View que ya no hay usuario logeado.
        userLiveData.postValue(null);
        Log.d(TAG, "Sesión cerrada.");
    }

    // ----------------------------------------------------
    // GETTERS PARA EL VIEWMODEL
    // ----------------------------------------------------

    // El ViewModel utilizará estos métodos para obtener las referencias de los LiveData
    // y exponerlas (como LiveData de solo lectura) a la View.

    public MutableLiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}