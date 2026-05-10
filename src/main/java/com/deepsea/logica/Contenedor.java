package com.deepsea.logica;

// Clase base abstracta (Superclase)
public abstract class Contenedor {
    private String id;
    private double peso; // En toneladas

    public Contenedor(String id, double peso) {
        this.id = id;
        setPeso(peso); // Usamos el setter para evitar pesos inválidos
    }

    // Encapsulamiento: Validamos que el peso no sea negativo
    public void setPeso(double peso) {
        if (peso > 0) {
            this.peso = peso;
        } else {
            this.peso = 1.0; // Peso mínimo por defecto si el usuario ingresa un error
        }
    }

    public double getPeso() {
        return peso;
    }

    public String getId() {
        return id;
    }

    // Polimorfismo: Cada tipo de contenedor calculará su costo/tiempo de forma
    // diferente
    public abstract double calcularCostoTransporte(double distancia);
}