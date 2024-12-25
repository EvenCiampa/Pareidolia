package com.pareidolia.servlet;

import com.pareidolia.entity.Event;
import com.pareidolia.entity.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HomePageServlet extends HttpServlet {
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

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

            LocalDateTime now = LocalDateTime.now();
            List<Event> eventi = eventRepository.findAllByDataAfterAndOraAfter(now.toLocalDate(), now.toLocalTime());

            request.setAttribute("utente", account);
            request.setAttribute("eventi", eventMapper.toModel(eventi, account));

            request.getRequestDispatcher("/WEB-INF/jsp/HomePage.jsp").forward(request, response);
        }
    }
}