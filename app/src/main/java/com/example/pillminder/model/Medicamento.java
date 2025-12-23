package com.example.pillminder.model;

public class Medicamento {

    // Campo único para identificar el documento en Firebase (no es el ID del objeto Java)
    private String documentId;

    private String nombre;
    private String horaToma; // Ejemplo: "08:00, 14:00, 22:00"
    private int dosis; // Cuántos comprimidos/ml debe tomar
    private String tipoDosis; // ml, pastillas, gotas, inhalación
    private int stockTotal;
    private String usuarioId; // ID del usuario logeado en Firebase Auth

    // 1. Constructor vacío (OBLIGATORIO para que Firebase lea los datos)
    public Medicamento() {}

    // 2. Constructor normal para guardar datos
    public Medicamento(String nombre, String horaToma, int dosis, int stockTotal, String usuarioId) {
        this.nombre = nombre;
        this.horaToma = horaToma;
        this.dosis = dosis;
        this.stockTotal = stockTotal;
        this.usuarioId = usuarioId;
    }

    // 3. Getters (Necesarios para que Firebase lea los campos)
    public String getNombre() { return nombre; }
    public String getHoraToma() { return horaToma; }
    public int getDosis() { return dosis; }
    public String getTipoDosis() { return tipoDosis; }
    public int getStockTotal() { return stockTotal; }
    public String getUsuarioId() { return usuarioId; }
    public String getDocumentId() { return documentId; }

    // 4. Setters (Necesarios si queremos modificar el objeto antes de guardarlo o si se requiere el DocumentId)
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setHoraToma(String horaToma) { this.horaToma = horaToma; }
    public void setDosis(int dosis) { this.dosis = dosis; }
    public void setTipoDosis(String tipoDosis) { this.tipoDosis = tipoDosis; }
    public void setStockTotal(int stockTotal) { this.stockTotal = stockTotal; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}