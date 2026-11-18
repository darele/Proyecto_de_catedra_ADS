<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%--
    Vista: registrar una tarifa y ver el historial.
    Datos que recibe del controlador (RHServlet):
      requestScope.empleados       (List<beans.Empleado>) opciones del select de empleado.
      requestScope.errorEmpleados  (String, opcional)     error al leer los empleados.
      requestScope.tarifas         (List<beans.Tarifa>)   historial, de la más reciente a la más antigua.
      requestScope.errorHistorial  (String, opcional)     error al leer el historial.
      requestScope.hoy             (String yyyy-MM-dd)    fecha por defecto del formulario.
      requestScope.error           (String, opcional)     error al registrar (validación o base de datos).
      requestScope.errorDetalle    (String, opcional)     mensaje original del driver, para depurar.
      requestScope.mensajeOk       (String, opcional)     confirmación tras registrar (llega con ?ok=1).
    Si hubo error, el formulario conserva lo que se escribió (param.*).
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tarifas - Interfaz de RH - RBO</title>
    <link rel="stylesheet" href="<c:url value='/css/rbo.css'/>">
</head>
<body class="pagina-app pagina-admin">

<jsp:include page="/WEB-INF/jsp/TopMenu.jsp" flush="true"/>

<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/rh/RHMenu.jsp" flush="true"/>

    <main class="admin-contenido">
        <h2>Registrar nueva tarifa</h2>

        <c:if test="${not empty mensajeOk}">
            <div class="exito-msg"><c:out value="${mensajeOk}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="error-msg error-detallado">
                <strong><c:out value="${error}"/></strong>
                <c:if test="${not empty errorDetalle}">
                    <details>
                        <summary>Detalle técnico</summary>
                        <code><c:out value="${errorDetalle}"/></code>
                    </details>
                </c:if>
            </div>
        </c:if>
        <c:if test="${not empty errorEmpleados}">
            <div class="error-msg"><c:out value="${errorEmpleados}"/></div>
        </c:if>

        <%-- Valores por defecto del formulario; si hubo error se conserva lo que se escribió. --%>
        <c:set var="valorFecha" value="${empty param.fecha ? hoy : param.fecha}"/>
        <c:set var="valorEntrada" value="${empty param.horaEntrada ? '08:00' : param.horaEntrada}"/>
        <c:set var="valorSalida" value="${empty param.horaSalida ? '17:00' : param.horaSalida}"/>

        <form action="<c:url value='/rh'/>" method="POST" class="formulario">
            <input type="hidden" name="action" value="registrar-tarifa">

            <div class="campo">
                <label for="idEmpleado">Empleado:</label>
                <select id="idEmpleado" name="idEmpleado" required>
                    <option value="">-- Seleccione --</option>
                    <c:forEach var="emp" items="${empleados}">
                        <option value="<c:out value='${emp.idEmpleado}'/>"
                            ${param.idEmpleado == emp.idEmpleado ? 'selected' : ''}>
                            <c:out value="${emp.idEmpleado}"/> - <c:out value="${emp.nombreCompleto}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="campo">
                <label for="tarifa">Tarifa por hora ($):</label>
                <input type="number" id="tarifa" name="tarifa" step="0.01" min="0.01" placeholder="0.00" required
                       value="<c:out value='${param.tarifa}'/>">
            </div>

            <div class="campo">
                <label for="fecha">Fecha de tarifa:</label>
                <input type="date" id="fecha" name="fecha" required
                       value="<c:out value='${valorFecha}'/>">
            </div>

            <div class="campo">
                <label for="horaEntrada">Hora de entrada:</label>
                <input type="time" id="horaEntrada" name="horaEntrada" required
                       value="<c:out value='${valorEntrada}'/>">
            </div>

            <div class="campo">
                <label for="horaSalida">Hora de salida:</label>
                <input type="time" id="horaSalida" name="horaSalida" required
                       value="<c:out value='${valorSalida}'/>">
            </div>

            <div class="acciones">
                <button type="submit" class="btn">Guardar tarifa</button>
            </div>
        </form>

        <h2>Historial de tarifas</h2>
        <p class="texto-tenue">Últimos 100 registros.</p>

        <c:choose>
            <c:when test="${not empty errorHistorial}">
                <div class="error-msg"><c:out value="${errorHistorial}"/></div>
            </c:when>
            <c:when test="${empty tarifas}">
                <p>Todavía no hay tarifas registradas.</p>
            </c:when>
            <c:otherwise>
                <table class="tabla tabla-apilable">
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
                        <c:forEach var="t" items="${tarifas}">
                            <tr>
                                <td data-label="ID"><c:out value="${t.idTarifa}"/></td>
                                <td data-label="Empleado">
                                    <c:out value="${t.idEmpleado}"/>
                                    <c:if test="${not empty t.nombreEmpleado}">
                                        <span class="texto-tenue">(<c:out value="${t.nombreEmpleado}"/>)</span>
                                    </c:if>
                                </td>
                                <td data-label="Fecha"><c:out value="${t.fechaTarifa}"/></td>
                                <td data-label="Entrada"><c:out value="${t.horaEntrada}"/></td>
                                <td data-label="Salida"><c:out value="${t.horaSalida}"/></td>
                                <td data-label="Tarifa">$<fmt:formatNumber value="${t.tarifa}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </main>
</div>

</body>
</html>
