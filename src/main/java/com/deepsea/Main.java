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

        // Carga estos contenedores de prueba para que el HTML no se vea vacío

        // EXTREMOS (Livianos - Distancia 2)
        miBarco.cargarContenedor(0, 0, new ContenedorEstandar("LIG-IZQ", 5.0));
        miBarco.cargarContenedor(4, 4, new ContenedorEstandar("LIG-DER", 5.0));

        // INTERMEDIOS (Medios - Distancia 1)
        miBarco.cargarContenedor(0, 1, new ContenedorEstandar("MED-IZQ", 15.0));
        miBarco.cargarContenedor(4, 3, new ContenedorEstandar("MED-DER", 15.0));

        // CENTRO (Pesados - Distancia 0)
        miBarco.cargarContenedor(2, 2, new ContenedorEstandar("PESADO-1", 40.0));
        miBarco.cargarContenedor(1, 2, new ContenedorEstandar("PESADO-2", 40.0));
        miBarco.cargarContenedor(0, 2, new ContenedorEstandar("PESADO-3", 40.0));

        // Registramos un camión para que la cola no esté vacía
        filaCamiones.encolar(new Camion("PRUEBA-01"));

        // ---------------------------------------------------------
        // 2. CONFIGURACIÓN DEL SERVIDOR (Aquí es donde va el app.get)
        // ---------------------------------------------------------
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(8080);

        // ESTA ES LA PARTE QUE NO ENCONTRABAS:
        // Crea el "puente" para que el JavaScript del HTML reciba la matriz del barco
        // Ruta para ver la estabilidad (Pesos de los lados)
        // Esto hará que el import de Gson deje de estar sombreado
        // 1. Asegúrate de tener esta instancia de Gson creada una sola vez
        Gson gson = new Gson();

        // 2. Cambia tus rutas app.get por estas exactamente:
        app.get("/api/barco", ctx -> {
            // Usamos .result() en lugar de .json() para enviar el texto ya traducido
            ctx.result(gson.toJson(miBarco.getSecciones()));
        });

        // Ruta para el reporte de estabilidad
        // Busca esto en tu Main.java y actualízalo para enviar MOMENTOS
        app.get("/api/estabilidad", ctx -> {
            double momentoIzq = miBarco.calcularMomentoLado(true);
            double momentoDer = miBarco.calcularMomentoLado(false);
            boolean esEstable = miBarco.esEstable();

            // Enviamos el json incluyendo el lastre
            String jsonResponse = String.format(
                    Locale.US,
                    "{\"izquierdo\": %.2f, \"derecho\": %.2f, \"esEstable\": %b, \"lastreIzq\": %.2f, \"lastreDer\": %.2f}",
                    momentoIzq, momentoDer, esEstable, miBarco.getLastreIzq(), miBarco.getLastreDer());
            ctx.result(jsonResponse);
        });

        app.get("/api/historial", ctx -> {
            ctx.result(gson.toJson(auditoria));
        });

        // Ruta para ver la cola de camiones
        app.get("/api/camiones", ctx -> {
            ctx.result(gson.toJson(filaCamiones.getCamionesEnOrden()));
        });

        // Rutas para los botones (+, -, R)
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

        // 3. Menú Principal por Consola (Sigue igual)
        Scanner teclado = new Scanner(System.in);
        int opcion = 0;

        // Ahora el ciclo se rompe con el 7
        while (opcion != 9) {
            System.out.println("\n--- SISTEMA PORTUARIO DEEPSEA ---");
            System.out.println("1. Registrar llegada de Camiones");
            System.out.println("2. Visualizar estado del Barco (Consola)");
            System.out.println("3. Descargar Contenedor");
            System.out.println("4. Ver historial (Auditoria)");
            System.out.println("5. Reporte de Estabilidad");
            System.out.println("6. Deshacer último movimiento"); // Bajó al puesto 6
            System.out.println("7. Cargar Contenedor al Barco (Manual)");
            System.out.println("8. Bombeo de Lastre");
            System.out.println("9. Salir");
            System.out.print("Seleccione una operacion : ");

            opcion = teclado.nextInt();

            switch (opcion) {
                case 1: // Registrar Camión (Cola Circular)
                    System.out.print("Ingrese la placa del camión: ");
                    String placa = teclado.next();
                    filaCamiones.encolar(new Camion(placa)); // [cite: 982, 1000]
                    break;

                case 2: // Visualizar Barco (Matriz de Pilas)
                    visualizarConsola(miBarco); // [cite: 991, 1001]
                    break;

                case 3: // DESCARGAR
                    System.out.println("\n--- OPERACIÓN DE DESCARGA ---");
                    // 1. Verificar si hay camiones esperando en la Cola
                    Camion camionActual = filaCamiones.verFrente();
                    if (camionActual == null) {
                        System.out.println("ERROR: No hay camiones en la cola. No se puede descargar.");
                        break;
                    }

                    // 2. Pedir coordenadas de la Matriz
                    System.out.print("Fila del contenedor (0-4): ");
                    int f = teclado.nextInt();
                    System.out.print("Columna del contenedor (0-4): ");
                    int c = teclado.nextInt();

                    // 3. Intentar descargar validando la estabilidad (Regla del 30%)
                    int alturaReal = miBarco.getSecciones()[f][c].getTop();

                    Contenedor descargado = miBarco.descargarContenedor(f, c);

                    if (descargado != null) {
                        Camion camionAsignado = filaCamiones.desencolar();

                        // 2. Ahora sí, pasamos la 'alturaReal' a la fórmula
                        double tiempoGrua = CalculadoraPortuaria.calcularTiempoGrua(alturaReal, descargado.getPeso());
                        double consumo = CalculadoraPortuaria.calcularConsumoCombustible(descargado.getPeso(), 50.0);

                        // CREAMOS UNA DESCRIPCIÓN MUCHO MÁS RICA EN DATOS
                        String reporte = String.format(
                                "Descarga %s | Grúa: %.1fs | Consumo: %.1f gal | Camión: %s",
                                descargado.getId(), tiempoGrua, consumo, camionAsignado.getPlaca());

                        // Guardamos el movimiento con este reporte detallado
                        Movimiento mov = new Movimiento(descargado, camionAsignado, f, c, reporte);
                        auditoria.registrarAccion(mov);

                        // También lo imprimimos en consola
                        System.out.println(">>> OPERACIÓN REGISTRADA: " + reporte);
                    }

                    break;

                case 4: // Ver historial (Pila de Auditoría)
                    auditoria.mostrarHistorial(); // [cite: 993]
                    break;

                case 5: // Reporte de ingresos y estabilidad [cite: 994]
                    System.out.println("\n--- REPORTE TÉCNICO ---");
                    double pI = miBarco.calcularPesoLado("izquierdo");
                    double pD = miBarco.calcularPesoLado("derecho");
                    System.out.println(
                            "Distribución de peso: Lado Izquierdo [" + pI + "t] vs Lado Derecho [" + pD + "t]");
                    System.out.println("Estado de Estabilidad: " + (miBarco.esEstable() ? "SEGURO" : "RIESGO CRÍTICO"));
                    break;

                case 6: // DESHACER SOLO CONTENEDORES
                    Movimiento ultimo = auditoria.pop();
                    if (ultimo != null && ultimo.getContenedor() != null) {
                        miBarco.cargarContenedor(ultimo.getFila(), ultimo.getColumna(), ultimo.getContenedor());
                        if (ultimo.getCamion() != null)
                            filaCamiones.encolar(ultimo.getCamion());
                        System.out.println("REVERTIDO: Contenedor devuelto.");
                    } else {
                        System.out.println("No hay movimientos de contenedores para deshacer.");
                    }
                    break;

                case 7: // CARGAR CONTENEDOR MANUALMENTE
                    System.out.println("\n--- INGRESO DE NUEVA CARGA ---");
                    System.out.print("Ingrese ID del contenedor (Ej: C-100): ");
                    String id = teclado.next();
                    System.out.print("Ingrese el peso en toneladas: ");
                    double peso = teclado.nextDouble();
                    System.out.print("Fila de destino (0-4): ");
                    int fila = teclado.nextInt();
                    System.out.print("Columna de destino (0-4): ");
                    int col = teclado.nextInt();

                    // Creamos el contenedor y lo cargamos
                    Contenedor nuevo = new ContenedorEstandar(id, peso);
                    miBarco.cargarContenedor(fila, col, nuevo);
                    System.out.println(">>> ÉXITO: Contenedor " + id + " cargado en (" + fila + "," + col + ")");
                    break;

                case 8: // BOMBEO DE LASTRE MANUAL
                    System.out.println("\n--- SISTEMA DE BOMBEO DE LASTRE ---");
                    System.out.println("1. Bombear al tanque Izquierdo");
                    System.out.println("2. Bombear al tanque Derecho");
                    System.out.print("Seleccione tanque: ");
                    int tanque = teclado.nextInt();
                    System.out.print("Cantidad de agua a bombear (toneladas): ");
                    double agua = teclado.nextDouble();

                    boolean esIzq = (tanque == 1);
                    if (miBarco.bombearLastre(esIzq, agua)) {
                        System.out.println(">>> ÉXITO: Se han inyectado " + agua + "t de agua al tanque.");
                        // Opcional: registrar en historial
                        Movimiento movLastre = new Movimiento(null, null, 0, 0,
                                "[LASTRE] Bombeo de " + agua + "t al tanque " + (esIzq ? "Izquierdo" : "Derecho"));
                        auditoria.registrarAccion(movLastre);
                    } else {
                        System.out.println(">>> ERROR: Capacidad excedida. El tanque solo soporta 50t en total.");
                    }
                    break;

                case 9: // SALIR (Asegúrate de cambiar el case de salida a 9 y el while a != 9)
                    System.out.println("Cerrando simulador DeepSea...");
                    app.stop();
                    break;
            }
        }
        teclado.close();
    }

    // Método auxiliar para no llenar tanto el main
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