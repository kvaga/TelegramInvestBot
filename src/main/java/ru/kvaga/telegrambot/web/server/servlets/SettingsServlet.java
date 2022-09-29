package ru.kvaga.telegrambot.web.server.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TreeSet;

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
		String format = request.getParameter("format");
		String source = request.getParameter("source");
		String redirectTo = request.getParameter("redirectTo");
		String workingDayMonday 	= request.getParameter("Monday");
		String workingDayTuesday 	= request.getParameter("Tuesday");
		String workingDayWednesday 	= request.getParameter("Wednesday");
		String workingDayThursday 	= request.getParameter("Thursday");
		String workingDayFriday 	= request.getParameter("Friday");
		String workingDaySaturday 	= request.getParameter("Saturday");
		String workingDaySunday 	= request.getParameter("Sunday");
		String hoursFrom			= request.getParameter("hoursFrom");
		String hoursTo				= request.getParameter("hoursTo");
		String minsFrom				= request.getParameter("minsFrom");
		String minsTo				= request.getParameter("minsTo");

		String telegramNotificationsTopCountForSending = request.getParameter("telegramNotificationsTopCountForSending");
		RequestDispatcher rd = redirectTo !=null? request.getRequestDispatcher(redirectTo) : (source!=null ? request.getRequestDispatcher(source) : request.getRequestDispatcher("/LoginSuccess.jsp"));

		try {
			log.info(ServerUtils.listOfParametersToString(
					"command", command, 
					"format", format,
					"source",source,
					"redirectTo", redirectTo,
					"telegramNotificationsTopCountForSending", telegramNotificationsTopCountForSending,
					"workingDayMonday",workingDayMonday,
					"workingDayTuesday", workingDayTuesday,
					"workingDayWednesday",workingDayWednesday,
					"workingDayThursday",workingDayThursday,
					"workingDayFriday", workingDayFriday,
					"workingDaySaturday", workingDaySaturday,
					"workingDaySunday", workingDaySunday
					));
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			Settings settings = null;
			if(command.toLowerCase().equals("getsettings")) {
				settings = getSettings();
				log.info("Settings loaded");
			}else if(command.toLowerCase().equals("savesettings")) {
				settings = Settings.load();
				settings.setTelegramNotificationsTopCountForSending(Integer.parseInt(telegramNotificationsTopCountForSending));
				settings.updateWorkingDay(new WorkingDay(Calendar.MONDAY, 		workingDayMonday!=null 		&& workingDayMonday.equals("on") ? true : false));
				settings.updateWorkingDay(new WorkingDay(Calendar.TUESDAY, 		workingDayTuesday!=null 	&& workingDayTuesday.equals("on") ? true : false));
				settings.updateWorkingDay(new WorkingDay(Calendar.WEDNESDAY, 	workingDayWednesday!=null 	&& workingDayWednesday.equals("on") ? true : false));
				settings.updateWorkingDay(new WorkingDay(Calendar.THURSDAY, 	workingDayThursday!=null 	&& workingDayThursday.equals("on") ? true : false));
				settings.updateWorkingDay(new WorkingDay(Calendar.FRIDAY, 		workingDayFriday!=null 		&& workingDayFriday.equals("on") ? true : false));
				settings.updateWorkingDay(new WorkingDay(Calendar.SATURDAY, 	workingDaySaturday!=null 	&& workingDaySaturday.equals("on") ? true : false));
				settings.updateWorkingDay(new WorkingDay(Calendar.SUNDAY,		workingDaySunday!=null 		&& workingDaySunday.equals("on") ? true: false));
				settings.setWorkingHours(new WorkingHour(Integer.parseInt(hoursFrom), Integer.parseInt(hoursTo), Integer.parseInt(minsFrom), Integer.parseInt(minsTo)));
				settings.save();
				//response.getWriter().write(OBJECT_MAPPER.writeValueAsString("Settings successfully saved"));
				// =J4 * K4 * IFS(H4="usd";'Курсы'!$B$1;1;H4="hkd";'Курсы'!$B$2;1;H4="cny";'Курсы'!$B$3;1;H4="hkd2";'Курсы'!$B$4;1)+ L4

				//response.sendRedirect(request.getHeader("referer"));
				log.info("Settings successfully saved");
			}else {
				throw new Exception("Unknown command parameter ["+command+"]");
			}
			if(format!=null && format.toLowerCase().equals("json")) {
				response.getWriter().write(OBJECT_MAPPER.writeValueAsString(settings));
			}else {
				request.setAttribute("resp", settings);
			}

		}catch (Exception e) {
			log.error("Exception", e);
//			response.getWriter().write(OBJECT_MAPPER.writeValueAsString(e));
			request.setAttribute("exception", e);
		}finally {
			rd.forward(request, response);
		}
	}
	
	public static Settings getSettings() throws JAXBException, IOException {
		return Settings.load();
	}
}
