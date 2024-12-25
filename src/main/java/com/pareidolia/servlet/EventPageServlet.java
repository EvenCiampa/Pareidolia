package com.pareidolia.servlet;

import com.pareidolia.entity.Account;
import com.pareidolia.entity.Booking;
import com.pareidolia.entity.Event;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/evento/*")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventPageServlet extends HttpServlet {
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

			request.setAttribute("utente", account);
			request.setAttribute("evento", eventMapper.toModel(event, account));

			request.getRequestDispatcher("/WEB-INF/jsp/EventPage.jsp").forward(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// prendi l'utente dalla sessione
		Long idUtente = (Long) request.getSession().getAttribute("idUtente");
		if (idUtente == null) {
			response.sendRedirect("/login");
		} else {
			Account account = userRepository.findById(idUtente)
				.orElseThrow(() -> new IllegalStateException("Id utente non più valido"));

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

			boolean partecipa;
			try {
				partecipa = Boolean.parseBoolean(request.getParameter("partecipa"));
			} catch (Exception ignore) {
				response.sendRedirect("/evento/" + idEvento);
				return;
			}

			Optional<Booking> prenotazione = bookingRepository.findByIdEventoAndIdUtente(event.getId(), account.getId());
			if (partecipa && prenotazione.isEmpty()) {
				Long numeroPartecipanti = bookingRepository.countAllByIdEvento(event.getId());

				if (numeroPartecipanti >= event.getMaxNumberOfParticipants()) {
					request.setAttribute("confermaPrenotazione", false);
				} else {
					bookingRepository.save(Booking.builder()
						.idEvento(event.getId())
						.idUtente(account.getId())
						.build());

					request.setAttribute("confermaPrenotazione", true);
				}
			} else if (!partecipa && prenotazione.isPresent()) {
				bookingRepository.deleteById(prenotazione.get().getId());

				request.setAttribute("confermaCancellazione", true);
			}

			request.setAttribute("utente", account);
			request.setAttribute("evento", eventMapper.toModel(event, account));

			request.getRequestDispatcher("/WEB-INF/jsp/EventPage.jsp").forward(request, response);
		}
	}
}