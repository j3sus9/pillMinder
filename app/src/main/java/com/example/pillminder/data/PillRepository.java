package com.example.pillminder.data;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.example.pillminder.model.Medicamento;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;
import java.util.ArrayList;

public class PillRepository {

    private static final String TAG = "PillRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "medicamentos";

    // ----------------------------------------------------
    // 1. OBTENCIÓN DE DATOS (READ)
    // ----------------------------------------------------

    /**
     * Obtiene la lista de medicamentos de un usuario en tiempo real.
     * Utiliza LiveData para notificar a la View sobre los cambios.
     */
    public MutableLiveData<List<Medicamento>> getPillsForUser(String userId) {
        MutableLiveData<List<Medicamento>> livePills = new MutableLiveData<>();

        // 1. Crear la consulta:
        // - Filtra por el ID del usuario
        // - Ordena por el nombre del medicamento (opcional, pero útil)
        Query query = db.collection(COLLECTION_NAME)
                .whereEqualTo("usuarioId", userId)
                .orderBy("nombre", Query.Direction.ASCENDING);

        // 2. Escuchar los cambios en tiempo real (Snapshot Listener)
        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Error al escuchar cambios en Firestore.", error);
                livePills.setValue(null); // Opcional: indicar error
                return;
            }

            List<Medicamento> pills = new ArrayList<>();
            if (value != null) {
                for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                    // Mapea el documento de Firestore a nuestro objeto Medicamento.java
                    Medicamento pill = doc.toObject(Medicamento.class);
                    if (pill != null) {
                        // Guardamos el ID del documento, que será útil para eliminar/modificar
                        pill.setDocumentId(doc.getId());
                        pills.add(pill);
                    }
                }
            }
            livePills.setValue(pills); // Notifica a todos los observadores (ViewModel -> Activity)
        });

        return livePills;
    }

    // ----------------------------------------------------
    // 2. INSERCIÓN DE DATOS (CREATE)
    // ----------------------------------------------------

    /**
     * Guarda un nuevo medicamento en la base de datos.
     */
    public void addPill(Medicamento medicamento) {
        db.collection(COLLECTION_NAME)
                .add(medicamento)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Medicamento añadido. ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al añadir medicamento", e));
    }

    // ----------------------------------------------------
    // 3. ELIMINACIÓN DE DATOS (DELETE)
    // ----------------------------------------------------

    /**
     * Elimina un medicamento de la base de datos usando su DocumentId.
     */
    public void deletePill(String documentId) {
        if (documentId == null || documentId.isEmpty()) {
            Log.e(TAG, "Error: DocumentId es nulo o vacío para eliminar.");
            return;
        }
        db.collection(COLLECTION_NAME)
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Medicamento eliminado con éxito: " + documentId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al eliminar medicamento: " + documentId, e));
    }

    // NOTA: Más adelante, podríamos añadir métodos para updatePill() y recordPillTaken().
}