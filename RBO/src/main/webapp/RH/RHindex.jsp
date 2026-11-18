
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.util.List"%>
<%@page import="recursoshumanos.RHEstadisticasDAO"%>
<%@page import="recursoshumanos.HistorialTarifasDAO"%>
<%@page import="recursoshumanos.EmpleadosDAO"%>

<%
    RHEstadisticasDAO estadisticas = new RHEstadisticasDAO();

    int totalEmpleados = estadisticas.contarEmpleados();
    int totalTarifas = estadisticas.contarTarifas();

    boolean conexionCorrecta =
        totalEmpleados >= 0 && totalTarifas >= 0;

    HistorialTarifasDAO historialDAO = new HistorialTarifasDAO();

    List<String[]> historial = null;
    String errorHistorial = null;

    try {
        historial = historialDAO.obtenerHistorial();
    } catch (IllegalStateException e) {
        errorHistorial = "No se pudo cargar el historial de tarifas.";
    }

    List<String[]> empleados = null;
    String errorEmpleados = null;

    try {
        empleados = new EmpleadosDAO().obtenerEmpleados();
    } catch (IllegalStateException e) {
        errorEmpleados = "No se pudo cargar la lista de empleados.";
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>RBO | Recursos Humanos</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f3f6fb;
            color: #1e293b;
        }

        .sidebar {
            width: 240px;
            background: #14243a;
            color: white;
            position: fixed;
            top: 0;
            bottom: 0;
            padding: 28px 20px;
        }

        .sidebar h2 {
            margin: 0 0 8px;
        }

        .sidebar p {
            color: #a9bdd4;
            font-size: 13px;
        }

        .menu {
            margin-top: 45px;
        }

        .menu a {
            display: block;
            padding: 14px;
            margin-bottom: 8px;
            border-radius: 8px;
            color: white;
            text-decoration: none;
        }

        .menu a:hover {
            background: #243b5a;
        }

        .menu .active {
            background: #2563eb;
        }

        main {
            margin-left: 240px;
            padding: 35px;
        }

        header h1 {
            margin-bottom: 5px;
        }

        header p {
            color: #64748b;
        }

        .cards {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
            margin-top: 30px;
        }

        .card, .panel {
            background: white;
            padding: 25px;
            border-radius: 14px;
            box-shadow: 0 4px 15px #0000000a;
        }

        .card h2 {
            color: #2563eb;
        }

        .panel {
            margin-top: 30px;
        }

        .fields {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            font-size: 14px;
        }

        input {
            width: 100%;
            padding: 12px;
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            font-size: 14px;
        }

        input:focus {
            outline: none;
            border-color: #2563eb;
        }

        button {
            background: #2563eb;
            color: white;
            border: none;
            border-radius: 8px;
            padding: 14px 24px;
            margin-top: 20px;
            cursor: pointer;
            font-weight: bold;
        }

        button:hover {
            background: #1d4ed8;
        }

        .mensaje {
            background: #e0f2fe;
            color: #075985;
            padding: 15px;
            border-radius: 8px;
            margin: 20px 0;
        }

        .nota {
            color: #64748b;
            font-size: 13px;
            margin-top: 15px;
        }

        .estado-ok {
            color: #16a34a !important;
        }

        .estado-error {
            color: #dc2626 !important;
        }

        .tabla-contenedor {
            overflow-x: auto;
            margin-top: 20px;
        }

        .tabla-historial {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }

        .tabla-historial th {
            background: #f1f5f9;
            text-align: left;
            padding: 14px;
            color: #334155;
        }

        .tabla-historial td {
            padding: 14px;
            border-bottom: 1px solid #e2e8f0;
        }

        .tabla-historial tbody tr:hover {
            background: #f8fafc;
        }

        .tabla-historial .vacio {
            text-align: center;
            color: #64748b;
            padding: 25px;
        }

        @media(max-width: 800px) {
            .sidebar {
                position: static;
                width: 100%;
            }

            main {
                margin-left: 0;
                padding: 20px;
            }

            .cards, .fields {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>

    <aside class="sidebar">
        <h2>RBO</h2>
        <p>Sistema de Recursos Humanos</p>

        <div class="menu">
            <a href="#panel" class="active">
                Panel principal
            </a>

            <a href="#empleados">
                Empleados
            </a>

            <a href="#registro">
                Registro de tarifas
            </a>

            <a href="#historial">
                Historial
            </a>
        </div>
    </aside>

    <main>

        <header id="panel">
            <h1>Recursos Humanos</h1>
            <p>Administración de empleados y tarifas</p>
        </header>

        <!-- TARJETAS CON DATOS REALES -->

        <section class="cards">

            <div class="card">
                <p>Empleados registrados</p>
                <h2>
                    <%= totalEmpleados >= 0
                        ? totalEmpleados : "No disponible" %>
                </h2>
            </div>

            <div class="card">
                <p>Tarifas registradas</p>
                <h2>
                    <%= totalTarifas >= 0
                        ? totalTarifas : "No disponible" %>
                </h2>
            </div>

            <div class="card">
                <p>Estado del sistema</p>

                <h2 class="<%= conexionCorrecta
                        ? "estado-ok" : "estado-error" %>">

                    <%= conexionCorrecta
                        ? "Conectado" : "Error de conexión" %>

                </h2>
            </div>

        </section>

        <!-- CONSULTA DE EMPLEADOS -->

        <section class="panel" id="empleados">

            <h2>Empleados registrados</h2>

            <p>
                Consulta de hasta 100 empleados
                ordenados alfabéticamente.
            </p>

            <div class="tabla-contenedor">

                <table class="tabla-historial">

                    <thead>
                        <tr>
                            <th>Carnet</th>
                            <th>Nombre</th>
                            <th>Apellido</th>
                            <th>Rol</th>
                            <th>Entrada</th>
                            <th>Salida</th>
                        </tr>
                    </thead>

                    <tbody>

                        <% if (errorEmpleados != null) { %>

                            <tr>
                                <td colspan="6" class="vacio">
                                    <%= errorEmpleados %>
                                </td>
                            </tr>

                        <% } else if (empleados.isEmpty()) { %>

                            <tr>
                                <td colspan="6" class="vacio">
                                    No hay empleados registrados.
                                </td>
                            </tr>

                        <% } else { %>

                            <% for (String[] empleado : empleados) { %>

                                <tr>
                                    <% for (String dato : empleado) { %>

                                        <td>
                                            <%= dato == null
                                                ? "—"
                                                : dato.replace("&", "&amp;")
                                                      .replace("<", "&lt;")
                                                      .replace(">", "&gt;")
                                                      .replace("\"", "&quot;")
                                                      .replace("'", "&#39;") %>
                                        </td>

                                    <% } %>
                                </tr>

                            <% } %>

                        <% } %>

                    </tbody>

                </table>

            </div>

        </section>

        <!-- FORMULARIO DE TARIFAS -->

        <section class="panel" id="registro">

            <h2>Registrar nueva tarifa</h2>

            <p>
                Completá los siguientes campos para registrar
                una nueva tarifa de empleado.
            </p>

            <%
                Object mensaje = request.getAttribute("mensaje");

                if (mensaje != null) {
            %>

                <div class="mensaje">
                    <%= mensaje %>
                </div>

            <%
                }
            %>

            <form
                action="${pageContext.request.contextPath}/RH/registrarTarifa"
                method="POST">

                <div class="fields">

                    <div>
                        <label for="idEmpleado">
                            Carnet del empleado
                        </label>

                        <input
                            type="text"
                            id="idEmpleado"
                            name="idEmpleado"
                            maxlength="10"
                            placeholder="Ej. TEST001"
                            required>
                    </div>

                    <div>
                        <label for="tarifa">
                            Tarifa
                        </label>

                        <input
                            type="number"
                            id="tarifa"
                            name="tarifa"
                            step="0.01"
                            min="0"
                            placeholder="0.00"
                            required>
                    </div>

                    <div>
                        <label for="fecha">
                            Fecha de tarifa
                        </label>

                        <input
                            type="date"
                            id="fecha"
                            name="fecha"
                            value="<%= LocalDate.now() %>"
                            required>
                    </div>

                    <div>
                        <label for="horaEntrada">
                            Hora de entrada
                        </label>

                        <input
                            type="time"
                            id="horaEntrada"
                            name="horaEntrada"
                            value="08:00"
                            required>
                    </div>

                    <div>
                        <label for="horaSalida">
                            Hora de salida
                        </label>

                        <input
                            type="time"
                            id="horaSalida"
                            name="horaSalida"
                            value="17:00"
                            required>
                    </div>

                </div>

                <button type="submit">
                    Guardar tarifa
                </button>

                <p class="nota">
                    Los datos se registrarán en la base
                    de datos del sistema RBO.
                </p>

            </form>

        </section>

        <!-- HISTORIAL DE TARIFAS -->

        <section class="panel" id="historial">

            <h2>Historial de tarifas</h2>
            <p>Últimos 100 registros de tarifas.</p>

            <div class="tabla-contenedor">

                <table class="tabla-historial">

                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Empleado</th>
                            <th>Fecha</th>
                            <th>Entrada</th>
                            <th>Salida</th>
                            <th>Tarifa</th>
                        </tr>
                    </thead>

                    <tbody>

                        <% if (errorHistorial != null) { %>

                            <tr>
                                <td colspan="6" class="vacio">
                                    <%= errorHistorial %>
                                </td>
                            </tr>

                        <% } else if (historial.isEmpty()) { %>

                            <tr>
                                <td colspan="6" class="vacio">
                                    No hay tarifas registradas.
                                </td>
                            </tr>

                        <% } else { %>

                            <% for (String[] registro : historial) { %>

                                <tr>
                                    <td><%= registro[0] %></td>
                                    <td><%= registro[1] %></td>
                                    <td><%= registro[2] %></td>
                                    <td><%= registro[3] %></td>
                                    <td><%= registro[4] %></td>
                                    <td>$<%= registro[5] %></td>
                                </tr>

                            <% } %>

                        <% } %>

                    </tbody>

                </table>

            </div>

        </section>

    </main>

</body>
</html>
