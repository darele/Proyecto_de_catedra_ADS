<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" session="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Página de error (registrada en web.xml para 403, 404, 500 y cualquier excepción).
    No muestra el detalle técnico al usuario: ese detalle queda en el log de Tomcat.
--%>
<c:set var="codigo" value="${pageContext.errorData.statusCode}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - RBO</title>
    <link rel="stylesheet" href="<c:url value='/css/rbo.css'/>">
</head>
<body class="pagina-centrada">

<div class="login-container">
    <div class="logo-container">
        <img src="<c:url value='/images/logo-rbo.jpg'/>" alt="Logo RBO">
    </div>
    <h2>
        <c:choose>
            <c:when test="${codigo == 403}">Acceso denegado</c:when>
            <c:when test="${codigo == 404}">Página no encontrada</c:when>
            <c:otherwise>Ocurrió un error</c:otherwise>
        </c:choose>
    </h2>
    <div class="subtitle">
        <c:choose>
            <c:when test="${codigo == 403}">No tiene permiso para ver esta sección.</c:when>
            <c:when test="${codigo == 404}">La dirección solicitada no existe.</c:when>
            <c:otherwise>Intente de nuevo. Si el problema continúa, revise el log de Tomcat.</c:otherwise>
        </c:choose>
        <c:if test="${codigo > 0}">(código <c:out value="${codigo}"/>)</c:if>
    </div>
    <a class="btn-ingresar" href="<c:url value='/rbo'/>">Volver al inicio</a>
</div>

</body>
</html>
