# Proyecto_de_catedra_ADS

Trabajo colaborativo en sistema de registro de entradas y salidas (**RBO – Sistema de Control de Asistencia**).

La aplicación es una web Java con Servlets + JSP + MySQL. Está organizada según el libro
*Beginning Jakarta EE Web Development* (Manelli & Zambon, 3.ª ed., Apress 2020): arquitectura
**Model 2 (MVC)** de su aplicación de ejemplo `eshop`/`eshopx`.

---

## Requisitos

| Herramienta | Versión | Nota |
|---|---|---|
| JDK | 17 | |
| XAMPP | con MySQL/MariaDB y **Tomcat 8.5** | Tomcat 8.5 usa `javax.*` (no `jakarta.*`) |
| IntelliJ IDEA Ultimate | 2024+ | trae Maven; la edición Community no tiene la integración con Tomcat |

No hace falta instalar Maven aparte: IntelliJ trae uno (panel **Maven** a la derecha).

---

## Primera vez (configuración)

1. **Abrir el proyecto en IntelliJ.** El `pom.xml` está en la carpeta `RBO/`, no en la raíz:
   - *File → Open* → elija la carpeta `RBO` (o `RBO/pom.xml` → *Open as Project*).
   - Si ya abrió la raíz del repositorio: clic derecho sobre `RBO/pom.xml` → **Add as Maven Project**.
   - En *File → Project Structure → Project*, ponga el SDK en **JDK 17**.

2. **Encender MySQL** en el panel de XAMPP.
   La base `rbo_marcas` y las tablas `Empleado`, `Marca` y `Tarifa` se crean solas la primera vez
   que arranca la aplicación.
   El usuario y la contraseña de MySQL están en `RBO/src/main/webapp/META-INF/context.xml`
   (XAMPP trae `root` sin contraseña).

3. **Configurar la SALT** de las contraseñas (sin ella el login falla):
   copie `RBO/src/main/resources/secret.properties.example` como
   `RBO/src/main/resources/secret.properties` y ponga la SALT que usa el equipo.
   Ese archivo está en el `.gitignore` y no se sube.
   Otra opción es definir la variable de entorno `ADS_SALT`.

