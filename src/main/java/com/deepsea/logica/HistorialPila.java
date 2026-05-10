package com.deepsea.logica;

public class HistorialPila {
    private Movimiento[] log; // Antes era String[], ahora es Movimiento[]
    private int top;

    public HistorialPila(int capacidad) {
        this.log = new Movimiento[capacidad];
        this.top = -1;
    }

    // Ahora recibe un objeto Movimiento completo
    public void registrarAccion(Movimiento m) {
        if (top < log.length - 1) {
            log[++top] = m;
        }
    }

    public Movimiento pop() {
        if (top >= 0) {
            return log[top--];
        }
        return null;
    }

    // Getters para que el HTML pueda leer los datos
    public Movimiento[] getLog() {
        return log;
    }

    public int getTop() {
        return top;
    }

    public void mostrarHistorial() {
        if (top == -1) {
            System.out.println("El historial está vacío.");
            return;
        }
        System.out.println("\n--- HISTORIAL DE MOVIMIENTOS ---");
        for (int i = top; i >= 0; i--) {
            Movimiento m = log[i];
            System.out.println((i + 1) + ". " + m.getDescripcion() + " en (" + m.getFila() + ", " + m.getColumna() + ")");
        }
    }
}