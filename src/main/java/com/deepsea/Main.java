package com.deepsea;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import com.deepsea.logica.*;
import com.google.gson.Gson;

import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Inicialización de Estructuras
        Barco miBarco = new Barco();
        ColaCamiones filaCamiones = new ColaCamiones(10);
        HistorialPila auditoria = new HistorialPila(100);

        // --- CARGA MAESTRA: ESTABLE Y OPERATIVA ---
        miBarco.cargarContenedor(0, 0, new ContenedorEstandar("LIG-IZQ", 5.0));
        miBarco.cargarContenedor(4, 4, new ContenedorEstandar("LIG-DER", 5.0));
        miBarco.cargarContenedor(0, 1, new ContenedorEstandar("MED-IZQ", 15.0));
        miBarco.cargarContenedor(4, 3, new ContenedorEstandar("MED-DER", 15.0));
        miBarco.cargarContenedor(2, 2, new ContenedorEstandar("PESADO-1", 40.0));
        miBarco.cargarContenedor(1, 2, new ContenedorEstandar("PESADO-2", 40.0));
        miBarco.cargarContenedor(0, 2, new ContenedorEstandar("PESADO-3", 40.0));

        filaCamiones.encolar(new Camion("PRUEBA-01"));

        // ---------------------------------------------------------
        // 2. CONFIGURACIÓN DEL SERVIDOR JAVALIN
        // ---------------------------------------------------------
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(8080);

        Gson gson = new Gson();

        // RUTA 1: BARCO (Traduce los Nodos a la Matriz antigua)
        app.get("/api/barco", ctx -> {
            Pila[][] matriz = miBarco.getSecciones();
            PilaDTO[][] matrizWeb = new PilaDTO[5][5];

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    matrizWeb[i][j] = new PilaDTO(matriz[i][j].getTop(), matriz[i][j].getElementos());
                }
            }
            ctx.result(gson.toJson(matrizWeb));
        });

        // RUTA 2: ESTABILIDAD (Incluye los lastres)
        app.get("/api/estabilidad", ctx -> {
            String jsonResponse = String.format(Locale.US,
                    "{\"izquierdo\": %.2f, \"derecho\": %.2f, \"esEstable\": %b, \"lastreIzq\": %.2f, \"lastreDer\": %.2f}",
                    miBarco.calcularMomentoLado(true), miBarco.calcularMomentoLado(false), miBarco.esEstable(),
                    miBarco.getLastreIzq(), miBarco.getLastreDer());
            ctx.result(jsonResponse);
        });

        // RUTA 3: HISTORIAL (Traduce los Nodos)
        app.get("/api/historial", ctx -> {
            ctx.result(gson.toJson(new HistorialDTO(auditoria.getTop(), auditoria.getLog())));
        });

        // RUTA 4: CAMIONES
        app.get("/api/camiones", ctx -> {
            ctx.result(gson.toJson(filaCamiones.getCamionesEnOrden()));
        });

        // RUTAS NUEVAS PARA LOS BOTONES DEL LASTRE EN LA WEB
        app.post("/api/lastre/ajustar", ctx -> {
            boolean esIzq = ctx.queryParam("lado").equals("izq");
            double cantidad = Double.parseDouble(ctx.queryParam("valor"));
            miBarco.ajustarLastre(esIzq, cantidad);
            ctx.status(200);
        });

        app.post("/api/lastre/reset", ctx -> {
            boolean esIzq = ctx.queryParam("lado").equals("izq");
            miBarco.resetLastre(esIzq);
            ctx.status(200);
        });

        // ---------------------------------------------------------
        // 3. Menú Principal por Consola
        // ---------------------------------------------------------
        Scanner teclado = new Scanner(System.in);
        int opcion = 0;

        while (opcion != 8) {
            System.out.println("\n--- SISTEMA PORTUARIO DEEPSEA ---");
            System.out.println("1. Registrar llegada de Camiones");
            System.out.println("2. Visualizar estado del Barco (Consola)");
            System.out.println("3. Descargar Contenedor");
            System.out.println("4. Ver historial (Auditoria)");
            System.out.println("5. Reporte de Estabilidad");
            System.out.println("6. Deshacer último movimiento");
            System.out.println("7. Cargar Contenedor al Barco (Manual)");
            System.out.println("8. Salir");
            System.out.print("Seleccione una operacion : ");

            opcion = teclado.nextInt();

            switch (opcion) {
                case 1:
                    System.out.print("Ingrese la placa del camión: ");
                    String placa = teclado.next();
                    filaCamiones.encolar(new Camion(placa));
                    break;

                case 2:
                    visualizarConsola(miBarco);
                    break;

                case 3:
                    System.out.println("\n--- OPERACIÓN DE DESCARGA ---");
                    Camion camionActual = filaCamiones.verFrente();
                    if (camionActual == null) {
                        System.out.println("ERROR: No hay camiones en la cola. No se puede descargar.");
                        break;
                    }

                    System.out.print("Fila del contenedor (0-4): ");
                    int f = teclado.nextInt();
                    System.out.print("Columna del contenedor (0-4): ");
                    int c = teclado.nextInt();

                    int alturaReal = miBarco.getSecciones()[f][c].getTop();
                    Contenedor descargado = miBarco.descargarContenedor(f, c);

                    if (descargado != null) {
                        Camion camionAsignado = filaCamiones.desencolar();
                        double tiempoGrua = CalculadoraPortuaria.calcularTiempoGrua(alturaReal, descargado.getPeso());
                        double consumo = CalculadoraPortuaria.calcularConsumoCombustible(descargado.getPeso(), 50.0);

                        String reporte = String.format(
                                "Descarga %s | Grúa: %.1fs | Consumo: %.1f gal | Camión: %s",
                                descargado.getId(), tiempoGrua, consumo, camionAsignado.getPlaca());

                        Movimiento mov = new Movimiento(descargado, camionAsignado, f, c, reporte);
                        auditoria.registrarAccion(mov);
                        System.out.println(">>> OPERACIÓN REGISTRADA: " + reporte);
                    } else {
                        System.out.println(">>> ❌ ERROR: OPERACIÓN DENEGADA.");
                        System.out.println(
                                ">>> Posibles causas: La pila está vacía O sacar este contenedor genera un RIESGO DE VOLCAMIENTO (>30%).");

                        // --- LA MAGIA: ENVIAMOS EL ERROR AL HISTORIAL DE LA WEB ---
                        String mensajeAlerta = "❌ BLOQUEO: Descarga denegada en (" + f + "," + c
                                + ") por riesgo de volcamiento.";
                        Movimiento movError = new Movimiento(null, null, f, c, mensajeAlerta);
                        auditoria.registrarAccion(movError);
                    }
                    break;

                case 4:
                    auditoria.mostrarHistorial();
                    break;

                case 5:
                    System.out.println("\n--- REPORTE TÉCNICO ---");
                    double pI = miBarco.calcularMomentoLado(true);
                    double pD = miBarco.calcularMomentoLado(false);
                    System.out.println("Momentos: Izquierdo [" + pI + "] vs Derecho [" + pD + "]");
                    System.out.println("Estado de Estabilidad: " + (miBarco.esEstable() ? "SEGURO" : "RIESGO CRÍTICO"));
                    break;

                case 6:
                    System.out.println("\n--- DESHACIENDO ÚLTIMA ACCIÓN ---");
                    Movimiento ultimo = auditoria.pop();
                    if (ultimo != null) {
                        miBarco.cargarContenedor(ultimo.getFila(), ultimo.getColumna(), ultimo.getContenedor());
                        if (ultimo.getCamion() != null)
                            filaCamiones.encolar(ultimo.getCamion());
                        System.out.println("REVERTIDO: " + ultimo.getDescripcion());
                    } else {
                        System.out.println("No hay movimientos para deshacer.");
                    }
                    break;

                case 7:
                    System.out.println("\n--- INGRESO DE NUEVA CARGA ---");
                    System.out.print("Ingrese ID del contenedor (Ej: C-100): ");
                    String id = teclado.next();
                    System.out.print("Ingrese el peso en toneladas: ");
                    double peso = teclado.nextDouble();
                    System.out.print("Fila de destino (0-4): ");
                    int fila = teclado.nextInt();
                    System.out.print("Columna de destino (0-4): ");
                    int col = teclado.nextInt();

                    Contenedor nuevo = new ContenedorEstandar(id, peso);
                    miBarco.cargarContenedor(fila, col, nuevo);
                    System.out.println(">>> ÉXITO: Contenedor " + id + " cargado.");
                    break;

                case 8:
                    System.out.println("Cerrando simulador DeepSea...");
                    app.stop();
                    break;
            }
        }
        teclado.close();
    }

    // --- CLASES DTO (Data Transfer Objects) ---
    // ¡AHORA ESTÁN FUERA DEL MAIN Y SON ESTÁTICAS! GSON LAS LEERÁ PERFECTO.
    static class PilaDTO {
        int top;
        Contenedor[] elementos;

        public PilaDTO(int top, Contenedor[] elementos) {
            this.top = top;
            this.elementos = elementos;
        }
    }

    static class HistorialDTO {
        int top;
        Movimiento[] log;

        public HistorialDTO(int top, Movimiento[] log) {
            this.top = top;
            this.log = log;
        }
    }

    private static void visualizarConsola(Barco b) {
        Pila[][] s = b.getSecciones();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.print("[" + s[i][j].getPesoTotal() + "t]\t");
            }
            System.out.println();
        }
    }
}