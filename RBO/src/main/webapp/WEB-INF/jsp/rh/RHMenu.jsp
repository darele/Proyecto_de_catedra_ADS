<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Fragmento: menú lateral de la Interfaz de RH (como AdminMenu.jsp).
    Cada enlace llama al controlador /rh con su action.
    requestScope.seccion indica qué opción resaltar.
    Para agregar una pantalla, agregue aquí su enlace (ver RHServlet).
--%>
<c:url var="urlInicio" value="/rh"/>
<c:url var="urlEmpleados" value="/rh">
    <c:param name="action" value="empleados"/>
</c:url>
<c:url var="urlTarifas" value="/rh">
    <c:param name="action" value="tarifas"/>
</c:url>
<c:url var="urlMarca" value="/rbo">
    <c:param name="action" value="marca"/>
</c:url>
<nav class="admin-menu">
    <h3>Recursos Humanos</h3>
    <a href="${urlInicio}" class="${seccion == 'inicio' ? 'activo' : ''}">Inicio</a>
    <a href="${urlEmpleados}" class="${seccion == 'empleados' ? 'activo' : ''}">Empleados</a>
    <a href="${urlTarifas}" class="${seccion == 'tarifas' ? 'activo' : ''}">Tarifas</a>
    <%-- La sesión de prueba (modo desarrollo) no tiene un empleado real que pueda marcar --%>
    <c:if test="${empty sessionScope.sesionDePrueba}">
        <a href="${urlMarca}">Mi marca de asistencia</a>
    </c:if>
</nav>
