<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Fragmento: barra superior con el usuario, el selector de interfaces y "Cerrar Sesión".
    Se incluye en todas las páginas con sesión (como TopMenu.jsp en el libro):
        <jsp:include page="/WEB-INF/jsp/TopMenu.jsp" flush="true"/>
    No carga estilos: solo usa clases que define css/rbo.css, que enlaza la página que lo incluye.

    Selector de interfaces: solo lo ve el admin, que puede entrar a Admin y a RH.
    requestScope.interfaz ("admin" | "rh" | "marca") lo pone el controlador e indica cuál resaltar.
    Para agregar una interfaz (por ejemplo Contabilidad), agregue aquí su enlace.
--%>
<c:set var="usuario" value="${sessionScope.empleado}"/>
<c:url var="urlLogout" value="/rbo">
    <c:param name="action" value="logout"/>
</c:url>
<div class="top-bar">
    <div class="brand-user">
        <img src="<c:url value='/images/logo-rbo.jpg'/>" alt="Logo RBO">
        <span>
            <strong><c:out value="${usuario.admin ? 'Administrador' : (usuario.RH ? 'Recursos Humanos' : 'Empleado')}"/>:</strong>
            <c:out value="${usuario.nombreCompleto}"/> (<c:out value="${usuario.idEmpleado}"/>)
        </span>
    </div>

    <c:if test="${usuario.admin}">
        <nav class="selector-interfaz" aria-label="Cambiar de interfaz">
            <a href="<c:url value='/admin'/>" class="${interfaz == 'admin' ? 'activo' : ''}"
               ${interfaz == 'admin' ? 'aria-current="page"' : ''}>Admin</a>
            <a href="<c:url value='/rh'/>" class="${interfaz == 'rh' ? 'activo' : ''}"
               ${interfaz == 'rh' ? 'aria-current="page"' : ''}>Recursos Humanos</a>
        </nav>
    </c:if>

    <a href="${urlLogout}" class="logout-btn">Cerrar Sesión</a>
</div>
