package com.pareidolia.servlet;

import com.pareidolia.entity.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@WebServlet("/register")
public class RegisterPageServlet extends HttpServlet {
    @Autowired
    UserRepository userRepository;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long idUtente = (Long) request.getSession().getAttribute("idUtente");
        if (idUtente == null) {
            request.getRequestDispatcher("/WEB-INF/jsp/RegisterPage.jsp").forward(request, response);
        } else {
            response.sendRedirect("/");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long idUtente = (Long) request.getSession().getAttribute("idUtente");
        if (idUtente != null) {
            response.sendRedirect("/");
            return;
        }

        // Recupera i valori dei parametri dal form di registrazione
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirm_password");

        String errorMessage = null;

        // Verifica che i campi non siano vuoti e che la password corrisponda alla conferma della password
        if (username == null || username.isEmpty()) {
            errorMessage = "Username non valido";
        } else if (username.length() <= 2) {
            errorMessage = "Lo username deve avere almeno 2 caratteri";
        } else if (password == null || password.isEmpty()) {
            errorMessage = "Password non valida";
        } else if (!password.equals(confirmPassword)) {
            errorMessage = "Le password non coincidono";
        } else if (password.length() <= 2) {
            errorMessage = "La password deve avere almeno 2 caratteri";
        } else if (email == null || email.isEmpty()) {
            errorMessage = "Email non valida";
        } else if (!EmailValidator.getInstance(false).isValid(email)) {
            errorMessage = "L'email non ha un formato valido";
        } else if (userRepository.findByEmail(email).isPresent()) {
            errorMessage = "L'email è già in uso";
        } else if (userRepository.findByUsername(username).isPresent()) {
            errorMessage = "Lo username è già in uso";
        }

        // in caso di errore salva l'errore su errorMessage

        if (errorMessage == null) {
            userRepository.save(Account.builder()
                    .username(username)
                    .email(email)
                    .password(password)
                    .admin(false)
                    .build());

            // Reindirizza l'utente alla pagina di login dopo la registrazione
            response.sendRedirect("/login");
        } else {
            request.setAttribute("errorMessage", errorMessage);
            // Se ci sono errori nella registrazione, reindirizza l'utente nuovamente alla pagina di registrazione con un messaggio di errore
            request.getRequestDispatcher("/WEB-INF/jsp/RegisterPage.jsp").forward(request, response);
        }
    }
}