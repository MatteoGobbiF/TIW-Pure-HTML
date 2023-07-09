package gobbi.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InvitationDAO {
	
	private Connection connection;

	public InvitationDAO(Connection connection) {
		this.connection = connection;
	}

	//Create a new invitation in the database
	public void addInvitation(int userId, int meetingId) throws SQLException {
		
		String query = "INSERT INTO invitation (user_id, meeting_id) VALUES (?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			pstatement.setInt(2, meetingId);
			pstatement.executeUpdate();
		}
	}
}