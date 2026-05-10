package com.deepsea.logica;

public class CalculadoraPortuaria {

    // PUNTO B: Consumo de Combustible del Camión
    public static double calcularConsumoCombustible(double pesoContenedor, double distanciaKm) {
        double consumoBase = 5.0; // Galones base por encender el camión
        double coeficienteFriccion = 0.02; // Constante dada
        double pesoCamionVacio = 15.0; // El camión pesa 15t por sí solo

        double pesoTotal = pesoCamionVacio + pesoContenedor;

        // Fórmula de la rúbrica: C_base + (PesoTotal * Coeficiente * Distancia)
        double consumo = consumoBase + (pesoTotal * coeficienteFriccion * distanciaKm);
        return consumo;
    }

    // PUNTO C: Tiempo Estimado de Grúa (Función Cuadrática)
    public static double calcularTiempoGrua(int alturaEnPila, double pesoContenedor) {
        // La grúa tarda más si tiene que bajar hasta el fondo del barco.
        // Altura 4 (el tope) es rápido. Altura 0 (el fondo) tarda mucho.
        int distanciaABajar = 5 - alturaEnPila;

        // Función Cuadrática (a * x^2 + b * y)
        // Elevamos la distancia al cuadrado porque la grúa debe acelerar y frenar
        double tiempoSegundos = (Math.pow(distanciaABajar, 2) * 1.5) + (pesoContenedor * 0.2);

        return tiempoSegundos;
    }
}