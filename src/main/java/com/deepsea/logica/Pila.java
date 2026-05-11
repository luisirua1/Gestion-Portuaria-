package com.deepsea.logica;

public class Pila {
    private Contenedor[] elementos;
    private int top;
    private int capacidad;

    // Constructor: Define la altura máxima de contenedores permitida
    public Pila(int capacidad) {
        this.capacidad = capacidad;
        this.elementos = new Contenedor[capacidad];
        this.top = -1; // Pila vacía
    }

    // Push: Insertar un contenedor en la cima
    public void push(Contenedor contenedor) {
        if (top == capacidad - 1) {
            System.out.println("No se pueden apilar más contenedores aquí (Límite alcanzado).");
        } else {
            top++;
            elementos[top] = contenedor;
        }
    }

    // Pop: Eliminar y sacar el contenedor de la cima para descargarlo
    public Contenedor pop() {
        if (top == -1) {
            System.out.println("No hay contenedores en esta sección.");
            return null;
        } else {
            Contenedor contenedorDescargado = elementos[top];
            top--;
            return contenedorDescargado;
        }
    }

    // Peek: Ver cuál es el contenedor de arriba (necesario para el cálculo de
    // estabilidad)
    public Contenedor peek() {
        if (top == -1) {
            return null;
        } else {
            return elementos[top];
        }
    }

    // verCima: alias en español de peek() — ver la cima sin extraer
    public Contenedor verCima() {
        return peek();
    }

    // estaVacia: retorna true si la pila no tiene ningún contenedor
    public boolean estaVacia() {
        return top == -1;
    }

    // Método extra para saber el peso total de esta pila (útil para la validación
    // del 30%)
    public double getPesoTotal() {
        double pesoTotal = 0;
        for (int i = 0; i <= top; i++) {
            pesoTotal += elementos[i].getPeso();
        }
        return pesoTotal;
    }

    // Estos métodos permiten que el servidor web "vea" los datos para enviarlos al
    // HTML
    public int getTop() {
        return top;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public Contenedor[] getElementos() {
        return elementos;
    }
}