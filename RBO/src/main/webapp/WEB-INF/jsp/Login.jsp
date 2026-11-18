<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Vista: inicio de sesión.
    Datos que recibe del controlador (RboServlet): requestScope.error (opcional).
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión - Reliable Back Office</title>
    <link rel="stylesheet" href="<c:url value='/css/rbo.css'/>">
</head>
<body class="pagina-centrada">

<div class="login-container">
    <div class="logo-container">
        <img src="<c:url value='/images/logo-rbo.jpg'/>" alt="Logo RBO">
    </div>
    <h2>Iniciar Sesión</h2>
    <div class="subtitle">Sistema de Control de Asistencia</div>

    <c:if test="${not empty error}">
        <div class="error-msg"><c:out value="${error}"/></div>
    </c:if>

    <form action="<c:url value='/rbo'/>" method="post">
        <input type="hidden" name="action" value="login">

        <div class="form-group">
            <label for="carnet">Número de Carnet</label>
            <input type="text"
                   id="carnet"
                   name="carnet"
                   maxlength="10"
                   value="<c:out value='${param.carnet}'/>"
                   placeholder="Introduzca su número de carnet"
                   required
                   autofocus>
            <span class="helper-text">Indicación: Introduzca su carnet de empleado (máx. 10 caracteres).</span>
        </div>

        <div class="form-group">
            <label for="password">Contraseña</label>
            <input type="password"
                   id="password"
                   name="password"
                   placeholder="Introduzca su contraseña"
                   required>
        </div>

        <button type="submit" class="btn-ingresar">Ingresar</button>
    </form>
</div>

</body>
</html>
