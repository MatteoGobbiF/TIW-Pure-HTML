package gobbi.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

import gobbi.project.beans.Meeting;


public class MeetingDAO {
	
	private Connection connection;

	public MeetingDAO(Connection connection) {
		this.connection = connection;
	}

	//Create a new meeting in the database, and call the invitationDAO to also create the invitations
	public void createNewMeeting (Meeting meeting, int creatorID, String[] selectedUsers) throws SQLException {
		
		InvitationDAO invitationDAO = new InvitationDAO(connection);
		connection.setAutoCommit(false); //So it don't create the mission if the invitations creation go wrong
		String query = "INSERT INTO meeting (title, duration, dateTime, max, creator) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement=connection.prepareStatement(query);){
			pstatement.setString(1,  meeting.getTitle());
			pstatement.setInt(2, meeting.getDuration());
			pstatement.setString(3, meeting.getDateTime());
			pstatement.setInt(4, meeting.getMax());
			pstatement.setInt(5,  creatorID);
			pstatement.executeUpdate();
			Statement idStatement = connection.createStatement();
			ResultSet result = idStatement.executeQuery("SELECT LAST_INSERT_ID()");
			result.next();
			int meetingId = result.getInt(1); //Get the created meeting id (which is created by auto-increment)
			    		
			for(int i=0;i<selectedUsers.length;i++) //For every person invited, create an invitation
				invitationDAO.addInvitation(Integer.parseInt(selectedUsers[i]), meetingId);
			//If we reach this point without throwing an Exception, the all the queries were successful
			connection.commit(); 
			
		} catch(SQLException e) {
			//If any query threw an exception, roll back the changes
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		} 
	}
	
	//Get the list of meetings created by an user that are in the future
	public List<Meeting> getFutureMeetingsByCreator(int creatorId) throws SQLException {
		
		List<Meeting> meetings = new ArrayList<Meeting>();
		
		String query = "SELECT * from meeting where creator = ? AND dateTime>CURRENT_TIMESTAMP ORDER BY dateTime ASC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, creatorId);
			ResultSet result = pstatement.executeQuery();
			while (result.next()) {
				Meeting meeting = new Meeting();
				meeting.setTitle(result.getString("title"));
				meeting.setDateTime(result.getString("dateTime"));
				meeting.setDuration(result.getInt("duration"));
				meeting.setMax(result.getInt("max"));
				meetings.add(meeting);
			}			
		}
		return meetings;
	}
	
	//Get the list of meeting an user is invited to that are in the future
	public List<Meeting> getFutureInvitationsByUser(int userId) throws SQLException {
		
		List<Meeting> meetings = new ArrayList<Meeting>();
		
		String query = "SELECT title, duration, dateTime, max FROM invitation JOIN meeting ON invitation.meeting_id=meeting.id WHERE user_id = ? AND dateTime>CURRENT_TIMESTAMP ORDER BY dateTime ASC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			ResultSet result = pstatement.executeQuery();
			while (result.next()) {
				Meeting meeting = new Meeting();
				meeting.setTitle(result.getString("title"));
				meeting.setDateTime(result.getString("dateTime"));
				meeting.setDuration(result.getInt("duration"));
				meeting.setMax(result.getInt("max"));
				meetings.add(meeting);
			}			
		}
		return meetings; 		
	}
		
	
}
