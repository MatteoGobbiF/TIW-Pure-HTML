package gobbi.project.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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

import gobbi.project.beans.*;
import gobbi.project.dao.MeetingDAO;
import gobbi.project.utils.ConnectionHandler;

@WebServlet("/Home")
public class GoToHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;
	
    public GoToHome() {
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
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		//If the user is not logged in, redirect to index
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		User user = (User) session.getAttribute("user");
		
		//Reset the number of tries and selectedUsers in case the User is coming here from anagraphic or cancellation
		if(session.getAttribute("tries")!=null)
			session.removeAttribute("tries");
		if(session.getAttribute("selectedUsersArray")!=null)
			session.removeAttribute("selectedUsersArray");

		//Retrieve the user's next meetings from the database
		List<Meeting> createdMeetings = new ArrayList<Meeting>();
		List<Meeting> invitedMeetings = new ArrayList<Meeting>();
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		try {
			createdMeetings=meetingDAO.getFutureMeetingsByCreator(user.getId());
			invitedMeetings=meetingDAO.getFutureInvitationsByUser(user.getId());
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover meetings");
			return;
		}
		
		//Get and format the datetime to set the minimum datetime of the "new meeting" form
		LocalDateTime now = LocalDateTime.now();
		String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
		
		String path = "/WEB-INF/Home.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		//Check for error or confirmation messages in the session in order to display them
		//If they are present, remove them from the session so that if the user refresh the page they disappear
		String errorMsg = (String) session.getAttribute("errorMsg");
		String creationMsg=(String) session.getAttribute("meetingCreationMsg");
		if(errorMsg!=null) {
			ctx.setVariable("formError", errorMsg);
			session.removeAttribute("errorMsg");
		}
		if(creationMsg!=null) {
			ctx.setVariable("meetingCreationMsg", creationMsg);
			session.removeAttribute("meetingCreationMsg");
		}
		//Set variables in the context so they can be displayed
		ctx.setVariable("name", user.getName());
		ctx.setVariable("surname", user.getSurname());
		ctx.setVariable("minDateTime", formattedDateTime);
		ctx.setVariable("createdMeetings", createdMeetings);
		ctx.setVariable("invitedMeetings", invitedMeetings);
		templateEngine.process(path, ctx, response.getWriter());
	}	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
    
