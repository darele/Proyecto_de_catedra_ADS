<%--
  Created by IntelliJ IDEA.
  User: DiegoMolina
  Date: 17/11/2026
  Time: 16:01
  To change this template use File | Settings | File Templates.
--%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Nuevo Empleado - RBO</title>
    <link rel="stylesheet" href="<c:url value='/css/rbo.css'/>">
</head>
<body class="pagina-app pagina-admin">

<jsp:include page="/WEB-INF/jsp/TopMenu.jsp" flush="true"/>

<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/admin/AdminMenu.jsp" flush="true"/>

    <main class="admin-contenido">
        <h2>Registrar Nuevo Empleado</h2>

        <c:if test="${not empty error}">
            <div class="error-msg"><c:out value="${error}"/></div>
        </c:if>

        <form action="<c:url value='/admin'/>" method="POST" class="formulario">
            <input type="hidden" name="action" value="guardar-empleado">

            <div class="campo">
                <label for="idEmpleado">Carnet / ID (máx 10 chars):</label>
                <input type="text" id="idEmpleado" name="idEmpleado" maxlength="10" required value="<c:out value='${param.idEmpleado}'/>">
            </div>

            <div class="campo">
                <label for="nombre">Nombre:</label>
                <input type="text" id="nombre" name="nombre" maxlength="30" required value="<c:out value='${param.nombre}'/>">
            </div>

            <div class="campo">
                <label for="apellido">Apellido:</label>
                <input type="text" id="apellido" name="apellido" maxlength="30" required value="<c:out value='${param.apellido}'/>">
            </div>

            <div class="campo">
                <label for="tarifa">Tarifa por hora (Opcional):</label>
                <input type="number" step="0.01" id="tarifa" name="tarifa" value="<c:out value='${param.tarifa}'/>">
            </div>

            <div class="campo">
                <label for="hora_entrada">Hora de Entrada:</label>
                <input type="time" id="hora_entrada" name="hora_entrada" required value="<c:out value='${param.hora_entrada}'/>">
            </div>

            <div class="campo">
                <label for="hora_salida">Hora de Salida:</label>
                <input type="time" id="hora_salida" name="hora_salida" required value="<c:out value='${param.hora_salida}'/>">
            </div>

            <div class="campo">
                <label for="tiempo_receso">Tiempo de Receso (en minutos):</label>
                <input type="number" id="tiempo_receso" name="tiempo_receso" required value="<c:out value='${empty param.tiempo_receso ? 60 : param.tiempo_receso}'/>">
            </div>

            <div class="campo">
                <label for="rol">Rol en el Sistema:</label>
                <select id="rol" name="rol" required>
                    <option value="Empleado">Empleado</option>
                    <option value="RH">RH (Recursos Humanos)</option>
                    <option value="Contabilidad">Contabilidad</option>
                    <option value="admin">Administrador</option>
                </select>
            </div>

            <div class="campo">
                <label for="password">Contraseña Temporal:</label>
                <input type="password" id="password" name="password" maxlength="64" required>
            </div>

            <div class="acciones">
                <button type="submit" class="btn">Guardar Empleado</button>
                <a href="<c:url value='/admin?action=empleados'/>" class="btn-secundario">Cancelar</a>
            </div>
        </form>
    </main>
</div>

</body>
</html>