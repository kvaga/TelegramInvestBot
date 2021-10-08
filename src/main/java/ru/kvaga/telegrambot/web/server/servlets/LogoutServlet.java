package ru.kvaga.telegrambot.web.server.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class LogoutServlet
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogoutServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		Cookie[] cookies = request.getCookies();
		if(cookies!=null) {
			for(Cookie cookie : cookies) {
				if(cookie.getName().equals("JSESSIONID")) {
					cookie.setMaxAge(0);
					response.addCookie(cookie);
				}
			}
			//invalidate the session if exists
			HttpSession session = request.getSession(false);
			if(session!=null) {
				session.invalidate();
			}
			//no encoding because we have invalidated the session
			response.sendRedirect("Login.html");
		}
	}

}
