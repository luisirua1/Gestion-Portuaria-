// --- FUNCIONES DE ACTUALIZACIÓN DE INTERFAZ ---

function actualizarInterfaz() {

    // 1. DIBUJAR BARCO (MATRIZ 5X5)
    fetch('/api/barco')
        .then(res => res.json())
        .then(secciones => {
            const grid = document.getElementById('barcoVisual');
            grid.innerHTML = ''; // Limpiar cuadrícula

            // Recorremos la matriz 5x5 (filas y columnas)
            for (let f = 0; f < 5; f++) {
                for (let c = 0; c < 5; c++) {
                    const pilaData = secciones[f][c];

                    // Creamos la Tarjeta de Pila (Celda)
                    const cardPila = document.createElement('div');
                    cardPila.className = 'pila-card';

                    // Calculamos cuántos contenedores hay en la pila
                    const numContenedores = pilaData.top + 1;

                    // CORRECCIÓN 1: Dibujamos en orden normal (0 -> top). 
                    // El CSS 'justify-content: end' se encargará de pegarlos al suelo de la celda.
                    for (let k = 0; k < numContenedores; k++) {
                        const bloque = document.createElement('div');
                        bloque.className = 'bloque-contenedor';

                        // Si es el contenedor del tope de la pila, le damos estilo amarillo
                        if (k === pilaData.top) {
                            bloque.classList.add('top-c');
                        }

                        // CORRECCIÓN 2: ESCRIBIR EL NOMBRE (ID) DENTRO DEL CONTENEDOR
                        // Jackson/Gson envía la pila como un objeto con top, capacidad y 'elementos' (el arreglo)
                        // Accedemos al contenedor real en la posición k y sacamos su 'id'
                        // CORRECCIÓN: ESCRIBIR EL NOMBRE (ID) Y EL PESO DENTRO DEL CONTENEDOR
                        if (pilaData.elementos && pilaData.elementos[k] && pilaData.elementos[k].id) {
                            const contenedor = pilaData.elementos[k];
                            // Usamos innerHTML para poder poner un salto de línea (<br>) y cambiar el tamaño de la letra del peso
                            bloque.innerHTML = `
                                <span>${contenedor.id}</span>
                                <span style="font-size: 12px; font-weight: normal; color: rgba(0, 0, 0, 0.7);">
                                    ${contenedor.peso.toFixed(1)}t
                                </span>
                            `;
                        } else {
                            bloque.innerText = "??";
                        }

                        cardPila.appendChild(bloque);
                    }

                    // Añadimos indicador de coordenadas (sutil arriba)
                    const coordLabel = document.createElement('span');
                    coordLabel.className = 'info-pila';
                    coordLabel.innerText = `(${f},${c})`;
                    cardPila.appendChild(coordLabel);

                    grid.appendChild(cardPila);
                }
            }
        });

    // 2. ACTUALIZAR ESTABILIDAD (PANEL DERECHO)
    // 2. ACTUALIZAR ESTABILIDAD (PANEL DERECHO)
    fetch('/api/estabilidad')
        .then(res => res.json())
        .then(data => {
            document.getElementById('pesoIzquierdo').innerText = data.izquierdo.toFixed(1) + "t";
            document.getElementById('pesoDerecho').innerText = data.derecho.toFixed(1) + "t";

            const mensajeDiv = document.getElementById('estadoMensaje');
            if (data.esEstable) {
                mensajeDiv.innerText = "ESTADO: SEGURO (✓)";
                mensajeDiv.style.backgroundColor = "rgba(46, 204, 113, 0.2)";
                mensajeDiv.style.color = "#2ecc71";
                mensajeDiv.style.border = "1px solid #2ecc71";
            } else {
                mensajeDiv.innerText = "⚠️ RIESGO DE VOLCAMIENTO ⚠️";
                mensajeDiv.style.backgroundColor = "rgba(231, 76, 60, 0.2)";
                mensajeDiv.style.color = "#e74c3c";
                mensajeDiv.style.border = "1px solid #e74c3c";
            }

            // --- NUEVO: ACTUALIZAR BARRAS DE LASTRE ---
            const lastreI = data.lastreIzq || 0;
            const lastreD = data.lastreDer || 0;

            // Actualizar textos
            document.getElementById('txtLastreIzq').innerText = lastreI.toFixed(1);
            document.getElementById('txtLastreDer').innerText = lastreD.toFixed(1);

            // Calcular porcentajes (Máximo 50 toneladas)
            const pctIzq = Math.min((lastreI / 50.0) * 100, 100);
            const pctDer = Math.min((lastreD / 50.0) * 100, 100);

            // Animar las barras
            document.getElementById('barLastreIzq').style.width = pctIzq + '%';
            document.getElementById('barLastreDer').style.width = pctDer + '%';
        });

    // 3. ACTUALIZAR HISTORIAL (PILA DE AUDITORÍA)
    fetch('/api/historial')
        .then(res => res.json())
        .then(data => {
            const logDiv = document.getElementById('logOperaciones');
            logDiv.innerHTML = '';

            // Recordar: data.log es el arreglo de movimientos, data.top es el índice de la cima
            const registros = data.log || [];
            // Mostramos los movimientos (del tope al fondo de la pila de auditoría)
            for (let i = data.top; i >= 0; i--) {
                if (registros[i]) {
                    const p = document.createElement('p');
                    p.innerText = `[${registros[i].fecha}] > ${registros[i].descripcion}`;
                    logDiv.appendChild(p);
                }
            }
            if (registros.length === 0 || data.top === -1) {
                logDiv.innerHTML = '<p>> No hay registros.</p>';
            }
        });

    // 4. ACTUALIZAR PATIO DE CAMIONES (COLA FIFO)
    fetch('/api/camiones')
        .then(res => res.json())
        .then(camiones => {
            const patio = document.getElementById('listaCamiones');
            patio.innerHTML = ''; // Limpiar

            if (!camiones || camiones.length === 0) {
                patio.innerHTML = '<p style="color:#8892b0; font-size:12px;">Sin transporte esperando.</p>';
                return;
            }

            camiones.forEach((camion, index) => {
                const cardC = document.createElement('div');
                cardC.className = 'camion-card';
                if (index === 0) { cardC.classList.add('first'); } // El primero en la cola es verde

                cardC.innerHTML = `
                    <div class="camion-icon">🚚</div>
                    <b>${camion.placa}</b><br>
                    <small>${index === 0 ? '¡Turno Actual!' : 'En fila'}</small>
                `;
                patio.appendChild(cardC);
            });
        });


}

function ajustarLastre(lado, valor) {
    fetch(`/api/lastre/ajustar?lado=${lado}&valor=${valor}`, { method: 'POST' })
        .then(() => actualizarInterfaz());
}

function resetLastre(lado) {
    if (confirm(`¿Desea vaciar el tanque ${lado}?`)) {
        fetch(`/api/lastre/reset?lado=${lado}`, { method: 'POST' })
            .then(() => actualizarInterfaz());
    }
}

// Actualizar cada segundo (Tiempo Real)
actualizarInterfaz(); // Primera vez
setInterval(actualizarInterfaz, 1000);