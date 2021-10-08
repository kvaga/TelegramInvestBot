package ru.kvaga.telegrambot.web.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servlet Filter implementation class AuthenticationFilter
 */
@WebFilter("/AuthenticationFilter")
public class AuthenticationFilter implements Filter {
	private static Logger log = LogManager.getLogger(AuthenticationFilter.class);
	private ServletContext context;
    /**
     * Default constructor. 
     */
    public AuthenticationFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String uri = req.getRequestURI();
		context.log("Requested Resource:" + uri);
		log.debug("Requested Resource:" + uri);
		HttpSession session = req.getSession(false);
		if(session==null && !(uri.endsWith("ProxySite") || uri.endsWith("html") || uri.endsWith("LoginServlet")|| uri.endsWith("showFeed"))) {
			context.log("Unauthorized access request");
			res.sendRedirect("Login.html");
		}else {
			chain.doFilter(request, response);
		}
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		context=fConfig.getServletContext();
		context.log("AuthenticationFilter initialized");
	}

}
