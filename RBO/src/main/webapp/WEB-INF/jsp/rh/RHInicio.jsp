<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Vista: portada de la Interfaz de RH.
    Datos que recibe del controlador (RHServlet):
      requestScope.resumen          (beans.ResumenRH)  total de empleados y de tarifas.
      sessionScope.empleado         (beans.Empleado)   usuario en sesión.
      sessionScope.sesionDePrueba   (Boolean, opcional) presente si es la sesión del modo desarrollo.
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Interfaz de RH - RBO</title>
    <link rel="stylesheet" href="<c:url value='/css/rbo.css'/>">
</head>
<body class="pagina-app pagina-admin">

<jsp:include page="/WEB-INF/jsp/TopMenu.jsp" flush="true"/>

<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/rh/RHMenu.jsp" flush="true"/>

    <main class="admin-contenido">
        <c:if test="${not empty sessionScope.sesionDePrueba}">
            <div class="aviso-desarrollo">
                <strong>Modo desarrollo:</strong> está viendo esta sección con un usuario de prueba,
                sin haber iniciado sesión. Desactívelo antes de entregar.
            </div>
        </c:if>

        <h2>¡Hola, <c:out value="${sessionScope.empleado.nombre}"/>!</h2>
        <p>Bienvenido a la <strong>Interfaz de Recursos Humanos</strong>: administración de empleados y tarifas.</p>

        <c:choose>
            <c:when test="${resumen.baseDisponible}">
                <div class="tarjetas">
                    <div class="tarjeta">
                        <span class="tarjeta-numero"><c:out value="${resumen.totalEmpleados}"/></span>
                        <span class="tarjeta-etiqueta">Empleados registrados</span>
                    </div>
                    <div class="tarjeta">
                        <span class="tarjeta-numero"><c:out value="${resumen.totalTarifas}"/></span>
                        <span class="tarjeta-etiqueta">Tarifas registradas</span>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="error-msg">
                    No hay conexión con la base de datos. Encienda MySQL en XAMPP y recargue la página.
                </div>
            </c:otherwise>
        </c:choose>
    </main>
</div>

</body>
</html>
