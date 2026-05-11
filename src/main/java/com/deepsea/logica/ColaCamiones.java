package com.deepsea.logica;

public class ColaCamiones {
    private Nodo<Camion> frente;
    private Nodo<Camion> fin;
    private int size;
    private int capacidad;

    public ColaCamiones(int capacidad) {
        this.capacidad = capacidad;
        this.frente = null;
        this.fin = null;
        this.size = 0;
    }

    public void encolar(Camion camion) {
        if (size >= capacidad)
            return;
        Nodo<Camion> nuevo = new Nodo<>(camion);
        if (frente == null) {
            frente = nuevo;
            fin = nuevo;
        } else {
            fin.setSiguiente(nuevo);
            fin = nuevo;
        }
        size++;
        System.out.println("Camión " + camion.getPlaca() + " en fila.");
    }

    public Camion desencolar() {
        if (frente == null)
            return null;
        Camion despachado = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null)
            fin = null;
        size--;
        return despachado;
    }

    public Camion verFrente() {
        return (frente == null) ? null : frente.getDato();
    }

    public int getSize() {
        return size;
    }

    public Camion[] getCamionesEnOrden() {
        Camion[] activos = new Camion[size];
        Nodo<Camion> actual = frente;
        int i = 0;
        while (actual != null) {
            activos[i++] = actual.getDato();
            actual = actual.getSiguiente();
        }
        return activos;
    }
}