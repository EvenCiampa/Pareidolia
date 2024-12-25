<%@ page import="java.util.Optional" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Registrazione - Pareidolia</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
<div class="floating-container">
    <div class="floating-container-body">
        <h1 class="mt-0 text-center">Registrazione - Pareidolia</h1>

        <%-- Mostra un messaggio di errore se è stato inviato un parametro di errore --%>
        <% String errorMessage = (String) request.getAttribute("errorMessage");
            if (errorMessage != null && !errorMessage.isEmpty()) { %>
        <p style="color: red;"><%= errorMessage %>
        </p>
        <% } %>

        <form method="post">
            <label for="username">Username</label>
            <input type="text" id="username" name="username"
                   value="<%= Optional.ofNullable(request.getParameter("username")).orElse("") %>" required>

            <label for="email">Email</label>
            <input type="email" id="email" name="email"
                   value="<%= Optional.ofNullable(request.getParameter("email")).orElse("") %>" required>

            <label for="password">Password</label>
            <input type="password" id="password" name="password" required>

            <label for="confirm_password">Ripeti Password</label>
            <input type="password" id="confirm_password" name="confirm_password" required>

            <br/>
            <br/>

            <input type="submit" value="Registrati">
        </form>

        <br/>
        <br/>

        <p>Sei già registrato? <a href="/login">Effettua il login</a></p>
    </div>
</div>
</body>
</html>