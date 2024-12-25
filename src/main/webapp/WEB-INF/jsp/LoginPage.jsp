<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login - Pareidolia</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
<div class="floating-container">
    <div class="floating-container-body">
        <h1 class="mt-0 text-center">Login - Pareidolia</h1>

        <%-- Mostra un messaggio di errore se è stato inviato un parametro di errore --%>
        <% String errorMessage = (String) request.getAttribute("errorMessage");
            if (errorMessage != null && !errorMessage.isEmpty()) { %>
        <p style="color: red;"><%= errorMessage %>
        </p>
        <% } %>

        <form class="full-page" method="post">
            <label for="email">Email</label>
            <input type="text" id="email" name="email" required>

            <label for="password">Password</label>
            <input type="password" id="password" name="password" required>

            <br/>
            <br/>

            <input type="submit" value="Login">
        </form>

        <br/>
        <br/>

        <p>Non sei registrato? <a href="/register">Effettua la registrazione</a></p>
    </div>
</div>
</body>
</html>