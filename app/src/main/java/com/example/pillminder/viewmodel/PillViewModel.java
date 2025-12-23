package com.example.pillminder.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pillminder.data.AuthRepository;
import com.example.pillminder.data.PillRepository;
import com.example.pillminder.model.Medicamento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class PillViewModel extends ViewModel {

    private final PillRepository pillRepository;
    private final AuthRepository authRepository;

    // Usamos MediatorLiveData para poder reaccionar a la fuente del repositorio
    private final MediatorLiveData<List<Medicamento>> medicamentosLiveData;

    // Almacena el ID del usuario actual
    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();

    public PillViewModel() {
        this.pillRepository = new PillRepository();
        this.authRepository = new AuthRepository();
        this.medicamentosLiveData = new MediatorLiveData<>();

        // ------------------------------------------------------------------
        // CORRECCIÓN CLAVE PARA QUE LA LISTA CARGUE AL INICIO
        // ------------------------------------------------------------------
        // En lugar de esperar al AuthRepository (que puede tener un retraso),
        // preguntamos directamente a la instancia de FirebaseAuth que ya tiene
        // la sesión en memoria caché.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            currentUserId.setValue(userId);

            // Conectamos la fuente de datos del repositorio al LiveData del ViewModel
            medicamentosLiveData.addSource(
                    pillRepository.getPillsForUser(userId),
                    medicamentosLiveData::setValue
            );
        } else {
            // Si no hay usuario, la lista está vacía
            medicamentosLiveData.setValue(null);
        }
    }

    // ----------------------------------------------------
    // MÉTODOS PÚBLICOS
    // ----------------------------------------------------

    public LiveData<List<Medicamento>> getMedicamentos() {
        return medicamentosLiveData;
    }

    /**
     * Añade un medicamento asegurándose de tener el ID de usuario correcto.
     */
    public void addMedicamento(Medicamento medicamento) {
        String userId = null;

        // Intentamos obtener el ID de la memoria local del ViewModel
        if (currentUserId.getValue() != null) {
            userId = currentUserId.getValue();
        }

        // Si falla, lo pedimos directamente a Firebase (Red de seguridad)
        if (userId == null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userId = user.getUid();
                // Actualizamos nuestra variable local también
                currentUserId.setValue(userId);
            }
        }

        // Guardamos
        if (userId != null) {
            medicamento.setUsuarioId(userId);
            pillRepository.addPill(medicamento);
        } else {
            System.out.println("ERROR CRÍTICO: No se pudo identificar al usuario al intentar guardar.");
        }
    }

    public void tomarMedicamento(Medicamento medicamento) {
        if (medicamento.getStockTotal() > 0) {
            int nuevoStock = medicamento.getStockTotal() - 1;
            pillRepository.updateStock(medicamento.getDocumentId(), nuevoStock);
        }
    }

    public void deleteMedicamento(String documentId) {
        pillRepository.deletePill(documentId);
    }

    public String getCurrentUserId() {
        return currentUserId.getValue();
    }
}