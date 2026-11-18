<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
    Vista: registro de marca de asistencia.
    Datos que recibe del controlador (RboServlet):
      requestScope.estado   (beans.EstadoMarca) estado actual de la jornada.
      requestScope.mensaje  (String, opcional)  confirmación o error de la última marca.
    El texto, el color de la caja y el botón salen del estado; aquí no hay lógica Java.
--%>
<c:set var="siguiente" value="${estado.siguienteAccion}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Marca de Asistencia - RBO</title>
    <link rel="stylesheet" href="<c:url value='/css/rbo.css'/>">
</head>
<body class="pagina-app">

<jsp:include page="/WEB-INF/jsp/TopMenu.jsp" flush="true"/>

<div class="card-marca">
    <h2>Registro de Marca</h2>

    <%-- Caja de estado actual: verde = en jornada, naranja = en receso, rojo = sin jornada --%>
    <div class="caja-estado ${estado.claseCaja}">
        <span class="punto-estado"></span>
        <span><c:out value="${estado.textoCaja}"/></span>
    </div>

    <c:if test="${not empty mensaje}">
        <div class="alert-info"><c:out value="${mensaje}"/></div>
    </c:if>

    <form action="<c:url value='/rbo'/>" method="post">
        <input type="hidden" name="action" value="registrarMarca">

        <div class="comentario-group">
            <label for="comentario">Comentario (opcional):</label>
            <textarea id="comentario"
                      name="comentario"
                      maxlength="100"
                      placeholder="Ingrese un comentario (máximo 100 caracteres)..."></textarea>
            <div id="contador" class="contador-caracteres">0 / 100</div>
        </div>

        <%-- Botón de la siguiente acción: cada botón envía su propio valor de tipo_marca --%>
        <span class="accion-label">Presione el botón para registrar su siguiente acción:</span>
        <button type="submit"
                name="tipo_marca"
                value="${siguiente.parametro}"
                class="btn-entrada${siguiente.botonAmarillo ? ' btn-receso' : ''}">
            <c:out value="${siguiente.textoBoton}"/>
        </button>

        <div class="contenedor-salida">
            <button type="submit"
                    name="tipo_marca"
                    value="Salida"
                    class="btn-salida"
                    onclick="return confirm('¿Está seguro de que desea registrar su SALIDA?');">
                Salida
            </button>
        </div>
    </form>
</div>

<script>
    (function () {
        var comentario = document.getElementById('comentario');
        var contador = document.getElementById('contador');
        comentario.addEventListener('input', function () {
            contador.textContent = comentario.value.length + ' / 100';
        });
    })();
</script>

</body>
</html>
