<%@ page import="com.pareidolia.model.EventModel" %>
<%@ page import="com.pareidolia.entity.Account" %>
<%@ page import="com.pareidolia.entity.Account" %>
<%@ page import="com.pareidolia.entity.Account" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Evento - Pareidolia</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
<div class="floating-container">
    <div class="floating-container-body">
        <%
            Account account = (Account) request.getAttribute("account");
            EventModel event = (EventModel) request.getAttribute("event");
        %>

        <h1 class="mt-0 text-center">Evento</h1>

        <p><a href="/">indietro</a></p>

        <% if (account.getAdmin()) { %>
        <div class="display-flex justify-space-around">
            <a href="/modificaEvento/<%= event.getId() %>"><button class="max-200"> Modifica event </button></a>
        </div>
        <% } %>

        <br/>
        <br/>

        <%-- Mostra un messaggio se è stato inviato il parametro booleano confermaPrenotazione oppure confermaCancellazione --%>
        <%
            Boolean confermaPrenotazione = (Boolean) request.getAttribute("confermaPrenotazione");
            Boolean confermaCancellazione = (Boolean) request.getAttribute("confermaCancellazione");
            if (confermaPrenotazione != null) {
                if (confermaPrenotazione) {
        %>
        <p style="color: green;">Partecipazione confermata</p>
        <% } else { %>
        <p style="color: red;">L'event è al completo</p>
        <% }
        } else if (confermaCancellazione != null && confermaCancellazione) { %>
        <p style="color: green;">Partecipazione annullata</p>
        <% } %>

        <div class="card">
            <h3 class="card-title"><%= event.getTitolo() %>
            </h3>
            <p class="card-subtitle">Data: <%= event.getData() %>
            </p>
            <p class="card-subtitle">Ora: <%= event.getOra() %>
            </p>
            <p class="card-subtitle">Luogo: <%= event.getLuogo() %>
            </p>
            <p class="card-subtitle">Descrizione: <%= event.getDescrizione() %>
            </p>
            <p class="card-subtitle">Partecipanti:
                <%= event.getNumeroPartecipanti() %>/<%= event.getNumeroMassimoPartecipanti() %>
            </p>
            <br/>

            <% if (event.getPartecipa()) { %>
            <form method="post">
                <input type="hidden" name="partecipa" value="false">
                <input type="submit" value="Annulla partecipazione">
            </form>
            <% } else if (event.getNumeroPartecipanti() < event.getNumeroMassimoPartecipanti()) { %>
            <form method="post">
                <input type="hidden" name="partecipa" value="true">
                <input type="submit" value="Partecipa">
            </form>
            <% } else { %>
            <p style="color: red;">L'event è al completo</p>
            <% } %>
        </div>

    </div>
</div>
</body>
</html>