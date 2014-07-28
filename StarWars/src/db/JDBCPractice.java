package db;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;


public class JDBCPractice {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection;
		String user = "yoonsung";
		String password = "yoondb";
		String url = "jdbc:mysql://10.73.45.65/yoda?noAccessToProcedureBodies=true";
		
		String sql = "{CALL ADDUSER(?, ?, ?)}";
		
		connection = DriverManager.getConnection(url, user, password);
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
}
