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
        this.firebaseAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

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
        Log.d("AUTH_DEBUG", "Intentando login para: " + email);
        errorLiveData.postValue(null);

        // Creamos un "paracaídas": si en 12 segundos no hay respuesta, avisamos.
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (userLiveData.getValue() == null && errorLiveData.getValue() == null) {
                String msg = "Sin respuesta de Firebase. Revisa el SHA-1 o desactiva reCAPTCHA en la consola.";
                Log.e("AUTH_DEBUG", msg);
                errorLiveData.postValue(msg);
            }
        }, 12000);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AUTH_DEBUG", "¡CONECTADO CON ÉXITO!");
                        userLiveData.postValue(firebaseAuth.getCurrentUser());
                    } else {
                        String errorMsg = (task.getException() != null) ? task.getException().getMessage() : "Fallo desconocido";
                        Log.e("AUTH_DEBUG", "FIREBASE RESPONDIÓ: " + errorMsg);
                        errorLiveData.postValue(errorMsg);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AUTH_DEBUG", "ERROR DE RED/SEGURIDAD: " + e.getMessage());
                    errorLiveData.postValue("Fallo de conexión: " + e.getMessage());
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