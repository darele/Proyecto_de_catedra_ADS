<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Vista: lista de empleados con la opción de eliminar.
    Datos que recibe del controlador (AdminServlet):
      requestScope.empleados      (List<beans.Empleado>) empleados ordenados por apellido.
      requestScope.error          (String, opcional)     mensaje para el usuario (BaseDatosException.getMessage()).
      requestScope.errorDetalle   (String, opcional)     mensaje original del driver, para depurar.
      requestScope.tipoError      (String, opcional)     BaseDatosException.Tipo: REGISTRO_EN_USO, NO_ENCONTRADO, ...
      param.eliminado             (String, opcional)     carnet recién eliminado (llega tras el redirect).
      sessionScope.empleado       (beans.Empleado)       administrador en sesión (no puede eliminarse a sí mismo).
      sessionScope.sesionDePrueba (Boolean, opcional)    presente si es la sesión del modo desarrollo.
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Eliminar Empleado - Interfaz de Admin - RBO</title>
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

        <div class="encabezado-seccion">
            <h2>Eliminar Empleado</h2>
            <a href="<c:url value='/admin?action=empleados'/>" class="btn-secundario">Volver a la lista</a>
        </div>

        <c:if test="${not empty param.eliminado and empty error}">
            <div class="exito-msg">
                Se eliminó el empleado <strong><c:out value="${param.eliminado}"/></strong>.
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="error-msg error-detallado">
                <strong><c:out value="${error}"/></strong>
                <c:if test="${tipoError == 'REGISTRO_EN_USO'}">
                    <p>El empleado tiene marcas de asistencia o tarifas guardadas. Para conservar el
                        historial, MariaDB no permite borrarlo mientras existan esos registros.</p>
                </c:if>
                <c:if test="${not empty errorDetalle}">
                    <details>
                        <summary>Detalle técnico</summary>
                        <code><c:out value="${errorDetalle}"/></code>
                    </details>
                </c:if>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty empleados}">
                <p>No hay empleados para mostrar.</p>
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
                            <tr>
                                <td data-label="Carnet"><c:out value="${emp.idEmpleado}"/></td>
                                <td data-label="Nombre"><c:out value="${emp.nombreCompleto}"/></td>
                                <td data-label="Rol"><c:out value="${emp.rol}"/></td>
                                <td class="celda-accion">
                                    <c:choose>
                                        <c:when test="${emp.idEmpleado == sessionScope.empleado.idEmpleado}">
                                            <span class="texto-tenue">(usted)</span>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="<c:url value='/admin'/>" method="POST"
                                                  onsubmit="return confirm('¿Eliminar a ' + this.dataset.nombre + '? Esta acción no se puede deshacer.');"
                                                  data-nombre="<c:out value='${emp.nombreCompleto}'/>">
                                                <input type="hidden" name="action" value="borrar-empleado">
                                                <input type="hidden" name="idEmpleado" value="<c:out value='${emp.idEmpleado}'/>">
                                                <button type="submit" class="btn-peligro">Eliminar</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
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
