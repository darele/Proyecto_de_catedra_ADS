<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%--
    Vista: editar un empleado de forma "bloqueante", campo por campo.

    Todos los campos se muestran bloqueados (solo lectura). El botón "Editar" de una fila abre
    SOLO ese campo (?campo=nombre); mientras está abierto, los demás botones desaparecen hasta
    guardar o cancelar. "Editar todos" (?campo=todos) abre todos con un único botón Guardar.
    El bloqueo lo decide el servidor: solo se dibujan como <input> los campos abiertos, así que
    el formulario solo envía esos.

    Datos que recibe del controlador (AdminServlet.prepararEditor):
      requestScope.emp           (beans.Empleado, opcional) empleado a editar; falta si no se encontró.
      requestScope.campo         (String, opcional)         campo abierto: nombre | apellido | tarifa |
                                                            hora_entrada | hora_salida | tiempo_receso | rol | todos.
      requestScope.roles         (List<String>)             valores del ENUM rol.
      requestScope.mensajeOk     (String, opcional)         confirmación tras guardar.
      requestScope.error         (String, opcional)         mensaje de error (validación o BaseDatosException).
      requestScope.errorDetalle  (String, opcional)         mensaje original del driver.
      param.<campo>              (String, opcional)         valor enviado, para no perderlo si hubo error.
      sessionScope.sesionDePrueba (Boolean, opcional)       presente si es la sesión del modo desarrollo.
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Empleado - Interfaz de Admin - RBO</title>
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
            <h2>Editar Empleado</h2>
            <a href="<c:url value='/admin?action=empleados'/>" class="btn-secundario">Volver a la lista</a>
        </div>

        <c:if test="${not empty mensajeOk}">
            <div class="exito-msg"><c:out value="${mensajeOk}"/></div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="error-msg error-detallado">
                <strong><c:out value="${error}"/></strong>
                <c:if test="${not empty errorDetalle}">
                    <details>
                        <summary>Detalle técnico</summary>
                        <code><c:out value="${errorDetalle}"/></code>
                    </details>
                </c:if>
            </div>
        </c:if>

        <c:if test="${not empty emp}">
            <%-- URL base del editor; cada botón "Editar" le agrega &campo=... --%>
            <c:url var="urlEditor" value="/admin">
                <c:param name="action" value="editar-empleado"/>
                <c:param name="id" value="${emp.idEmpleado}"/>
            </c:url>
            <c:set var="todos" value="${campo == 'todos'}"/>
            <c:set var="libre" value="${empty campo}"/>

            <c:if test="${libre}">
                <div id="f-todos" class="barra-edicion">
                    <span class="texto-tenue">Los campos están bloqueados. Edite uno a la vez o todos juntos.</span>
                    <a href="${urlEditor}&campo=todos#f-todos" class="btn">Editar todos</a>
                </div>
            </c:if>
            <c:if test="${todos}">
                <div id="f-todos" class="aviso-desarrollo">Editando todos los campos. Guarde o cancele para salir.</div>
            </c:if>

            <%-- El #f-campo en el action hace que, si hay error, la página vuelva a la fila abierta. --%>
            <form action="<c:url value='/admin'/>#f-${campo}" method="POST" class="editor-campos">
                <input type="hidden" name="action" value="actualizar-empleado">
                <input type="hidden" name="idEmpleado" value="<c:out value='${emp.idEmpleado}'/>">
                <input type="hidden" name="campo" value="<c:out value='${campo}'/>">

                <%-- ---------------------------------------------------------------- carnet (nunca editable) --%>
                <div class="fila-edicion">
                    <span class="etiqueta-edicion">Carnet / ID</span>
                    <div class="valor-edicion">
                        <c:out value="${emp.idEmpleado}"/>
                        <span class="texto-tenue">(no editable)</span>
                    </div>
                    <div class="acciones-fila"></div>
                </div>

                <%-- ---------------------------------------------------------------- nombre --%>
                <c:set var="abierto" value="${todos or campo == 'nombre'}"/>
                <c:set var="filaId" value="f-nombre"/>
                <div id="${filaId}" class="fila-edicion${abierto ? ' abierta' : ''}">
                    <label class="etiqueta-edicion" for="nombre">Nombre</label>
                    <div class="valor-edicion">
                        <c:choose>
                            <c:when test="${abierto}">
                                <input type="text" id="nombre" name="nombre" maxlength="30" required
                                       value="<c:out value='${param.nombre != null ? param.nombre : emp.nombre}'/>">
                            </c:when>
                            <c:otherwise><c:out value="${emp.nombre}"/></c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${abierto and not todos and not empty error}">
                        <div class="error-campo"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${not empty mensajeOk and (param.ok == 'todos' or param.ok == fn:substringAfter(filaId, 'f-'))}">
                        <div class="ok-campo">✓ Guardado</div>
                    </c:if>
                    <div class="acciones-fila">
                        <c:choose>
                            <c:when test="${campo == 'nombre'}">
                                <button type="submit" class="btn">Guardar</button>
                                <a href="${urlEditor}#f-${campo}" class="btn-secundario">Cancelar</a>
                            </c:when>
                            <c:when test="${libre}"><a href="${urlEditor}&campo=nombre#f-nombre" class="btn-secundario">Editar</a></c:when>
                        </c:choose>
                    </div>
                </div>

                <%-- ---------------------------------------------------------------- apellido --%>
                <c:set var="abierto" value="${todos or campo == 'apellido'}"/>
                <c:set var="filaId" value="f-apellido"/>
                <div id="${filaId}" class="fila-edicion${abierto ? ' abierta' : ''}">
                    <label class="etiqueta-edicion" for="apellido">Apellido</label>
                    <div class="valor-edicion">
                        <c:choose>
                            <c:when test="${abierto}">
                                <input type="text" id="apellido" name="apellido" maxlength="30" required
                                       value="<c:out value='${param.apellido != null ? param.apellido : emp.apellido}'/>">
                            </c:when>
                            <c:otherwise><c:out value="${emp.apellido}"/></c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${abierto and not todos and not empty error}">
                        <div class="error-campo"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${not empty mensajeOk and (param.ok == 'todos' or param.ok == fn:substringAfter(filaId, 'f-'))}">
                        <div class="ok-campo">✓ Guardado</div>
                    </c:if>
                    <div class="acciones-fila">
                        <c:choose>
                            <c:when test="${campo == 'apellido'}">
                                <button type="submit" class="btn">Guardar</button>
                                <a href="${urlEditor}#f-${campo}" class="btn-secundario">Cancelar</a>
                            </c:when>
                            <c:when test="${libre}"><a href="${urlEditor}&campo=apellido#f-apellido" class="btn-secundario">Editar</a></c:when>
                        </c:choose>
                    </div>
                </div>

                <%-- ---------------------------------------------------------------- tarifa --%>
                <c:set var="abierto" value="${todos or campo == 'tarifa'}"/>
                <c:set var="filaId" value="f-tarifa"/>
                <div id="${filaId}" class="fila-edicion${abierto ? ' abierta' : ''}">
                    <label class="etiqueta-edicion" for="tarifa">Tarifa por hora</label>
                    <div class="valor-edicion">
                        <c:choose>
                            <c:when test="${abierto}">
                                <input type="number" step="0.01" min="0" id="tarifa" name="tarifa"
                                       placeholder="Vacío = sin tarifa"
                                       value="<c:out value='${param.tarifa != null ? param.tarifa : emp.tarifa}'/>">
                            </c:when>
                            <c:when test="${empty emp.tarifa}"><span class="texto-tenue">Sin tarifa</span></c:when>
                            <c:otherwise>$<fmt:formatNumber value="${emp.tarifa}" minFractionDigits="2" maxFractionDigits="2"/></c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${abierto and not todos and not empty error}">
                        <div class="error-campo"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${not empty mensajeOk and (param.ok == 'todos' or param.ok == fn:substringAfter(filaId, 'f-'))}">
                        <div class="ok-campo">✓ Guardado</div>
                    </c:if>
                    <div class="acciones-fila">
                        <c:choose>
                            <c:when test="${campo == 'tarifa'}">
                                <button type="submit" class="btn">Guardar</button>
                                <a href="${urlEditor}#f-${campo}" class="btn-secundario">Cancelar</a>
                            </c:when>
                            <c:when test="${libre}"><a href="${urlEditor}&campo=tarifa#f-tarifa" class="btn-secundario">Editar</a></c:when>
                        </c:choose>
                    </div>
                </div>

                <%-- ---------------------------------------------------------------- hora de entrada --%>
                <c:set var="abierto" value="${todos or campo == 'hora_entrada'}"/>
                <c:set var="filaId" value="f-hora_entrada"/>
                <div id="${filaId}" class="fila-edicion${abierto ? ' abierta' : ''}">
                    <label class="etiqueta-edicion" for="hora_entrada">Hora de entrada</label>
                    <div class="valor-edicion">
                        <c:choose>
                            <c:when test="${abierto}">
                                <input type="time" id="hora_entrada" name="hora_entrada" required
                                       value="<c:out value='${fn:substring(param.hora_entrada != null ? param.hora_entrada : emp.horaEntrada, 0, 5)}'/>">
                            </c:when>
                            <c:otherwise><c:out value="${fn:substring(emp.horaEntrada, 0, 5)}"/></c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${abierto and not todos and not empty error}">
                        <div class="error-campo"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${not empty mensajeOk and (param.ok == 'todos' or param.ok == fn:substringAfter(filaId, 'f-'))}">
                        <div class="ok-campo">✓ Guardado</div>
                    </c:if>
                    <div class="acciones-fila">
                        <c:choose>
                            <c:when test="${campo == 'hora_entrada'}">
                                <button type="submit" class="btn">Guardar</button>
                                <a href="${urlEditor}#f-${campo}" class="btn-secundario">Cancelar</a>
                            </c:when>
                            <c:when test="${libre}"><a href="${urlEditor}&campo=hora_entrada#f-hora_entrada" class="btn-secundario">Editar</a></c:when>
                        </c:choose>
                    </div>
                </div>

                <%-- ---------------------------------------------------------------- hora de salida --%>
                <c:set var="abierto" value="${todos or campo == 'hora_salida'}"/>
                <c:set var="filaId" value="f-hora_salida"/>
                <div id="${filaId}" class="fila-edicion${abierto ? ' abierta' : ''}">
                    <label class="etiqueta-edicion" for="hora_salida">Hora de salida</label>
                    <div class="valor-edicion">
                        <c:choose>
                            <c:when test="${abierto}">
                                <input type="time" id="hora_salida" name="hora_salida" required
                                       value="<c:out value='${fn:substring(param.hora_salida != null ? param.hora_salida : emp.horaSalida, 0, 5)}'/>">
                            </c:when>
                            <c:otherwise><c:out value="${fn:substring(emp.horaSalida, 0, 5)}"/></c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${abierto and not todos and not empty error}">
                        <div class="error-campo"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${not empty mensajeOk and (param.ok == 'todos' or param.ok == fn:substringAfter(filaId, 'f-'))}">
                        <div class="ok-campo">✓ Guardado</div>
                    </c:if>
                    <div class="acciones-fila">
                        <c:choose>
                            <c:when test="${campo == 'hora_salida'}">
                                <button type="submit" class="btn">Guardar</button>
                                <a href="${urlEditor}#f-${campo}" class="btn-secundario">Cancelar</a>
                            </c:when>
                            <c:when test="${libre}"><a href="${urlEditor}&campo=hora_salida#f-hora_salida" class="btn-secundario">Editar</a></c:when>
                        </c:choose>
                    </div>
                </div>

                <%-- ---------------------------------------------------------------- tiempo de receso --%>
                <c:set var="abierto" value="${todos or campo == 'tiempo_receso'}"/>
                <c:set var="filaId" value="f-tiempo_receso"/>
                <div id="${filaId}" class="fila-edicion${abierto ? ' abierta' : ''}">
                    <label class="etiqueta-edicion" for="tiempo_receso">Tiempo de receso</label>
                    <div class="valor-edicion">
                        <c:choose>
                            <c:when test="${abierto}">
                                <input type="number" min="0" id="tiempo_receso" name="tiempo_receso" required
                                       value="<c:out value='${param.tiempo_receso != null ? param.tiempo_receso : emp.tiempoReceso}'/>">
                            </c:when>
                            <c:otherwise><c:out value="${emp.tiempoReceso}"/> minutos</c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${abierto and not todos and not empty error}">
                        <div class="error-campo"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${not empty mensajeOk and (param.ok == 'todos' or param.ok == fn:substringAfter(filaId, 'f-'))}">
                        <div class="ok-campo">✓ Guardado</div>
                    </c:if>
                    <div class="acciones-fila">
                        <c:choose>
                            <c:when test="${campo == 'tiempo_receso'}">
                                <button type="submit" class="btn">Guardar</button>
                                <a href="${urlEditor}#f-${campo}" class="btn-secundario">Cancelar</a>
                            </c:when>
                            <c:when test="${libre}"><a href="${urlEditor}&campo=tiempo_receso#f-tiempo_receso" class="btn-secundario">Editar</a></c:when>
                        </c:choose>
                    </div>
                </div>

                <%-- ---------------------------------------------------------------- rol --%>
                <c:set var="abierto" value="${todos or campo == 'rol'}"/>
                <c:set var="filaId" value="f-rol"/>
                <c:set var="rolActual" value="${param.rol != null ? param.rol : emp.rol}"/>
                <div id="${filaId}" class="fila-edicion${abierto ? ' abierta' : ''}">
                    <label class="etiqueta-edicion" for="rol">Rol</label>
                    <div class="valor-edicion">
                        <c:choose>
                            <c:when test="${abierto}">
                                <select id="rol" name="rol" required>
                                    <c:forEach var="r" items="${roles}">
                                        <option value="${r}"${r == rolActual ? ' selected' : ''}><c:out value="${r}"/></option>
                                    </c:forEach>
                                </select>
                            </c:when>
                            <c:otherwise><c:out value="${emp.rol}"/></c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${abierto and not todos and not empty error}">
                        <div class="error-campo"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${not empty mensajeOk and (param.ok == 'todos' or param.ok == fn:substringAfter(filaId, 'f-'))}">
                        <div class="ok-campo">✓ Guardado</div>
                    </c:if>
                    <div class="acciones-fila">
                        <c:choose>
                            <c:when test="${campo == 'rol'}">
                                <button type="submit" class="btn">Guardar</button>
                                <a href="${urlEditor}#f-${campo}" class="btn-secundario">Cancelar</a>
                            </c:when>
                            <c:when test="${libre}"><a href="${urlEditor}&campo=rol#f-rol" class="btn-secundario">Editar</a></c:when>
                        </c:choose>
                    </div>
                </div>

                <c:if test="${todos}">
                    <div class="acciones-todos">
                        <button type="submit" class="btn">Guardar todos</button>
                        <a href="${urlEditor}#f-${campo}" class="btn-secundario">Cancelar</a>
                    </div>
                </c:if>
            </form>

            <%-- ---------------------------------------------------------------- contraseña (formulario aparte) --%>
            <c:if test="${libre}">
                <section id="f-password" class="zona-password">
                    <h3>Contraseña</h3>
                    <p class="texto-tenue">
                        La contraseña no se puede ver. Al reiniciarla queda como
                        <code>password123</code>; pídale al empleado que la cambie.
                    </p>
                    <c:if test="${not empty error and param.action == 'reiniciar-password'}">
                        <div class="error-campo"><c:out value="${error}"/></div>
                    </c:if>
                    <c:if test="${not empty mensajeOk and param.ok == 'password'}">
                        <div class="ok-campo">✓ <c:out value="${mensajeOk}"/></div>
                    </c:if>
                    <form action="<c:url value='/admin'/>#f-password" method="POST"
                          onsubmit="return confirm('¿Reiniciar la contraseña de este empleado a password123?');">
                        <input type="hidden" name="action" value="reiniciar-password">
                        <input type="hidden" name="idEmpleado" value="<c:out value='${emp.idEmpleado}'/>">
                        <button type="submit" class="btn-peligro">Reiniciar contraseña</button>
                    </form>
                </section>
            </c:if>
        </c:if>
    </main>
</div>

</body>
</html>
