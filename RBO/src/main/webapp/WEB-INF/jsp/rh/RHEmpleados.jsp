<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Vista: empleados con su horario (consulta de solo lectura para RH).
    Datos que recibe del controlador (RHServlet):
      requestScope.empleados       (List<beans.Empleado>) ordenados por apellido, con horario.
      requestScope.errorEmpleados  (String, opcional)     error al leer la base de datos.
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Empleados - Interfaz de RH - RBO</title>
    <link rel="stylesheet" href="<c:url value='/css/rbo.css'/>">
</head>
<body class="pagina-app pagina-admin">

<jsp:include page="/WEB-INF/jsp/TopMenu.jsp" flush="true"/>

<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/rh/RHMenu.jsp" flush="true"/>

    <main class="admin-contenido">
        <div class="encabezado-seccion">
            <h2>Empleados</h2>
            <div class="botones-encabezado">
                <a href="<c:url value='/rh?action=tarifas'/>" class="btn">+ Registrar tarifa</a>
            </div>
        </div>

        <c:choose>
            <c:when test="${not empty errorEmpleados}">
                <div class="error-msg"><c:out value="${errorEmpleados}"/></div>
            </c:when>
            <c:when test="${empty empleados}">
                <p>Todavía no hay empleados registrados.</p>
            </c:when>
            <c:otherwise>
                <table class="tabla tabla-apilable">
                    <thead>
                        <tr>
                            <th>Carnet</th>
                            <th>Nombre</th>
                            <th>Rol</th>
                            <th>Entrada</th>
                            <th>Salida</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="emp" items="${empleados}">
                            <tr>
                                <td data-label="Carnet"><c:out value="${emp.idEmpleado}"/></td>
                                <td data-label="Nombre"><c:out value="${emp.nombreCompleto}"/></td>
                                <td data-label="Rol"><c:out value="${emp.rol}"/></td>
                                <td data-label="Entrada"><c:out value="${emp.horaEntrada}" default="—"/></td>
                                <td data-label="Salida"><c:out value="${emp.horaSalida}" default="—"/></td>
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
