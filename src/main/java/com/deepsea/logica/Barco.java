package com.deepsea.logica;

public class Barco {
    // Una matriz donde cada celda es una Pila de contenedores
    private Pila[][] secciones;
    private final int FILAS = 5;
    private final int COLUMNAS = 5;

    // --- NUEVO: SISTEMA DE LASTRE ---
    private double lastreIzquierdo = 0.0;
    private double lastreDerecho = 0.0;
    private final double MAX_LASTRE = 50.0; // 50 toneladas máximo por tanque

    public Barco() {
        // Declaramos la matriz según la teoría de Java [cite: 877]
        secciones = new Pila[FILAS][COLUMNAS];

        // Inicializamos cada celda con una nueva Pila (capacidad 10) [cite: 918]
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                secciones[i][j] = new Pila(10);
            }
        }
    }

    // Calcula el peso total de un lado del barco para la regla del 30%
    public double calcularPesoLado(String lado) {
        double pesoTotal = 0;
        // Lado izquierdo: columnas 0 y 1. Lado derecho: columnas 3 y 4
        int inicioCol = lado.equalsIgnoreCase("izquierdo") ? 0 : 3;
        int finCol = lado.equalsIgnoreCase("izquierdo") ? 1 : 4;

        for (int i = 0; i < FILAS; i++) {
            for (int j = inicioCol; j <= finCol; j++) {
                pesoTotal += secciones[i][j].getPesoTotal();
            }
        }
        return pesoTotal;
    }

    // Valida si la operación es segura
    // PUNTO A: CÁLCULO DE ESTABILIDAD POR MOMENTOS (M = m * d)
    public boolean esEstable() {
        double momentoIzquierdo = 0;
        double momentoDerecho = 0;

        // Recorremos todas las filas
        for (int f = 0; f < 5; f++) {
            // LADO IZQUIERDO
            momentoIzquierdo += sumarPesoPila(f, 0) * 2.0; // Columna 0: Distancia 2
            momentoIzquierdo += sumarPesoPila(f, 1) * 1.0; // Columna 1: Distancia 1

            // CENTRO (Columna 2): Distancia 0, por lo tanto el momento es 0, no afecta.

            // LADO DERECHO
            momentoDerecho += sumarPesoPila(f, 3) * 1.0; // Columna 3: Distancia 1
            momentoDerecho += sumarPesoPila(f, 4) * 2.0; // Columna 4: Distancia 2
        }

        // --- NUEVO: EL AGUA DE LASTRE GENERA TORQUE EN EL EXTREMO (Distancia 2) ---
        momentoIzquierdo += (lastreIzquierdo * 2.0);
        momentoDerecho += (lastreDerecho * 2.0);

        double diferenciaMomento = Math.abs(momentoIzquierdo - momentoDerecho);
        double momentoMayor = Math.max(momentoIzquierdo, momentoDerecho);

        if (momentoMayor == 0)
            return true; // Barco vacío

        // Verificamos si la diferencia de momento supera el 30%
        return (diferenciaMomento / momentoMayor) <= 0.30;
    }

    // Obtiene el peso total de una pila específica
    public double sumarPesoPila(int f, int c) {
        if (f >= 0 && f < FILAS && c >= 0 && c < COLUMNAS) {
            return secciones[f][c].getPesoTotal();
        }
        return 0;
    }

    // Método para agregar un contenedor en una posición específica
    public void cargarContenedor(int f, int c, Contenedor con) {
        if (f >= 0 && f < FILAS && c >= 0 && c < COLUMNAS) {
            secciones[f][c].push(con);
        }
    }

    // Método para descargar (Pop) validando estabilidad [cite: 980, 981, 992]
    public Contenedor descargarContenedor(int f, int c) {
        if (!esEstable()) {
            System.out.println("RIESGO DE VOLCAMIENTO: Operación bloqueada.");
            return null;
        }
        return secciones[f][c].pop();
    }

    public Pila[][] getSecciones() {
        return secciones;
    }

    // Método para la interfaz web (devuelve el momento exacto)
    public double calcularMomentoLado(boolean izquierdo) {
        double momento = 0;
        for (int f = 0; f < 5; f++) {
            if (izquierdo) {
                momento += sumarPesoPila(f, 0) * 2.0;
                momento += sumarPesoPila(f, 1) * 1.0;
            } else {
                momento += sumarPesoPila(f, 3) * 1.0;
                momento += sumarPesoPila(f, 4) * 2.0;
            }
        }
        // Sumar el lastre
        if (izquierdo)
            momento += (lastreIzquierdo * 2.0);
        else
            momento += (lastreDerecho * 2.0);

        return momento;
    }

    // --- MÉTODOS DE BOMBEO ---
    public boolean bombearLastre(boolean alIzquierdo, double toneladas) {
        if (alIzquierdo) {
            if (lastreIzquierdo + toneladas > MAX_LASTRE)
                return false;
            lastreIzquierdo += toneladas;
        } else {
            if (lastreDerecho + toneladas > MAX_LASTRE)
                return false;
            lastreDerecho += toneladas;
        }
        return true;
    }

    public double getLastreIzq() {
        return lastreIzquierdo;
    }

    public double getLastreDer() {
        return lastreDerecho;
    }

    // --- LÓGICA DE CONTROL MANUAL DE LASTRE ---
    public boolean ajustarLastre(boolean esIzq, double cantidad) {
        if (esIzq) {
            double nuevoValor = lastreIzquierdo + cantidad;
            if (nuevoValor < 0 || nuevoValor > MAX_LASTRE)
                return false;
            lastreIzquierdo = nuevoValor;
        } else {
            double nuevoValor = lastreDerecho + cantidad;
            if (nuevoValor < 0 || nuevoValor > MAX_LASTRE)
                return false;
            lastreDerecho = nuevoValor;
        }
        return true;
    }

    public void resetLastre(boolean esIzq) {
        if (esIzq)
            lastreIzquierdo = 0;
        else
            lastreDerecho = 0;
    }
}