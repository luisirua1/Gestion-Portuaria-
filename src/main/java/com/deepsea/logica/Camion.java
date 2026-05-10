package com.deepsea.logica;

public class Camion {
    private String placa;
    private Contenedor carga; // El contenedor que se le asignará

    public Camion(String placa) {
        this.placa = placa;
        this.carga = null; // Llega vacío
    }

    public String getPlaca() {
        return placa;
    }

    public void setCarga(Contenedor carga) {
        this.carga = carga;
    }

    public Contenedor getCarga() {
        return carga;
    }
}