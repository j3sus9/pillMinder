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

        // Crear la consulta:
        // - Filtra por el ID del usuario
        // - Ordena por el nombre del medicamento
        Query query = db.collection(COLLECTION_NAME)
                .whereEqualTo("usuarioId", userId);

        // Escuchar los cambios en tiempo real (Snapshot Listener)
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

    public void tomarMedicamento(Medicamento med, String tomaId) {
        if (med.getDocumentId() != null) {
            // Calculamos el nuevo stock
            int nuevoStock = med.getStockTotal() - med.getDosis();

            // Evitamos que el stock sea negativo por seguridad
            if (nuevoStock < 0) nuevoStock = 0;

            db.collection("medicamentos").document(med.getDocumentId())
                    .update(
                            "stockTotal", nuevoStock,
                            "ultimaTomaId", tomaId // Aquí guardamos el ID que bloquea el botón
                    )
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Toma registrada: " + tomaId))
                    .addOnFailureListener(e -> Log.e("Firebase", "Error al registrar toma", e));
        }
    }

    /**
     * Actualiza un medicamento existente en la base de datos.
     */
    public void updateMedicamento(Medicamento med) {
        if (med.getDocumentId() != null) {
            db.collection("medicamentos")
                    .document(med.getDocumentId())
                    .set(med) // Sobrescribe el documento con los nuevos datos del objeto
                    .addOnSuccessListener(aVoid -> {
                        Log.d("PillRepository", "Medicamento actualizado correctamente: " + med.getNombre());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("PillRepository", "Error al actualizar medicamento", e);
                    });
        } else {
            Log.e("PillRepository", "No se puede actualizar: el DocumentId es nulo");
        }
    }

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

    /**
     * Actualiza el stock de un medicamento en la base de datos.
     */
    public void updateStock(String documentId, int nuevoStock) {
        db.collection(COLLECTION_NAME)
                .document(documentId)
                .update("stockTotal", nuevoStock) // Asegúrate que el campo en Firebase se llame "stockTotal"
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Stock actualizado"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar stock", e));
    }
}