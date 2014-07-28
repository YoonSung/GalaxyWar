package threads;

import java.sql.Connection;
import java.sql.SQLException;

import db.ConnectionPool;

public class Galaxy {
	
	/* NOT USING NOW */
	ConnectionPool connectionPool;
	int id;
	String name;
	
	public Galaxy(ConnectionPool connectionPool, int gid, String name) {
		this.connectionPool = connectionPool;
		this.id = gid;
		this.name = name;
	}

	public Connection getConnection() throws SQLException {
		return connectionPool.getConnection();
	}
}
