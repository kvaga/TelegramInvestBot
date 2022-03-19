package ru.kvaga.telegrambot.web.server.servlets;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.kvaga.telegrambot.web.util.ServerUtils;


/**
 * Servlet implementation class CompositeFeedsList
 */
@WebServlet("/DeleteInstrument")
public class DeleteInstrumentServlet extends HttpServlet {
	final private static Logger log = LogManager.getLogger(DeleteInstrumentServlet.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteInstrumentServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String redirectTo = request.getParameter("redirectTo");
		String source = request.getParameter("source");
		String type = request.getParameter("type");
		String ticker = request.getParameter("ticker");

		log.debug("Got parameters "+ServerUtils.listOfParametersToString("redirectTo", redirectTo, "source", source, "type", type, "ticker", ticker));
		
		try {
			//response.setContentType("application/json");
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8"); 
			//response.getWriter().write(OBJECT_MAPPER.writeValueAsString(deleteInstrument(ticker, type)/*.toArray()*/));
			request.getSession().setAttribute("status", deleteInstrument(ticker, type));

			RequestDispatcher rd = redirectTo !=null? getServletContext().getRequestDispatcher(redirectTo) : (source!=null ? getServletContext().getRequestDispatcher(source) : getServletContext().getRequestDispatcher("/"));
			rd.forward(request, response);
		}catch (Exception e) {
			log.error("Exception on DeleteInstrumentServlet", e);
			request.setAttribute("Exception", e);
		}	

	}

	private String deleteInstrument(String ticker, String type) {
		StringBuilder sb = new StringBuilder();
		sb.append("Status of deletion "+type+" ["+ticker+"]: ");
		if(type.toLowerCase().equals("stock")) {
			sb.append(ServerUtils.getStockFileByName(ticker).delete());
		}else if(type.toLowerCase().equals("bond")) {
			sb.append(ServerUtils.getBondFileByName(ticker).delete());
		}else if(type.toLowerCase().equals("etf")) {
			sb.append(ServerUtils.getEtfFileByName(ticker).delete());
		}else {
			sb.append("corresponding file not found");
		}
		return sb.toString();
	}
	
}
