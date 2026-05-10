package com.deepsea.logica;

public class Movimiento {
    private Contenedor contenedor; // Guardamos el objeto real
    private Camion camion; // NUEVO: Guardamos el camión
    private int fila;
    private int columna;
    private String descripcion;
    private String fecha;

    public Movimiento(Contenedor c, Camion camion, int f, int col, String desc) {
        this.contenedor = c;
        this.camion = camion;
        this.fila = f;
        this.columna = col;
        this.descripcion = desc;
        this.fecha = new java.util.Date().toString();
    }

    // Getters para poder reconstruir la acción
    public Contenedor getContenedor() {
        return contenedor;
    }

    public Camion getCamion() {
        return camion;
    } // NUEVO GETTER

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }
}