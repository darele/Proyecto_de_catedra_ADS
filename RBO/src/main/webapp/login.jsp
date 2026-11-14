<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión - Reliable Back Office</title>
    <style>
        * {
            box-sizing: border-box;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        body {
            background-color: #eef2f7;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .login-container {
            background-color: #ffffff;
            padding: 35px 30px;
            border-radius: 12px;
            border-top: 6px solid #003DA5; /* Azul institucional del logo RBO */
            box-shadow: 0 6px 18px rgba(0, 61, 165, 0.12);
            width: 100%;
            max-width: 390px;
        }
        .logo-container {
            text-align: center;
            margin-bottom: 12px;
        }
        .logo-container img {
            width: 115px;
            height: auto;
            display: inline-block;
        }
        .login-container h2 {
            margin-top: 0;
            margin-bottom: 6px;
            text-align: center;
            color: #003DA5;
            font-size: 22px;
        }
        .subtitle {
            text-align: center;
            color: #65676b;
            font-size: 13px;
            margin-bottom: 24px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #212529;
            font-size: 14px;
        }
        .form-group input {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #ccd0d5;
            border-radius: 6px;
            font-size: 15px;
            transition: border-color 0.2s, box-shadow 0.2s;
        }
        .form-group input:focus {
            outline: none;
            border-color: #003DA5;
            box-shadow: 0 0 0 3px rgba(0, 61, 165, 0.18);
        }
        .helper-text {
            display: block;
            margin-top: 5px;
            font-size: 12px;
            color: #65676b;
        }
        .btn-ingresar {
            width: 100%;
            padding: 12px;
            background-color: #003DA5;
            color: #ffffff;
            border: none;
            border-radius: 6px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            transition: background-color 0.2s;
        }
        .btn-ingresar:hover {
            background-color: #002c77;
        }
        .error-msg {
            background-color: #f8d7da;
            color: #842029;
            border: 1px solid #f5c2c7;
            padding: 10px;
            border-radius: 6px;
            margin-bottom: 20px;
            font-size: 14px;
            text-align: center;
        }
    </style>
</head>
<body>

<div class="login-container">
    <div class="logo-container">
        <img src="${pageContext.request.contextPath}/img/logo-rbo.jpg" alt="Logo RBO">
    </div>
    <h2>Iniciar Sesión</h2>
    <div class="subtitle">Sistema de Control de Asistencia</div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="error-msg">
            <%= request.getAttribute("error") %>
        </div>
    <% } %>

    <form action="LoginServlet" method="POST">
        <div class="form-group">
            <label for="carnet">Número de Carnet</label>
            <input type="text" 
                   id="carnet" 
                   name="carnet" 
                   maxlength="10" 
                   placeholder="Introduzca su número de carnet" 
                   required 
                   autofocus>
            <span class="helper-text">Indicación: Introduzca su carnet de empleado (máx. 10 caracteres).</span>
        </div>

        <div class="form-group">
            <label for="password">Contraseña</label>
            <input type="password" 
                   id="password" 
                   name="password" 
                   placeholder="Introduzca su contraseña" 
                   required>
        </div>

        <button type="submit" class="btn-ingresar">Ingresar</button>
    </form>
</div>

</body>
</html> 