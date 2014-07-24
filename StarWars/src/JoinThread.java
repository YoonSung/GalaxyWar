import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;


public class JoinThread extends Thread{
	
	private final String user = "yoonsung";
	private final String password = "yoondb";
	private final String url = "jdbc:mysql://10.73.45.65/yoda?noAccessToProcedureBodies=true";
	private final int MAX_REGISTER_NUMBER = 100000;
	
	private void register() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection;
		connection = DriverManager.getConnection(url, user, password);
		
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
		
		sql = "{CALL ADDUSER_SHARD(?, ?)}";
		callableStatement = connection.prepareCall(sql);
		
		callableStatement.setInt(1, userId);
		callableStatement.setInt(2, galaxyId);
		
		callableStatement.execute();
		
		System.out.println("updateCount : "+callableStatement.getUpdateCount());
		
		//System.out.println("ADDUER_SHARD RESULT: "+affectedRowCount);
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
