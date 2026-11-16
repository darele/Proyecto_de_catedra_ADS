<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Vista: portada de la Interfaz de Admin ("hola mundo").
    Datos que recibe del controlador (AdminServlet):
      requestScope.resumen          (beans.ResumenAdmin) números de la portada.
      sessionScope.empleado         (beans.Empleado)     administrador en sesión.
      sessionScope.sesionDePrueba   (Boolean, opcional)  presente si es la sesión del modo desarrollo.
    Copie esta página como plantilla para cada pantalla nueva de su sección.
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Interfaz de Admin - RBO</title>
    <link rel="stylesheet" href="<c:url value='/css/rbo.css'/>">
</head>
<body class="pagina-app pagina-admin">

<jsp:include page="/WEB-INF/jsp/TopMenu.jsp" flush="true"/>

<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/admin/AdminMenu.jsp" flush="true"/>

    <main class="admin-contenido">
        <c:if test="${not empty sessionScope.sesionDePrueba}">
            <div class="aviso-desarrollo">
                <strong>Modo desarrollo:</strong> está viendo esta sección con un administrador de prueba,
                sin haber iniciado sesión. Desactívelo antes de entregar.
            </div>
        </c:if>

        <h2>¡Hola, <c:out value="${sessionScope.empleado.nombre}"/>!</h2>
        <p>Bienvenido a la <strong>Interfaz de Admin</strong> del Sistema de Control de Asistencia.</p>

        <c:choose>
            <c:when test="${resumen.baseDisponible}">
                <div class="tarjetas">
                    <div class="tarjeta">
                        <span class="tarjeta-numero"><c:out value="${resumen.totalEmpleados}"/></span>
                        <span class="tarjeta-etiqueta">Empleados registrados</span>
                    </div>
                    <div class="tarjeta">
                        <span class="tarjeta-numero"><c:out value="${resumen.marcasHoy}"/></span>
                        <span class="tarjeta-etiqueta">Marcas registradas hoy</span>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="error-msg">
                    No hay conexión con la base de datos. Encienda MySQL en XAMPP y recargue la página.
                </div>
            </c:otherwise>
        </c:choose>

        <h3>¿Cómo agrego una pantalla a esta sección?</h3>
        <ol class="pasos">
            <li>En <code>AdminServlet.resolverVista</code>, agregue un <code>case</code> con el nuevo <code>action</code>.</li>
            <li>En <code>AdminServlet.prepararDatos</code>, pida los datos al <code>DataManager</code> y déjelos en el request.</li>
            <li>Cree la vista en <code>WEB-INF/jsp/admin/</code> copiando esta página (sin scriptlets, solo JSTL y EL).</li>
            <li>Agregue el enlace en <code>AdminMenu.jsp</code>.</li>
        </ol>
    </main>
</div>

</body>
</html>
