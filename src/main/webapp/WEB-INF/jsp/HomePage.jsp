<%@ page import="com.pareidolia.entity.Account" %>
<%@ page import="com.pareidolia.model.EventModel" %>
<%@ page import="java.util.List" %>
<%@ page import="com.pareidolia.entity.Account" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Pareidolia</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
<div class="container">
    <%
        Account account = (Account) request.getAttribute("account");
        List<EventModel> eventi = (List<EventModel>) request.getAttribute("eventi");
    %>
    <h1 class="mt-0 text-center">Benvenuto/a <%= account.getUsername() %> nel Sistema di Pareidolia</h1>

    <div class="display-flex justify-space-around">
        <% if (account.getAdmin()) { %>
        <a href="/registraEvento"><button class="max-200"> Registra event </button></a>
        <% } %>
        <a href="/logout"><button class="max-200"> Logout </button></a>
    </div>

    <br/>
    <br/>

    <%  for (EventModel event : eventi) { %>
    <div class="card">
        <h3 class="card-title"><%= event.getTitolo() %></h3>
        <p class="card-subtitle">Data: <%= event.getData() %></p>
        <p class="card-subtitle">Ora: <%= event.getOra() %></p>
        <p class="card-subtitle">Luogo: <%= event.getLuogo() %></p>
        <p class="card-subtitle">Partecipanti: <%= event.getNumeroPartecipanti() %>/<%= event.getNumeroMassimoPartecipanti() %></p>
        <br/>
        <p><a href="/event/<%=event.getId()%>"> Visualizza dettagli </a></p>
    </div>
    <% } %>

</div>
</body>
</html>