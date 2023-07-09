package gobbi.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
import gobbi.project.beans.Meeting;
import gobbi.project.beans.User;
import gobbi.project.dao.MeetingDAO;


@WebServlet("/CreateMeeting")
public class CreateMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;
	
    public CreateMeeting() {
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
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//If the user is not logged in, redirect to the login page
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			String loginpath = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(loginpath);
			return;
		}
		
		//Retrieve the array of selectedUsers from the request and check it's length 
		String[] selectedUsersArray = request.getParameterValues("selectedUsers");
		if(selectedUsersArray.length>=((Meeting)session.getAttribute("meeting")).getMax()) {
			//If it's more than the max amount of invitations, increment tries
			int tries = (int)session.getAttribute("tries");
			tries++;
			if(tries>3) {
				//If more than 3 tries have happened, clear the session attributes and redirect to Cancellation
				session.removeAttribute("meeting");
				session.removeAttribute("tries");
				session.removeAttribute("selectedUsersArray");
				session.removeAttribute("exceedingValue");
				String path = "/WEB-INF/Cancellation.html";
				final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
				templateEngine.process(path, ctx, response.getWriter());
			}
			else {
				//If the user can try again, put the array of selected users in the session (so they can be preselected) and redirect to GoToAnagraphic
				session.setAttribute("tries", tries);
				session.setAttribute("selectedUsersArray", selectedUsersArray);
				String path = getServletContext().getContextPath() + "/GoToAnagraphic";
				response.sendRedirect(path);
			}
		}
		else {
			try {
				//If the number of invited users is below the maximum, create the meeting in the database and redirect to Home
				MeetingDAO meetingDAO = new MeetingDAO(connection);
				meetingDAO.createNewMeeting((Meeting)session.getAttribute("meeting"), ((User)session.getAttribute("user")).getId(), selectedUsersArray);
				session.setAttribute("meetingCreationMsg", "Meeting succesfully created");
				String path = getServletContext().getContextPath() + "/Home";
				response.sendRedirect(path);
				
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create meeting");
				return;
			}
		}
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
