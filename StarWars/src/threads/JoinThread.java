package threads;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import core.ConnectionPool;


public class JoinThread extends Thread{

	private final ConnectionPool globalConnectionPool;
	private final Map<Integer, ConnectionPool> shardConnectionPools;
	private final int MAX_REGISTER_NUMBER = 100000;
	
	public JoinThread(ConnectionPool globalConnectionPool, Map<Integer, ConnectionPool> shardConnectionPools) {
		this.globalConnectionPool = globalConnectionPool;
		this.shardConnectionPools = shardConnectionPools;
		
	}

	private void register() throws ClassNotFoundException, SQLException {
		
		//regist master
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = globalConnectionPool.getConnection();
		
		String sql = "{CALL ADDUSER(?, ?, ?)}";
		CallableStatement callableStatement = connection.prepareCall(sql);

		callableStatement.registerOutParameter(1, Types.INTEGER);
		callableStatement.registerOutParameter(2, Types.INTEGER);
		callableStatement.registerOutParameter(3, Types.TINYINT);
		callableStatement.execute();
		
		int userId = (int) callableStatement.getObject(1);
		int databaseId = (int) callableStatement.getObject(2);
		int galaxyId = (int) callableStatement.getObject(3);
		
		System.out.println(userId);
		System.out.println(databaseId);
		System.out.println(galaxyId);
		
		callableStatement.close();
		connection.close();
		
		//regist shard
		registerShard(userId, databaseId, galaxyId);
	}

	private void registerShard(int userId, int databaseId, int galaxyId)
			throws SQLException {

		String sql = "{CALL ADDUSER_SHARD(?, ?)}";
		Connection connection = shardConnectionPools.get(databaseId).getConnection();
		CallableStatement callableStatement = connection.prepareCall(sql);
		
		callableStatement.setInt(1, userId);
		callableStatement.setInt(2, galaxyId);
		callableStatement.execute();
		
		connection.close();
	}
	
	@Override
	public void run() {
		for (int i = 0; i < MAX_REGISTER_NUMBER && !this.isInterrupted(); i++) {
			try {
				register();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
