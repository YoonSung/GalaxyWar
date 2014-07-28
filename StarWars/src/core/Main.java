package core;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import threads.AttackThread;
import threads.JoinThread;
import threads.WebThread;
import db.ConnectionPool;


public class Main {
	
	private static final int ATTACK_THREAD_NUM = 4; 
	private static ArrayList<Thread> threads = new ArrayList<Thread>();
	private static final String GLOBAL_DB_IP = "localhost";
	
	private static ConnectionPool globalConnectionPool = null;
	private static Connection globalConnection = null;
	private static Map<Integer, ConnectionPool> shardConnectionPools = null;
	
	public static void main(String[] args) {
		
		
		try {
			globalConnectionPool = makeConnectionPool(GLOBAL_DB_IP);
			globalConnection = globalConnectionPool.getConnection();
			shardConnectionPools = getShardConnectionPoolList(globalConnection);
			globalConnection.close();
			Thread.sleep(1000);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Web Thread
		WebThread webThread = new WebThread(globalConnectionPool, shardConnectionPools);
		webThread.start();
		
		startGame();
	}
	
	public static void startGame() {
		threads = new ArrayList<Thread>();
		
		// 회원가입
		JoinThread joinThread = new JoinThread(globalConnectionPool, shardConnectionPools);
		threads.add(joinThread);
		
		// 공격
		for (int i = 0; i < ATTACK_THREAD_NUM; i++) {
			AttackThread attackThread = new AttackThread(globalConnectionPool, shardConnectionPools);
			threads.add(attackThread);
		}

		for (Thread thread : threads) {
			thread.start();
		}
	}
	
	public static void gameOver() {
		for (Thread thread : threads) {
			if (!thread.isAlive())
				continue;
			thread.interrupt();
		}
	}
	
	private static Map<Integer, ConnectionPool> getShardConnectionPoolList(
			Connection globalConnection) throws SQLException, ClassNotFoundException {
		Map<Integer, ConnectionPool> shardConnectionPoolList = new HashMap<Integer, ConnectionPool>();
		String sql = "SELECT * FROM db";
		PreparedStatement psmt = globalConnection.prepareStatement(sql);
		ResultSet rs = psmt.executeQuery();
		while (rs.next()) {
			int dbID = rs.getInt("DBID");
			String IP = rs.getString("IP");
			System.out.println(dbID);
			System.out.println(IP);
			ConnectionPool shardConnectionPool = makeConnectionPool(IP);
			shardConnectionPoolList.put(dbID, shardConnectionPool);
		}
		return shardConnectionPoolList;
	}

	private static ConnectionPool makeConnectionPool(String ip) throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		String user = "jedi";
		String password = "jedi";
		String url = "jdbc:mysql://" + ip + "/yoda?noAccessToProcedureBodies=true";
		ConnectionPool connectionPool = new ConnectionPool(user, password, url);
		System.out.println(connectionPool);
		return connectionPool;
	}

}
