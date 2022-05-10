package ru.kvaga.telegrambot.web.server.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.kvaga.telegrambot.web.util.ServerUtils;
import telegrambot.Settings;

/**
 * Servlet implementation class SettingsServlet
 */
@WebServlet("/Settings")
public class SettingsServlet extends HttpServlet {
	final private static Logger log = LogManager.getLogger(SettingsServlet.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SettingsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String command = request.getParameter("command");
		String telegramNotificationsTopCountForSending = request.getParameter("telegramNotificationsTopCountForSending");
		
		try {
			log.info(ServerUtils.listOfParametersToString("command", command, "telegramNotificationsTopCountForSending", telegramNotificationsTopCountForSending));
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			if(command.toLowerCase().equals("getsettings")) {
				response.getWriter().write(OBJECT_MAPPER.writeValueAsString(getSettings()));
				log.info("Settings loaded");
			}else if(command.toLowerCase().equals("savesettings")) {
				Settings settings = Settings.load();
				settings.setTelegramNotificationsTopCountForSending(Integer.parseInt(telegramNotificationsTopCountForSending));
				settings.save();
				response.getWriter().write(OBJECT_MAPPER.writeValueAsString("Settings successfully saved"));
				response.sendRedirect(request.getHeader("referer"));
				log.info("Settings successfully saved");
			}else {
				throw new Exception("Unknown command parameter ["+command+"]");
			}
		}catch (Exception e) {
			log.error("Exception", e);
			response.getWriter().write(OBJECT_MAPPER.writeValueAsString(e));
		}	 
	}
	
	public static Settings getSettings() throws JAXBException, IOException {
		return Settings.load();
	}
}
