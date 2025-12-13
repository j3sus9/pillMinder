package com.example.pillminder;

public class Medicamento {
    private String nombre;
    private String horaToma;
    private int stock;
    // IMPORTANTE: Para identificar de qué alumno es el dato (provisionalmente)
    private String usuarioId;

    // 1. Constructor vacío (OBLIGATORIO para que Firebase lea los datos)
    public Medicamento() {}

    // 2. Constructor normal para guardar datos
    public Medicamento(String nombre, String horaToma, int stock, String usuarioId) {
        this.nombre = nombre;
        this.horaToma = horaToma;
        this.stock = stock;
        this.usuarioId = usuarioId;
    }

    // 3. Getters (Necesarios para que Firebase lea los campos)
    public String getNombre() { return nombre; }
    public String getHoraToma() { return horaToma; }
    public int getStock() { return stock; }
    public String getUsuarioId() { return usuarioId; }
}