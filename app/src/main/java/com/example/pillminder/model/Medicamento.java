package com.example.pillminder.model;

import java.util.ArrayList;
import java.util.List;

public class Medicamento implements java.io.Serializable{

    // Campo único para identificar el documento en Firebase (no es el ID del objeto Java)
    private String documentId;
    private String nombre;
    private List<String> horasToma; // Ejemplo: "08:00, 14:00, 22:00"
    private int dosis; // Cuántos comprimidos/ml debe tomar
    private String tipoDosis; // ml, pastillas, gotas, inhalación
    private int stockTotal;
    private String usuarioId; // ID del usuario logeado en Firebase Auth
    private String ultimaTomaId; // Formato sugerido: "20231024_08:00" (fecha_hora)


    // 1. Constructor vacío (OBLIGATORIO para que Firebase lea los datos)
    public Medicamento() {
        this.horasToma = new ArrayList<>();
    }

    // 2. Constructor normal para guardar datos
    public Medicamento(String nombre, List<String> horasToma, int dosis, int stockTotal, String usuarioId) {
        this.nombre = nombre;
        this.horasToma = horasToma;
        this.dosis = dosis;
        this.stockTotal = stockTotal;
        this.usuarioId = usuarioId;
    }

    // 3. Getters (Necesarios para que Firebase lea los campos)
    public String getNombre() { return nombre; }
    public List<String> getHorasToma() { return horasToma; }
    public int getDosis() { return dosis; }
    public String getTipoDosis() { return tipoDosis; }
    public int getStockTotal() { return stockTotal; }
    public String getUsuarioId() { return usuarioId; }
    public String getDocumentId() { return documentId; }
    public String getUltimaTomaId() {return ultimaTomaId; }

    // 4. Setters (Necesarios si queremos modificar el objeto antes de guardarlo o si se requiere el DocumentId)
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setHorasToma(List<String> horasToma) { this.horasToma = horasToma; }
    public void setDosis(int dosis) { this.dosis = dosis; }
    public void setTipoDosis(String tipoDosis) { this.tipoDosis = tipoDosis; }
    public void setStockTotal(int stockTotal) { this.stockTotal = stockTotal; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setUltimaTomaId(String ultimaTomaId) {this.ultimaTomaId = ultimaTomaId; }
}