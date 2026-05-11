package com.deepsea.logica;

public class HistorialPila {
    private Nodo<Movimiento> tope;
    private int size;
    private int capacidad;

    public HistorialPila(int capacidad) {
        this.capacidad = capacidad;
        this.tope = null;
        this.size = 0;
    }

    public void registrarAccion(Movimiento m) {
        if (size < capacidad) {
            Nodo<Movimiento> nuevo = new Nodo<>(m);
            nuevo.setSiguiente(tope);
            tope = nuevo;
            size++;
        }
    }

    public Movimiento pop() {
        if (tope == null)
            return null;
        Movimiento extraido = tope.getDato();
        tope = tope.getSiguiente();
        size--;
        return extraido;
    }

    public Movimiento[] getLog() {
        Movimiento[] arregloWeb = new Movimiento[size];
        Nodo<Movimiento> actual = tope;
        for (int i = size - 1; i >= 0; i--) {
            arregloWeb[i] = actual.getDato();
            actual = actual.getSiguiente();
        }
        return arregloWeb;
    }

    public int getTop() {
        return size - 1;
    }

    public void mostrarHistorial() {
        Nodo<Movimiento> actual = tope;
        int i = size;
        while (actual != null) {
            System.out.println(i + ". " + actual.getDato().getDescripcion());
            actual = actual.getSiguiente();
            i--;
        }
    }
}