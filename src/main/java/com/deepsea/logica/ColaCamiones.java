package com.deepsea.logica;

public class ColaCamiones {
    private Camion[] cola;
    private int front;
    private int rear;
    private int size;
    private int capacidad;

    // Constructor
    public ColaCamiones(int capacidad) {
        this.capacidad = capacidad;
        this.cola = new Camion[capacidad];
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }

    // ENQUEUE: Registrar la llegada de un camión (Insertar)
    public void encolar(Camion camion) {
        if (size == capacidad) {
            System.out.println("Cola llena (Overflow). No hay espacio para más camiones.");
            return;
        }

        // Avanza rear manualmente
        rear = rear + 1;

        // Si se pasa del final, vuelve al inicio (Lógica Circular del PDF)
        if (rear == capacidad) {
            rear = 0;
        }

        cola[rear] = camion;
        size++;
        System.out.println("Camión " + camion.getPlaca() + " en fila.");
    }

    // DEQUEUE: Despachar el camión (Eliminar del frente)
    public Camion desencolar() {
        if (size == 0) {
            System.out.println("No hay camiones esperando (Underflow).");
            return null;
        }

        Camion despachado = cola[front]; // Guardamos el camión antes de mover el frente
        front = (front + 1) % capacidad;
        size--;
        return despachado; // Entregamos el camión al Main
    }

    // PEEK: Ver qué camión sigue sin quitarlo de la fila
    public Camion verFrente() {
        if (size == 0) {
            return null;
        }
        return cola[front];
    }

    public int getSize() {
        return size;
    }

    // Método para el HTML: Devuelve los camiones en orden desde el primero hasta el
    // último
    // Método para el HTML: Devuelve los camiones en orden
    public Camion[] getCamionesEnOrden() {
        Camion[] activos = new Camion[this.size];
        int actual = this.front;

        for (int i = 0; i < this.size; i++) {
            activos[i] = this.cola[actual];
            actual = (actual + 1) % this.cola.length; // Avanza en la cola circular
        }

        return activos;
    }
}