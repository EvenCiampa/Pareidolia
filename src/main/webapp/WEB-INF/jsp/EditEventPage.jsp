<%@ page import="java.util.Optional" %>
<%@ page import="com.pareidolia.model.EventModel" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Modifica Evento - Pareidolia</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
<div class="floating-container">
    <div class="floating-container-body">
        <h1 class="mt-0 text-center">Modifica event</h1>

        <p><a href="/">indietro</a></p>

        <%
            EventModel event = (EventModel) request.getAttribute("event");
        %>

        <%-- Mostra un messaggio di errore se è stato inviato un parametro di errore --%>
        <% String errorMessage = (String) request.getAttribute("errorMessage");
            if (errorMessage != null && !errorMessage.isEmpty()) { %>
        <p style="color: red;"><%= errorMessage %>
        </p>
        <% } %>

        <form class="full-page" method="post">
            <label for="titolo">Titolo</label>
            <input type="text" id="titolo" name="titolo"
                   value="<%= Optional.ofNullable(request.getParameter("titolo")).orElse(
                       Optional.ofNullable(event.getTitolo()).orElse("")
                   ) %>" required>

            <label for="descrizione">Descrizione</label>
            <input type="text" id="descrizione" name="descrizione"
                   value="<%= Optional.ofNullable(request.getParameter("descrizione")).orElse(
                       Optional.ofNullable(event.getDescrizione()).orElse("")
                   ) %>" required>

            <label for="data">Data</label>
            <input type="date" id="data" name="data"
                   value="<%= Optional.ofNullable(request.getParameter("data")).orElse(
                       Optional.ofNullable(event.getData())
                       .map(Object::toString).orElse("")
                   ) %>" required>

            <label for="ora">Ora</label>
            <input type="time" id="ora" name="ora"
                   value="<%= Optional.ofNullable(request.getParameter("ora")).orElse(
                       Optional.ofNullable(event.getOra())
                       .map(Object::toString).orElse("")
                   ) %>" required>

            <label for="luogo">Luogo</label>
            <input type="text" id="luogo" name="luogo"
                   value="<%= Optional.ofNullable(request.getParameter("luogo")).orElse(
                       Optional.ofNullable(event.getLuogo()).orElse("")
                   ) %>" required>

            <label for="numeroMassimoPartecipanti">Numero massimo partecipanti</label>
            <input type="number" id="numeroMassimoPartecipanti" name="numeroMassimoPartecipanti"
                   value="<%= Optional.ofNullable(request.getParameter("maxNumberOfParticipants")).orElse(
                       Optional.ofNullable(event.getNumeroMassimoPartecipanti())
                       .map(Object::toString).orElse("")
                   ) %>"
                   required>

            <br/>
            <br/>

            <input type="submit" value="Salva">

        </form>
    </div>
</div>
</body>
</html>