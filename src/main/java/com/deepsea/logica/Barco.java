package com.deepsea.logica;

public class Barco {
    private Pila[][] secciones;
    private final int FILAS = 5;
    private final int COLUMNAS = 5;

    // --- SISTEMA DE LASTRE MANUAL ---
    private double lastreIzquierdo = 0.0;
    private double lastreDerecho = 0.0;
    private final double MAX_LASTRE = 50.0;

    public Barco() {
        secciones = new Pila[FILAS][COLUMNAS];
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                secciones[i][j] = new Pila(10);
            }
        }
    }

    public double calcularPesoLado(String lado) {
        double pesoTotal = 0;
        int inicioCol = lado.equalsIgnoreCase("izquierdo") ? 0 : 3;
        int finCol = lado.equalsIgnoreCase("izquierdo") ? 1 : 4;

        for (int i = 0; i < FILAS; i++) {
            for (int j = inicioCol; j <= finCol; j++) {
                pesoTotal += secciones[i][j].getPesoTotal();
            }
        }
        return pesoTotal;
    }

    public boolean esEstable() {
        double momentoIzquierdo = 0;
        double momentoDerecho = 0;

        for (int f = 0; f < 5; f++) {
            momentoIzquierdo += sumarPesoPila(f, 0) * 2.0;
            momentoIzquierdo += sumarPesoPila(f, 1) * 1.0;

            momentoDerecho += sumarPesoPila(f, 3) * 1.0;
            momentoDerecho += sumarPesoPila(f, 4) * 2.0;
        }

        // Sumamos el torque del agua de lastre
        momentoIzquierdo += (lastreIzquierdo * 2.0);
        momentoDerecho += (lastreDerecho * 2.0);

        double diferenciaMomento = Math.abs(momentoIzquierdo - momentoDerecho);
        double momentoMayor = Math.max(momentoIzquierdo, momentoDerecho);

        if (momentoMayor == 0)
            return true;
        return (diferenciaMomento / momentoMayor) <= 0.30;
    }

    public double sumarPesoPila(int f, int c) {
        if (f >= 0 && f < FILAS && c >= 0 && c < COLUMNAS) {
            return secciones[f][c].getPesoTotal();
        }
        return 0;
    }

    public void cargarContenedor(int f, int c, Contenedor con) {
        if (con == null)
            return;
        if (f >= 0 && f < FILAS && c >= 0 && c < COLUMNAS) {
            secciones[f][c].push(con);
        }
    }

    // DESCARGA INTELIGENTE (Validación a posteriori)
    public Contenedor descargarContenedor(int f, int c) {
        if (secciones[f][c].getTop() == -1)
            return null; // Pila vacía

        Contenedor extraido = secciones[f][c].pop(); // Simulamos sacarlo

        if (!esEstable()) {
            secciones[f][c].push(extraido); // Revertimos si hay peligro
            return null;
        }
        return extraido;
    }

    public Pila[][] getSecciones() {
        return secciones;
    }

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
        if (izquierdo)
            momento += (lastreIzquierdo * 2.0);
        else
            momento += (lastreDerecho * 2.0);
        return momento;
    }

    // --- MÉTODOS DE BOMBEO DE LASTRE MANUAL ---
    public boolean bombearLastre(boolean esIzq, double cantidad) {
        return ajustarLastre(esIzq, cantidad);
    }

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

    public double getLastreIzq() {
        return lastreIzquierdo;
    }

    public double getLastreDer() {
        return lastreDerecho;
    }
}