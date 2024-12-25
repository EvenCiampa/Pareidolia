package com.pareidolia.servlet;

import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@WebServlet("/modificaEvento/*")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EditEventPageServlet extends HttpServlet {
	private final EventMapper eventMapper;
	private final UserRepository userRepository;
	private final EventRepository eventRepository;
	private final BookingRepository bookingRepository;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		// prendi l'utente dalla sessione
		Long idUtente = (Long) request.getSession().getAttribute("idUtente");
		if (idUtente == null) {
			response.sendRedirect("/login");
		} else {
			Account account = userRepository.findById(idUtente)
				.orElseThrow(() -> new IllegalStateException("Id utente non più valido"));

			// se non è admin faccio il redirect alla home
			if (!account.getAdmin()) {
				response.sendRedirect("/");
				return;
			}

			long idEvento;
			try {
				idEvento = Long.parseLong(request.getPathInfo().substring(1));
			} catch (Exception ignore) {
				response.sendRedirect("/");
				return;
			}

			Event event = eventRepository.findById(idEvento).orElse(null);

			if (event == null) {
				response.sendRedirect("/");
				return;
			}

			request.setAttribute("evento", eventMapper.toModel(event, account));

			request.getRequestDispatcher("/WEB-INF/jsp/EditEventPage.jsp").forward(request, response);
		}
	}


	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Long idUtente = (Long) request.getSession().getAttribute("idUtente");
		if (idUtente == null) {
			response.sendRedirect("/login");
		} else {
			Account account = userRepository.findById(idUtente)
				.orElseThrow(() -> new IllegalStateException("Id utente non più valido"));

			// se non è admin faccio il redirect alla home
			if (!account.getAdmin()) {
				response.sendRedirect("/");
				return;
			}

			long idEvento;
			try {
				idEvento = Long.parseLong(request.getPathInfo().substring(1));
			} catch (Exception ignore) {
				response.sendRedirect("/");
				return;
			}

			Event event = eventRepository.findById(idEvento).orElse(null);

			if (event == null) {
				response.sendRedirect("/");
				return;
			}

			Long numeroPartecipanti = bookingRepository.countAllByIdEvento(event.getId());

			// Recupera i valori dei parametri dal form di registrazione
			String titolo = request.getParameter("titolo");
			String descrizione = request.getParameter("descrizione");
			String luogo = request.getParameter("luogo");

			LocalDate data = null;
			try {
				data = LocalDate.parse(request.getParameter("data"));
			} catch (Exception ignored) {
			}

			LocalTime ora = null;
			try {
				ora = LocalTime.parse(request.getParameter("ora"));
			} catch (Exception ignored) {
			}

			Long numeroMassimoPartecipanti = null;
			try {
				numeroMassimoPartecipanti = Long.parseLong(request.getParameter("maxNumberOfParticipants"));
			} catch (Exception ignored) {
			}

			String errorMessage = null;
			LocalDateTime now = LocalDateTime.now();

			// Verifica che i campi non siano vuoti e che la password corrisponda alla conferma della password
			if (titolo == null || titolo.isEmpty()) {
				errorMessage = "Titolo non valido";
			} else if (titolo.length() <= 2) {
				errorMessage = "Il titolo deve avere almeno 2 caratteri";
			} else if (luogo == null || luogo.isEmpty()) {
				errorMessage = "Luogo non valido";
			} else if (luogo.length() <= 2) {
				errorMessage = "Il luogo deve avere almeno 2 caratteri";
			} else if (data == null || data.isBefore(now.toLocalDate())) {
				errorMessage = "Data non valida";
			} else if (ora == null || data.equals(now.toLocalDate()) && ora.isBefore(now.toLocalTime())) {
				errorMessage = "Ora non valida";
			} else if (numeroMassimoPartecipanti == null || numeroMassimoPartecipanti < 0) {
				errorMessage = "Numero massimo partecipanti non valido";
			} else if (numeroPartecipanti > numeroMassimoPartecipanti) {
				errorMessage = String.format("Numero massimo partecipanti già superato (%s)", numeroPartecipanti);
			}

			// in caso di errore salva l'errore su errorMessage
			if (errorMessage == null) {
				event.setTitolo(titolo);
				event.setDescrizione(descrizione);
				event.setLuogo(luogo);
				event.setData(data);
				event.setOra(ora);
				event.setMaxNumberOfParticipants(numeroMassimoPartecipanti);
				eventRepository.save(event);

				// Reindirizza l'utente alla pagina dell'evento dopo la modifica
				response.sendRedirect("/evento/" + event.getId());
			} else {
				request.setAttribute("errorMessage", errorMessage);
				request.setAttribute("evento", eventMapper.toModel(event, account));
				// Se ci sono errori nella registrazione, reindirizza l'utente nuovamente alla pagina di registrazione con un messaggio di errore
				request.getRequestDispatcher("/WEB-INF/jsp/EditEventPage.jsp").forward(request, response);
			}
		}
	}
}