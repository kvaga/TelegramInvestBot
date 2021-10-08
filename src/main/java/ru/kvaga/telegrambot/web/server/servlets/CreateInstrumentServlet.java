package ru.kvaga.telegrambot.web.server.servlets;


import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.kvaga.investments.stocks.StockItem;
import ru.kvaga.telegrambot.web.util.ServerUtils;

/**
 * Servlet implementation class CreateInstrumentServlet
 */
@WebServlet("/CreateInstrument")
public class CreateInstrumentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	 final static Logger log = LogManager.getLogger(CreateInstrumentServlet.class);

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateInstrumentServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String instrumentType = request.getParameter("instrumentType");
		String instrumentName = request.getParameter("instrumentName");
		log.debug("Got parameters instrumentName ["+instrumentName+"], instrumentType ["+instrumentType+"]");
		if(!ServerUtils.parameterExists("instrumentType", request, response)) return;
		if(!ServerUtils.parameterExists("instrumentName", request, response)) return;
		createInstrument(instrumentName, instrumentType);
	}

	private void createInstrument(String instrumentName, String instrumentType) {
		if(instrumentType.equals("stock")) {
			//StockItem item = new S
		}
	}
	
	

}
