package com.deepsea.logica;

public class Nodo {
    int dato;
    Nodo siguiente;

    public Nodo(int dato) {
        this.dato = dato;
        this.siguiente = null;
        // Es buena práctica inicializarlo en null
    }
}