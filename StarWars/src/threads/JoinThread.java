package threads;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;


public class JoinThread extends Thread{

	private static Map<Integer, Galaxy> galaxies;
	
	private final String user = "jedi";
	private final String password = "jedi";
	private final String url = "jdbc:mysql://10.73.43.201/yoda?noAccessToProcedureBodies=true";
	private final int MAX_REGISTER_NUMBER = 100000;
	
	private void register() throws ClassNotFoundException, SQLException {
		
		//regist master
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
		//TODO databaseId에 대한 삭제필요
		int databaseId = (int) callableStatement.getObject(2);
		int galaxyId = (int) callableStatement.getObject(3);
		
		System.out.println(userId);
		System.out.println(databaseId);
		System.out.println(galaxyId);
		
		callableStatement.close();
		connection.close();
		
		//regist shard
		registerShard(userId, galaxyId);
	}

	private void registerShard(int userId, int galaxyId)
			throws SQLException {
		System.out.println("galaxyId : "+galaxyId);
		System.out.println("getGalaxy : " + galaxies.size());
		System.out.println("getGalaxy : " + galaxies.get(galaxyId));
		
		Connection connection = galaxies.get(galaxyId).getConnection();
		String sql = "{CALL ADDUSER_SHARD(?, ?)}";
		CallableStatement callableStatement;
		callableStatement = connection.prepareCall(sql);
		
		callableStatement.setInt(1, userId);
		callableStatement.setInt(2, galaxyId);
		
		callableStatement.execute();
		
		System.out.println("updateCount : "+callableStatement.getUpdateCount());
		
		connection.close();
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

	public static void setGalaxies(Map<Integer, Galaxy> galaxies) {
		JoinThread.galaxies = galaxies; 
	}
}
