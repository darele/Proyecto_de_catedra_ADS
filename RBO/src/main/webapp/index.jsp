<%-- 
    Document   : index
    Created on : Nov 8, 2026, 4:22:46 PM
    Author     : pdarw
--%>

<%@page import="basededatos.DBHandler"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>This is Index.JSP</title>
    </head>
    <body>
        
        <h1>Hello I am Index.jsp!</h1>
        
        <%
            DBHandler baseDatos = new DBHandler();
            baseDatos.init();
        %>
    </body>
</html>
