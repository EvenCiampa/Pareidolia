<%@ page import="java.util.Optional" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Registrazione Evento - Pareidolia</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
<div class="floating-container">
    <div class="floating-container-body">
        <h1 class="mt-0 text-center">Registrazione event</h1>

        <p><a href="/">indietro</a></p>

        <%-- Mostra un messaggio di errore se è stato inviato un parametro di errore --%>
        <% String errorMessage = (String) request.getAttribute("errorMessage");
            if (errorMessage != null && !errorMessage.isEmpty()) { %>
        <p style="color: red;"><%= errorMessage %>
        </p>
        <% } %>

        <form class="full-page" method="post">
            <label for="titolo">Titolo</label>
            <input type="text" id="titolo" name="titolo"
                   value="<%= Optional.ofNullable(request.getParameter("titolo")).orElse("") %>" required>

            <label for="descrizione">Descrizione</label>
            <input type="text" id="descrizione" name="descrizione"
                   value="<%= Optional.ofNullable(request.getParameter("descrizione")).orElse("") %>" required>

            <label for="data">Data</label>
            <input type="date" id="data" name="data"
                   value="<%= Optional.ofNullable(request.getParameter("data")).orElse("") %>" required>

            <label for="ora">Ora</label>
            <input type="time" id="ora" name="ora"
                   value="<%= Optional.ofNullable(request.getParameter("ora")).orElse("") %>" required>

            <label for="luogo">Luogo</label>
            <input type="text" id="luogo" name="luogo"
                   value="<%= Optional.ofNullable(request.getParameter("luogo")).orElse("") %>" required>

            <label for="numeroMassimoPartecipanti">Numero massimo partecipanti</label>
            <input type="number" id="numeroMassimoPartecipanti" name="numeroMassimoPartecipanti"
                   value="<%= Optional.ofNullable(request.getParameter("maxNumberOfParticipants")).orElse("") %>"
                   required>

            <br/>
            <br/>

            <input type="submit" value="Salva">

        </form>
    </div>
</div>
</body>
</html>