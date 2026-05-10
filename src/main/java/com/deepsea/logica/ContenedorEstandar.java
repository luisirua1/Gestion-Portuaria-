package com.deepsea.logica;

public class ContenedorEstandar extends Contenedor {

    public ContenedorEstandar(String id, double peso) {
        super(id, peso);
    }

    @Override
    public double calcularCostoTransporte(double distancia) {
        return getPeso() * 10 * distancia; // Fórmula básica de ejemplo
    }
}