4. **Arrancar la aplicación una vez** (ver [Cómo correrlo](#cómo-correrlo)) para que se creen la base y las tablas.
   Otra opción es crearlas a mano: en phpMyAdmin ejecute `CREATE DATABASE IF NOT EXISTS rbo_marcas; USE rbo_marcas;`
   y luego el contenido de `RBO/src/main/resources/sv/edu/udb/rbo/model/rbo_create.sql`.

5. **Crear el primer usuario** (si la tabla `Empleado` no tiene usuarios):
   1. En IntelliJ abra `RBO/src/main/java/sv/edu/udb/rbo/model/SeguridadUtil.java`, clic derecho → **Run 'SeguridadUtil.main()'**.
      Luego, en *Run → Edit Configurations → SeguridadUtil*, ponga la contraseña en **Program arguments** y ejecútelo otra vez.
      Imprime `contraseña -> hash`.
   2. Encienda también **Apache** en XAMPP (phpMyAdmin funciona sobre Apache) y abra http://localhost/phpmyadmin.
      En la base `rbo_marcas`, pestaña SQL, ejecute:
      ```sql
      INSERT INTO Empleado (id_empleado, nombre, Apellido, tarifa, hora_entrada, hora_salida, tiempo_receso, rol, password)
      VALUES ('A001', 'Ana', 'López', NULL, '08:00', '17:00', 60, 'admin', 'PEGUE_AQUI_EL_HASH');
      ```
      Los roles válidos son `admin`, `RH`, `Contabilidad` y `Empleado`.

---

## Cómo correrlo

### Opción A: desde IntelliJ (Run Configuration de Tomcat)

1. **Detenga Tomcat en XAMPP.** IntelliJ arranca su propio Tomcat en el puerto 8080; MySQL sí debe quedar encendido.
2. En el panel **Maven**, pulse **Reload** (🔄) para que IntelliJ lea el `pom.xml`.
3. *Run → Edit Configurations → **+** → Tomcat Server → Local*:
   - **Server** → *Application server*: `C:\xampp\tomcat` (Tomcat 8.5.x).
   - **Server** → *URL*: `http://localhost:8080/RBO/`
   - **Deployment** → **+** → *Artifact* → `RBO:war exploded`, *Application context*: `/RBO`
4. **Run** ▶ o **Debug** 🐞. Se abre `http://localhost:8080/RBO/`, que lleva al login.

> La URL y el *Application context* deben coincidir (`/RBO`). Si no coinciden, Tomcat responde 404.

### Opción B: con el WAR en el Tomcat de XAMPP

1. Panel **Maven** → *Lifecycle* → **package**. Genera `RBO\target\RBO.war`.
2. Copie `RBO\target\RBO.war` a `C:\xampp\tomcat\webapps\`.
3. Encienda Tomcat en XAMPP y abra http://localhost:8080/RBO/

Desde la terminal de IntelliJ (PowerShell), el paso 1 es:
```powershell
cd RBO
& "C:\Program Files\JetBrains\IntelliJ IDEA <versión>\plugins\maven\lib\maven3\bin\mvn.cmd" package
```

### Pruebas unitarias

Panel **Maven** → *Lifecycle* → **test**. En la terminal, use el mismo comando de arriba cambiando `package` por `test`.
No necesitan Tomcat ni la base de datos.

---

## Estructura del proyecto

Es la estructura de `eshop` del libro (Listing 10-1) llevada a un proyecto Maven:

```
RBO/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/sv/edu/udb/rbo/
    │   │   ├── RboServlet.java          ← CONTROLADOR de la interfaz de empleado (como ShopServlet)
    │   │   ├── ModoDesarrollo.java      ← interruptor para probar /admin sin login
    │   │   ├── admin/
    │   │   │   └── AdminServlet.java    ← CONTROLADOR de la Interfaz de Admin
    │   │   ├── beans/                   ← MODELO: JavaBeans (datos que viajan a las vistas)
    │   │   │   ├── Empleado.java
    │   │   │   ├── Marca.java
    │   │   │   ├── AccionMarca.java     ← acciones del botón de marca (Entrada, Receso, …)
    │   │   │   ├── EstadoMarca.java     ← estado de la jornada (colores y textos de la vista)
    │   │   │   └── ResumenAdmin.java
    │   │   └── model/                   ← MODELO: acceso a datos (DataManager + Peers)
    │   │       ├── DataManager.java     ← ÚNICO punto de entrada a la base de datos
    │   │       ├── EmpleadoPeer.java    ← SQL de la tabla Empleado
    │   │       ├── MarcaPeer.java       ← SQL de la tabla Marca
    │   │       ├── EsquemaPeer.java     ← crea las tablas (reemplaza a DBHandler)
    │   │       └── SeguridadUtil.java   ← SHA-256 + SALT
    │   ├── resources/
    │   │   ├── secret.properties.example
    │   │   └── sv/edu/udb/rbo/model/rbo_create.sql   ← script de las tablas
    │   └── webapp/
    │       ├── index.jsp                ← redirige a /rbo
    │       ├── css/rbo.css              ← ÚNICA hoja de estilos
    │       ├── images/logo-rbo.jpg
    │       ├── META-INF/context.xml     ← pool de conexiones JNDI (jdbc/rbo)
    │       └── WEB-INF/
    │           ├── web.xml              ← servlets, mapeos, páginas de error, resource-ref
    │           └── jsp/                 ← VISTAS (no se pueden pedir directo desde el navegador)
    │               ├── Login.jsp
    │               ├── MarcaEmpleado.jsp
    │               ├── TopMenu.jsp      ← fragmento: barra superior (jsp:include)
    │               ├── Error.jsp
    │               └── admin/
    │                   ├── AdminInicio.jsp      ← "hola mundo" de la Interfaz de Admin
    │                   ├── AdminEmpleados.jsp   ← ejemplo de pantalla con datos
    │                   └── AdminMenu.jsp        ← fragmento: menú lateral
    └── test/java/sv/edu/udb/rbo/        ← pruebas JUnit 5
```

### Cómo viaja una petición (Model 2)

```
Navegador ──► /rbo?action=marca ──► RboServlet (controlador)
                                         │ 1. revisa la sesión
                                         │ 2. pide datos al DataManager ──► EmpleadoPeer / MarcaPeer ──► MySQL
                                         │ 3. deja los resultados en el request (beans)
                                         ▼
                                   forward a /WEB-INF/jsp/MarcaEmpleado.jsp (vista: solo JSTL + EL)
```

| URL | Controlador | Acciones (`action=`) |
|---|---|---|
| `/RBO/rbo` | `RboServlet` | *(ninguna)* login · `login` · `logout` · `marca` · `registrarMarca` |
| `/RBO/admin` | `AdminServlet` | *(ninguna)* / `inicio` · `empleados` |

### Reglas del proyecto (tomadas del libro)

| Regla | Capítulo |
|---|---|
| Un **controlador** por interfaz; el parámetro `action` decide la vista; `doGet` delega en `doPost`. | 3, 5, 10 |
| Las vistas **no** acceden a la base: el controlador pide los datos y los pasa como beans (Model 2, Fig. 3-2). | 3 |
| El `DataManager` es el único punto de entrada a la base y delega el SQL en una clase `*Peer` por tabla. | 5 |
| Una interfaz separada por rol (administrador y empleado). | 4 |
| Beans en `beans/`: campos privados, constructor vacío y `get`/`set` por propiedad. | 4, 6 |
| **Sin scriptlets** (`<% %>`): solo JSTL (`c:if`, `c:choose`, `c:forEach`, `c:url`) y EL (`${...}`). | 6, 7, 10 |
| Mostrar datos con `<c:out>` (escapa HTML). | 6 |
| Rutas con `<c:url>`, nunca con `/RBO` escrito a mano. | 10 |
| Partes comunes en fragmentos con `<jsp:include page="..." flush="true"/>`. | 6, 10 |
| Una sola hoja de estilos externa (`css/rbo.css`), sin `<style>` en las JSP. | 3, 5 |
| Servlets y sus mapeos en `web.xml` (como en eshopx). | 7, 10 |
| Datos de conexión fuera del código: en `context.xml`, con un pool JNDI. | 9, 10 |
| Validar siempre en el servidor. | 10 |

### Diferencias con el libro, y por qué

- **El controlador pide los datos, no la JSP.** En `eshop` (capítulos 5, 6 y 10) el `ShopServlet` solo despacha, y cada JSP llama al `DataManager` con `<jsp:useBean scope="application">`. Aquí se sigue el Model 2 del capítulo 3: el controlador pide los datos y los deja en el request, y las vistas solo los muestran, sin scriptlets ni lógica.
- **Mapeo en `web.xml` y no con `@WebServlet`**: el capítulo 5 usa la anotación. Se eligió la forma de eshopx (capítulos 7 y 10) para tener toda la configuración en un archivo.
- **Vistas en `WEB-INF/jsp/`** (el libro las pone en `/jsp/`): así nadie las abre sin pasar por el controlador, que es el que revisa la sesión.
- **`PreparedStatement`** en lugar de concatenar SQL como hace el libro: evita la inyección SQL.
- **try-with-resources** en lugar de `putConnection()`: es la misma regla del libro (cerrar en `finally`), escrita con la sintaxis de Java 7+.
- **`web.xml` 3.1 y `maxTotal`/`maxWaitMillis` en `context.xml`**: son los valores que entiende Tomcat 8.5. El libro usa Tomcat 9 y los nombres antiguos de DBCP.
- **Driver JDBC dentro del WAR** (la opción "portable" del libro), con des-registro en `RboServlet.destroy()` para que Tomcat no avise de fugas de memoria al redesplegar.
- **Sin JSF** (capítulos 8 y 9): Tomcat no lo incluye. Se sigue la versión JSP + JSTL del libro.
- **Nombres de tablas sin cambios** (`Empleado`, `Marca`, `Tarifa`): son los que ya existen en las bases del equipo.

---

## Interfaz de Admin (`interfaz-admin`)

- **URL:** `http://localhost:8080/RBO/admin`
- **Quién entra:** quien inicia sesión con el rol `admin`; el login lo redirige ahí. Los demás roles reciben *403 – Acceso denegado*.
- **Archivos:** `admin/AdminServlet.java` (controlador) y `WEB-INF/jsp/admin/*.jsp` (vistas). Comparte con el resto de la aplicación el `DataManager`, la barra superior y la hoja de estilos.

### Probarla de forma desacoplada (sin login ni usuarios)

Active el **modo desarrollo** solo en su máquina:
*Run → Edit Configurations → RBO (Tomcat) → Server → **VM options*** y agregue:

```
-Drbo.modoDesarrollo=true
```

Con eso, `http://localhost:8080/RBO/admin` entra directo con un *admin de prueba* (aparece un aviso amarillo).
No necesita usuarios en la base ni la SALT. Si MySQL está apagado, la portada lo indica en lugar de fallar.
La sesión de prueba solo sirve para `/admin`.
Hay otra forma de activarlo: el `context-param` `modoDesarrollo` de `web.xml`. **Déjelo en `false`**, porque ese archivo sí se sube a git.

Las reglas de la sección también se prueban sin servidor en `src/test/java/sv/edu/udb/rbo/admin/AdminServletTest.java`.

### Agregar una pantalla

1. `AdminServlet.resolverVista`: un `case` con el nuevo `action`.
2. `AdminServlet.prepararDatos`: pida los datos al `DataManager` (agregue el método ahí y el SQL en su `*Peer`).
3. Cree la vista en `WEB-INF/jsp/admin/` copiando `AdminInicio.jsp`.
4. Agregue el enlace en `AdminMenu.jsp` y un caso en `AdminServletTest`.

---

## Cambios de comportamiento respecto a la versión anterior

- La base de datos y las tablas se crean al **arrancar** la aplicación. Si eso falla, por ejemplo porque MySQL estaba apagado, se reintenta en el siguiente login o al abrir el admin, y el error solo queda en el log.
- Los roles **RH** y **Contabilidad** van a la marca de asistencia. Antes se redirigían a `rh.jsp` y `contabilidad.jsp`, que no existen, y daba 404.
- Después de marcar **Salida**, la caja queda en rojo aunque se recargue la página. Antes volvía a verde.
- El estado de la marca solo avanza si la marca **se guardó** en la base.
- Un `tipo_marca` desconocido se ignora. Antes se registraba como salida.
- Los nombres y comentarios se muestran escapados, para evitar inyección de HTML.
