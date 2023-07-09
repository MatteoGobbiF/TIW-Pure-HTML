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

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gobbi.project.dao.UserDAO;
import gobbi.project.utils.ConnectionHandler;
import gobbi.project.exceptions.*;


@WebServlet("/Registration")
public class Registration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
    public Registration() {
        super();
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String username = null;
		String name = null;
		String surname=null;
		String email=null;
		String password=null;
		String re_password=null;
		
		try {
			//Get data from the form
			username=StringEscapeUtils.escapeJava(request.getParameter("username"));
			name=StringEscapeUtils.escapeJava(request.getParameter("name"));
			surname=StringEscapeUtils.escapeJava(request.getParameter("surname"));
			email=StringEscapeUtils.escapeJava(request.getParameter("email"));
			password = StringEscapeUtils.escapeJava(request.getParameter("password"));
			re_password=StringEscapeUtils.escapeJava(request.getParameter("re_password"));
			
			//Check validity
			if (username == null || name == null || surname == null || email == null || password == null
				|| re_password == null || username.isEmpty() || name.isEmpty() || surname.isEmpty()
				|| email.isEmpty() || password.isEmpty() || re_password.isEmpty()) 
				throw new BadParametersException("Missing or empty credential value");
		} catch (BadParametersException e) {
			//In case something is missing send error
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credential value");
			return;
		}
			
		UserDAO userDao = new UserDAO(connection);
		
		//Store the variables in a context in order to fill the form again if something is wrong
		ServletContext servletContext = getServletContext();
		final WebContext ctx_err = new WebContext(request, response, servletContext, request.getLocale());
		ctx_err.setVariable("savedUsername", username);
		ctx_err.setVariable("savedName", name);
		ctx_err.setVariable("savedSurname", surname);
		ctx_err.setVariable("savedEmail", email);
			
		//Check if the two passwords match, the email syntax and if the username and the email aren't already taken 
		try {
			
			if(!password.equals(re_password))
				throw new PasswordsNotMatchingException("The two passwords are different");
			String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(email);
			if(!matcher.matches())
				throw new InvalidEmailSyntaxException("Invalid email");
			if(userDao.isUsernameTaken(username))
				throw new UsernameTakenException("Username taken");
			if(userDao.isEmailTaken(email))
				throw new EmailTakenException("Email taken");
			
			//If something is wrong, throw an exception, otherwise register the new user in the database
			
			userDao.registerNewUser(username, name, surname, email, password);
			
			//If everything goes well prepare a message that confirms the registration has been successful
			final WebContext ctx_succ = new WebContext(request, response, servletContext, request.getLocale());
			processTemplate("registrationSuccessMsg", "Registration successful", request, response, ctx_succ);
			
			//If an Exception is thrown, send the correct error message to the client
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed");
			return;
		} catch (PasswordsNotMatchingException e) {
			processTemplate("registrationErrorMsg", "The two password don't match", request, response, ctx_err);
		} catch (InvalidEmailSyntaxException e) {
			processTemplate("registrationErrorMsg", "Invalid email", request, response, ctx_err);
		} catch (UsernameTakenException e) {
			processTemplate("registrationErrorMsg", "Username taken", request, response, ctx_err);
		} catch (EmailTakenException e) {
			processTemplate("registrationErrorMsg", "Email taken", request, response, ctx_err);
		}		
		
    }
    
    //Since I used the same 4 lines of code in every catch, I made a function
    private void processTemplate(String name, Object value, HttpServletRequest request, HttpServletResponse response, WebContext ctx) 
    throws IOException, ServletException{
    	String path;
		ctx.setVariable(name, value);
		path = "/index.html";
		templateEngine.process(path, ctx, response.getWriter());
    }
    
    public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
