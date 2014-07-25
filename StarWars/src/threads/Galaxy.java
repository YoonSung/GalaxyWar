package threads;

import java.sql.Connection;

import core.ConnectionPool;

public class Galaxy {
	
	ConnectionPool connectionPool;
	int id;
	String name;
	
	public Galaxy(ConnectionPool connectionPool, int gid, String name) {
		this.connectionPool = connectionPool;
		this.id = gid;
		this.name = name;
	}

	public Connection getConnection() {
		return null;
	}
}
