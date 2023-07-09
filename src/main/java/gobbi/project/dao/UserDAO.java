package gobbi.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gobbi.project.beans.User;
 

public class UserDAO {
	
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	//Check if a user with such username and password exists, if so return it
	public User checkCredentials(String username, String password) throws SQLException {
		
		String query = "SELECT id, username, name, surname, email FROM user  WHERE username = ? AND password =?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst())
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					user.setEmail(result.getString("email"));
					return user;
				}
			}
		}		
	}
	
	//Check if there is already an user with the desired username
	public boolean isUsernameTaken(String username) throws SQLException {
		String query = "SELECT username FROM user WHERE username = ?";
		try (PreparedStatement pstatement=connection.prepareStatement(query);){
			pstatement.setString(1,  username);
			try (ResultSet result = pstatement.executeQuery();){
				if(result.isBeforeFirst())
					return true;
				else
					return false;
			}
		}
	}
	
	//Check if there is already an user with such email
	public boolean isEmailTaken(String email) throws SQLException {
		String query = "SELECT username FROM user WHERE email = ?";
		try (PreparedStatement pstatement=connection.prepareStatement(query);){
			pstatement.setString(1,  email);
			try (ResultSet result = pstatement.executeQuery();){
				if(result.isBeforeFirst())
					return true;
				else
					return false;
			}
		}
	}
	
	//Create the user in the database
	public void registerNewUser(String username, String name, String surname, String email, String password) throws SQLException {
		String query = "INSERT INTO user (username, email, password, name, surname) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement=connection.prepareStatement(query);){
			pstatement.setString(1,  username);
			pstatement.setString(2, email);
			pstatement.setString(3, password);
			pstatement.setString(4, name);
			pstatement.setString(5,  surname);
			pstatement.executeUpdate();
		}
	}
	
	//Get a list of all the users but the one with the specified id
	public List<User> listAllUsersButOne(int id) throws SQLException {
		List<User> users = new ArrayList<User>();
		String query = "SELECT id, username, name, surname FROM user WHERE id!=? ORDER BY username ASC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, id);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					users.add(user);
				}
			}
		}
		return users;
	}
		
}
