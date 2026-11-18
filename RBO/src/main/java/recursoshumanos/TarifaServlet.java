
package recursoshumanos;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/RH/registrarTarifa")
public class TarifaServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String idEmpleado = request.getParameter("idEmpleado");
        String fecha = request.getParameter("fecha");
        String entrada = request.getParameter("horaEntrada");
        String salida = request.getParameter("horaSalida");
        String monto = request.getParameter("tarifa");

        String mensaje;

        try {

            // VALIDAR CARNET DEL EMPLEADO

            if (idEmpleado == null ||
                idEmpleado.trim().isEmpty()) {

                throw new IllegalArgumentException(
                    "Ingresá el carnet del empleado."
                );
            }

            idEmpleado = idEmpleado.trim();

            if (idEmpleado.length() > 10) {
                throw new IllegalArgumentException(
                    "El carnet no puede superar 10 caracteres."
                );
            }

            // VALIDAR TARIFA

            if (monto == null || monto.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Ingresá una tarifa."
                );
            }

            double valorTarifa = Double.parseDouble(monto);

            if (!Double.isFinite(valorTarifa) ||
                valorTarifa <= 0) {

                throw new IllegalArgumentException(
                    "La tarifa debe ser mayor que cero."
                );
            }

            // VALIDAR FECHA

            if (fecha == null || fecha.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Seleccioná una fecha."
                );
            }

            LocalDate fechaTarifa = LocalDate.parse(fecha);

            // VALIDAR HORARIOS

            if (entrada == null || entrada.trim().isEmpty() ||
                salida == null || salida.trim().isEmpty()) {

                throw new IllegalArgumentException(
                    "Completá los horarios."
                );
            }

            LocalTime horaEntrada = LocalTime.parse(entrada);
            LocalTime horaSalida = LocalTime.parse(salida);

            if (!horaSalida.isAfter(horaEntrada)) {
                throw new IllegalArgumentException(
                    "La hora de salida debe ser posterior "
                    + "a la hora de entrada."
                );
            }

            // REGISTRAR TARIFA EN MYSQL

            TarifaDAO dao = new TarifaDAO();

            boolean registrado = dao.registrarTarifa(
                idEmpleado,
                Date.valueOf(fechaTarifa),
                Time.valueOf(horaEntrada),
                Time.valueOf(horaSalida),
                valorTarifa
            );

            if (registrado) {
                mensaje = "Tarifa registrada correctamente.";
            } else {
                mensaje = "No se pudo registrar la tarifa. "
                        + "Verificá que el empleado exista.";
            }

        } catch (DateTimeParseException e) {

            mensaje = "La fecha o los horarios no son válidos.";

        } catch (NumberFormatException e) {

            mensaje = "Ingresá una tarifa numérica válida.";

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();

        } catch (Exception e) {

            mensaje = "Ocurrió un error al registrar la tarifa.";
            log("Error al registrar tarifa", e);
        }

        request.setAttribute("mensaje", mensaje);

        request.getRequestDispatcher("/RH/RHindex.jsp")
               .forward(request, response);
    }
}
