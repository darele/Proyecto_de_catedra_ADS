<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Vista: lista de empleados (ejemplo de pantalla que lee de la base de datos).
    Datos que recibe del controlador (AdminServlet):
      requestScope.empleados  (List<beans.Empleado>) empleados ordenados por apellido.
      requestScope.error      (String, opcional)     error al leer la base de datos.
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Empleados - Interfaz de Admin - RBO</title>
    <link rel="stylesheet" href="<c:url value='/css/rbo.css'/>">
</head>
<body class="pagina-app pagina-admin">

<jsp:include page="/WEB-INF/jsp/TopMenu.jsp" flush="true"/>

<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/admin/AdminMenu.jsp" flush="true"/>

    <main class="admin-contenido">
        <div class="encabezado-seccion">
            <h2>Empleados</h2>
            <div class="botones-encabezado">
                <a href="<c:url value='/admin?action=nuevo-empleado'/>" class="btn">+ Nuevo Empleado</a>
                <a href="<c:url value='/admin?action=eliminar-empleado'/>" class="btn-peligro">Eliminar Empleado</a>
            </div>
        </div>

        <c:choose>
            <c:when test="${not empty error}">
                <div class="error-msg"><c:out value="${error}"/></div>
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
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="emp" items="${empleados}">
                            <c:url var="urlEditar" value="/admin">
                                <c:param name="action" value="editar-empleado"/>
                                <c:param name="id" value="${emp.idEmpleado}"/>
                            </c:url>
                            <tr>
                                <td data-label="Carnet"><c:out value="${emp.idEmpleado}"/></td>
                                <td data-label="Nombre"><c:out value="${emp.nombreCompleto}"/></td>
                                <td data-label="Rol"><c:out value="${emp.rol}"/></td>
                                <td class="celda-accion">
                                    <a href="${urlEditar}" class="btn-secundario btn-chico">Editar</a>
                                </td>
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
