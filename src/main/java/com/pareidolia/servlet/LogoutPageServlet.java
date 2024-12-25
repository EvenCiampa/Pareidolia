package com.pareidolia.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutPageServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
		Long idUtente = (Long) request.getSession().getAttribute("idUtente");
		if (idUtente != null) {
			request.getSession().removeAttribute("idUtente");
		}
		response.sendRedirect("/login");
	}
}