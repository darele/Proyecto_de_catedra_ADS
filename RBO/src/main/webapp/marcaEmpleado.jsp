<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String carnet = (String) session.getAttribute("id_empleado");
    if (carnet == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String nombreCompleto = (String) session.getAttribute("nombre_empleado");
    if (nombreCompleto == null) nombreCompleto = carnet;

    // Estados manejados en sesión:
    // null / "SALIDA" -> No ha entrado o ya salió
    // "RECESO"        -> Ya marcó entrada (está trabajando) y su siguiente acción es ir a Receso
    // "RETORNO"       -> Está en receso y su siguiente acción es regresar del receso
    // "ENTRADA"       -> Regresó del receso (está trabajando) y el botón volvió a verde
    String estadoMarca = (String) session.getAttribute("estado_marca");
    String ultimaAccion = (String) request.getParameter("tipo_marca");

    // Definir el color y texto de la CAJITA DE ESTADO ACTUAL (sugerencia de tu compañero)
    String claseCajaEstado = "estado-rojo";
    String textoCajaEstado = "Estado actual: Sin iniciar jornada / Has registrado salida";

    // Definir el color y texto del BOTÓN PRINCIPAL (lo que pasará al presionarlo)
    boolean botonAmarillo = false;
    String textoBoton = "Entrada";

    if ("RECESO".equals(estadoMarca)) {
        // Ya presionó Entrada -> Está trabajando (Caja Verde) y el botón ofrece ir a Receso (Botón Amarillo)
        claseCajaEstado = "estado-verde";
        textoCajaEstado = "Estado actual: Has registrado entrada (En jornada)";
        botonAmarillo = true;
        textoBoton = "Receso";
    } else if ("RETORNO".equals(estadoMarca)) {
        // Presionó Receso -> Está en descanso (Caja Naranja) y el botón ofrece regresar (Botón Amarillo)
        claseCajaEstado = "estado-naranja";
        textoCajaEstado = "Estado actual: Estás en receso";
        botonAmarillo = true;
        textoBoton = "Finalizar Receso";
    } else if ("ENTRADA".equals(estadoMarca) && !"Salida".equalsIgnoreCase(ultimaAccion)) {
        // Regresó del receso -> Está trabajando (Caja Verde) y el botón volvió a verde
        claseCajaEstado = "estado-verde";
        textoCajaEstado = "Estado actual: Has registrado entrada (Retorno de receso)";
        botonAmarillo = false;
        textoBoton = "Entrada";
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Marca de Asistencia - RBO</title>
    <style>
        * {
            box-sizing: border-box;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        body {
            background-color: #eef2f7;
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 30px 15px;
            margin: 0;
        }
        .top-bar {
            background-color: #ffffff;
            width: 100%;
            max-width: 500px;
            padding: 12px 20px;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 18px;
            font-size: 14px;
            color: #333333;
        }
        .brand-user {
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .brand-user img {
            width: 38px;
            height: auto;
        }
        .logout-btn {
            color: #dc3545;
            text-decoration: none;
            font-weight: 600;
        }
        .logout-btn:hover {
            text-decoration: underline;
        }
        .card-marca {
            background-color: #ffffff;
            width: 100%;
            max-width: 500px;
            padding: 30px;
            border-radius: 12px;
            border-top: 6px solid #003DA5;
            box-shadow: 0 6px 18px rgba(0, 61, 165, 0.1);
            text-align: center;
        }
        .card-marca h2 {
            margin-top: 0;
            color: #003DA5;
            margin-bottom: 15px;
        }

        /* Cajita indicadora del ESTADO ACTUAL (Verde, Naranja o Rojo) */
        .caja-estado {
            padding: 12px 15px;
            border-radius: 8px;
            font-weight: bold;
            font-size: 15px;
            margin-bottom: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }
        .punto-estado {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            display: inline-block;
        }
        /* Verde cuando está trabajando (no está en receso) */
        .estado-verde {
            background-color: #d1e7dd;
            color: #0f5132;
            border: 2px solid #28a745;
        }
        .estado-verde .punto-estado {
            background-color: #28a745;
        }
        /* Naranja cuando está en receso */
        .estado-naranja {
            background-color: #ffe5d0;
            color: #7c3800;
            border: 2px solid #fd7e14;
        }
        .estado-naranja .punto-estado {
            background-color: #fd7e14;
        }
        /* Rojo cuando ha marcado salida o no ha iniciado */
        .estado-rojo {
            background-color: #f8d7da;
            color: #842029;
            border: 2px solid #dc3545;
        }
        .estado-rojo .punto-estado {
            background-color: #dc3545;
        }

        .alert-info {
            background-color: #e7f1ff;
            color: #084298;
            border: 1px solid #b6d4fe;
            padding: 8px 10px;
            border-radius: 6px;
            margin-bottom: 16px;
            font-size: 13px;
        }

        /* Cajita de comentario arriba (máx 100 caracteres) */
        .comentario-group {
            text-align: left;
            margin-bottom: 18px;
        }
        .comentario-group label {
            display: block;
            font-weight: 600;
            margin-bottom: 6px;
            color: #333333;
            font-size: 14px;
        }
        .comentario-group textarea {
            width: 100%;
            height: 75px;
            padding: 10px;
            border: 1px solid #ced4da;
            border-radius: 6px;
            resize: none;
            font-size: 14px;
        }
        .comentario-group textarea:focus {
            outline: none;
            border-color: #003DA5;
        }
        .contador-caracteres {
            text-align: right;
            font-size: 12px;
            color: #6c757d;
            margin-top: 4px;
        }

        /* Etiqueta de ayuda sobre el botón para evitar confusión */
        .accion-label {
            font-size: 12px;
            color: #6c757d;
            margin-bottom: 6px;
            display: block;
        }

        /* Botón principal debajo del comentario */
        .btn-entrada {
            width: 65%;
            height: 62px;
            background-color: #28a745;
            color: #ffffff;
            border: none;
            border-radius: 10px;
            font-size: 20px;
            font-weight: bold;
            cursor: pointer;
            box-shadow: 0 4px 10px rgba(40, 167, 69, 0.3);
            transition: background-color 0.2s, transform 0.1s;
            margin-bottom: 22px;
        }
        .btn-entrada:hover {
            background-color: #218838;
        }
        .btn-entrada:active {
            transform: scale(0.98);
        }

        /* Botón en Amarillo para Receso */
        .btn-receso {
            background-color: #ffc107 !important;
            color: #212529 !important;
            box-shadow: 0 4px 10px rgba(255, 193, 7, 0.35) !important;
        }
        .btn-receso:hover {
            background-color: #e0a800 !important;
        }

        /* Botón de Salida chiquito y rojo */
        .contenedor-salida {
            display: flex;
            justify-content: center;
            border-top: 1px solid #eef2f7;
            padding-top: 16px;
        }
        .btn-salida {
            background-color: #dc3545;
            color: #ffffff;
            border: none;
            padding: 6px 16px;
            font-size: 12px;
            font-weight: bold;
            border-radius: 5px;
            cursor: pointer;
            transition: background-color 0.2s;
        }
        .btn-salida:hover {
            background-color: #bb2d3b;
        }
    </style>
</head>
<body>

<div class="top-bar">
    <div class="brand-user">
        <img src="img/logo-rbo.jpg" alt="Logo RBO">
        <span><strong>Empleado:</strong> <%= nombreCompleto %> (<%= carnet %>)</span>
    </div>
    <a href="LogoutServlet" class="logout-btn">Cerrar Sesión</a>
</div>

<div class="card-marca">
    <h2>Registro de Marca</h2>

    <!-- CAJITA DE ESTADO ACTUAL (Verde = Entrada, Naranja = En Receso, Rojo = Salida) -->
    <div class="caja-estado <%= claseCajaEstado %>">
        <span class="punto-estado"></span>
        <span><%= textoCajaEstado %></span>
    </div>

    <% if (request.getAttribute("mensaje") != null) { %>
        <div class="alert-info">
            <%= request.getAttribute("mensaje") %>
        </div>
    <% } %>

    <form id="formMarca" action="MarcaServlet" method="POST">
        <input type="hidden" id="tipo_marca" name="tipo_marca" value="">

        <!-- 1. Cajita de comentario -->
        <div class="comentario-group">
            <label for="comentario">Comentario (opcional):</label>
            <textarea id="comentario" 
                      name="comentario" 
                      maxlength="100" 
                      placeholder="Ingrese un comentario (máximo 100 caracteres)..."
                      oninput="actualizarContador()"></textarea>
            <div id="contador" class="contador-caracteres">0 / 100</div>
        </div>

        <!-- 2. Botón de siguiente acción -->
        <span class="accion-label">Presione el botón para registrar su siguiente acción:</span>
        <button type="button" 
                id="btnAccionPrincipal" 
                class="btn-entrada <%= botonAmarillo ? "btn-receso" : "" %>" 
                onclick="presionarBotonPrincipal()">
            <%= textoBoton %>
        </button>

        <!-- 3. Botón de Salida chiquito y rojo -->
        <div class="contenedor-salida">
            <button type="button" class="btn-salida" onclick="presionarBotonSalida()">
                Salida
            </button>
        </div>
    </form>
</div>

<script>
    function actualizarContador() {
        const txt = document.getElementById("comentario");
        const contador = document.getElementById("contador");
        contador.textContent = txt.value.length + " / 100";
    }

    function presionarBotonPrincipal() {
        const btn = document.getElementById("btnAccionPrincipal");
        const inputTipo = document.getElementById("tipo_marca");
        const textoActual = btn.innerText.trim();

        if (textoActual === "Entrada") {
            inputTipo.value = "Entrada";
        } else if (textoActual === "Receso") {
            inputTipo.value = "Receso";
        } else if (textoActual === "Finalizar Receso") {
            inputTipo.value = "Retorno";
        }

        document.getElementById("formMarca").submit();
    }

    function presionarBotonSalida() {
        if (confirm("¿Está seguro de que desea registrar su SALIDA?")) {
            const inputTipo = document.getElementById("tipo_marca");
            inputTipo.value = "Salida";
            document.getElementById("formMarca").submit();
        }
    }
</script>

</body>
</html>