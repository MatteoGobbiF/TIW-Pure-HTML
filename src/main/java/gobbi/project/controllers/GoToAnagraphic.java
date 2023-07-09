package gobbi.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import gobbi.project.utils.ConnectionHandler;
import gobbi.project.beans.*;
import gobbi.project.dao.UserDAO;
import gobbi.project.exceptions.BadParametersException;
import gobbi.project.exceptions.InvalidDateTimeException;

@WebServlet("/GoToAnagraphic")
public class GoToAnagraphic extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
	
    public GoToAnagraphic() {
        super();
    }

    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in redirect to the login
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		
		//If tries isn't null it isn't the first try, so calculate how many users need to be unselected
		if(session.getAttribute("tries")!=null) {			
			String[] selectedUsersArray = (String[])session.getAttribute("selectedUsersArray");
			int exceedingValue=selectedUsersArray.length+1-((Meeting)session.getAttribute("meeting")).getMax();
			session.setAttribute("exceedingValue", exceedingValue);
		}
		else { //If it is the first try, get the meeting data from the form and check the validity
		
			String title=null;
			String dateTime=null;
			Integer duration=null;
			Integer max=null;
			
			try {
				title = request.getParameter("title");
			    dateTime = request.getParameter("dateTime");
			    duration = Integer.parseInt(request.getParameter("duration"));
			    max = Integer.parseInt(request.getParameter("max"));
	
			    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
			    LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
			    if (localDateTime.isBefore(LocalDateTime.now())) {
			        throw new InvalidDateTimeException("DateTime is in the past");
			    }
				if(title==null || title.isEmpty() || duration<=0 || duration>1440 || max<1)
					throw new BadParametersException("Missing or wrong value");
			} catch (DateTimeException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad date");
				return;
			} catch (NumberFormatException | NullPointerException | BadParametersException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or wrong credential value");
				return;
			} catch (InvalidDateTimeException e) {
				//If the selected date is in the past, save in the session the error message and redirect to home
				session.setAttribute("errorMsg", "Impossible to create a meeting in the past");
				response.sendRedirect(getServletContext().getContextPath() + "/Home");
			}
			
			//If no data exception save the meeting in the session and set tries to 1
			Meeting meeting = new Meeting(title, dateTime, duration, max);
			session.setAttribute("meeting", meeting);
			session.setAttribute("tries", 1);
		}
		
		UserDAO userDAO = new UserDAO(connection);
		List<User> users = new ArrayList<User>();
		
		//In any case, get the users list from the database and set it as a ctx attribute
		try {
			users = userDAO.listAllUsersButOne(((User)session.getAttribute("user")).getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover missions");
			return;
		}		
		
		String path = "/WEB-INF/Anagraphic.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("users", users);
		templateEngine.process(path, ctx, response.getWriter());
    }
    
    //doGet is called by CreateMeeting (tries is >1 in this case so the request.getParameter block is skipped
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

