package ru.kvaga.telegrambot.web.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.web.WebLoggerContextUtils;

import telegrambot.ConfigMap;



/**
 * Servlet implementation class LoginServlet
 */
@WebServlet(
		description = "Login Servlet", 
		urlPatterns = { "/LoginServlet" }, 
		initParams = {
				@WebInitParam(name = "q", value = "qq"), 
				@WebInitParam(name = "qqq", value = "qqqq") 
		}
)
public class LoginServlet extends HttpServlet {
	 final static Logger log = LogManager.getLogger(LoginServlet.class);
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	     log.info("--------------------------->   Hello, servlet!");
//	     if(	
//				    getServletContext().getInitParameter("dbURL").equals("jdbc:mysql://localhost/mysql_db")
//				&& 	getServletContext().getInitParameter("dbUser").equals("mysql_user")
//				&&  getServletContext().getInitParameter("dbUserPwd").equals("mysql_some_pwd")
//				) {
//			getServletContext().setAttribute("DB_SUCCESS", "True");
//			log.info("Servlet initialization was finished. DB_SUCCESS attribute was set to True");
//		}else {
//			throw new ServletException("DB Connection Error");
//		}

//		log("getServletContext().getInitParameter(\"dbURL\"):" + getServletContext());
		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String user = request.getParameter("user");
		String pwd = request.getParameter("pwd");
		
		String userID = ConfigMap.adminLogin;
		String password = ConfigMap.adminPassword;
		log.debug("Received credentials: User="+user+"::password=");
		
		if(userID.equals(user) && password.equals(pwd)) {
			HttpSession session = request.getSession();
			session.setAttribute("user", "Alex");
			session.setAttribute("login", user);
			session.setMaxInactiveInterval(30*60);
			
			Cookie userName = new Cookie("user", user);
			response.addCookie(userName);
			log.debug("Login success for user [" + user + "]");
			response.sendRedirect("Main.jsp");
		}else {
			log.error("Either user name or password is wrong.");
			RequestDispatcher rd = getServletContext().getRequestDispatcher("/Login.html");
			PrintWriter out = response.getWriter();
			out.print("<font color=red>Either user name or password is wrong.</font>");
			rd.include(request, response);
		}
//		doGet(request, response);
	}

}
