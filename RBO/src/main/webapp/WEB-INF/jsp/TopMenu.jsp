<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Fragmento: barra superior con el usuario y "Cerrar Sesión".
    Se incluye en todas las páginas con sesión (como TopMenu.jsp en el libro):
        <jsp:include page="/WEB-INF/jsp/TopMenu.jsp" flush="true"/>
    No carga estilos: solo usa clases que define css/rbo.css, que enlaza la página que lo incluye.
--%>
<c:set var="usuario" value="${sessionScope.empleado}"/>
<c:url var="urlLogout" value="/rbo">
    <c:param name="action" value="logout"/>
</c:url>
<div class="top-bar">
    <div class="brand-user">
        <img src="<c:url value='/images/logo-rbo.jpg'/>" alt="Logo RBO">
        <span>
            <strong><c:out value="${usuario.admin ? 'Administrador' : 'Empleado'}"/>:</strong>
            <c:out value="${usuario.nombreCompleto}"/> (<c:out value="${usuario.idEmpleado}"/>)
        </span>
    </div>
    <a href="${urlLogout}" class="logout-btn">Cerrar Sesión</a>
</div>
