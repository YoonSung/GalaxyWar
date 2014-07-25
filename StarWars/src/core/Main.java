package core;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import threads.Galaxy;
import threads.JoinThread;
import threads.SocketThread;
import threads.User;


public class Main {
	
	private static final int ATTACK_THREAD_NUM = 4; 
	private static ArrayList<Thread> threads = new ArrayList<Thread>();
	
	@SuppressWarnings("serial")
	private static ArrayList<String> IP_LIST = new ArrayList<String>(){{
		add("10.73.45.65");
		add("10.73.45.67");
	}};
	
	public static void main(String[] args) {
		
		SocketThread socketThread = new SocketThread();
		socketThread.start();
		
		List<Galaxy> galaxies = null;
		
		try {
			galaxies = getGalaxies();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//회원가입
		JoinThread joinThread = new JoinThread();
		joinThread.start();
		
		threads.add(joinThread);
		
		//공격
		User.setGalaxies(galaxies);
		for (int i = 0; i < ATTACK_THREAD_NUM; i++) {
			User user = new User();
			threads.add(user);
			user.start();
		}
		
	}
	
	private static List<Galaxy> getGalaxies() throws SQLException, ClassNotFoundException {
		
		List<Galaxy> galaxies = new ArrayList<Galaxy>();
		
		for (String ip : IP_LIST) {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection;
			String user = "yoonsung";
			String password = "yoondb";
			String url = "jdbc:mysql://" + ip + "/yoda?noAccessToProcedureBodies=true";
			
			String sql = "SELECT * from db";
			
			//connection = DriverManager.getConnection(url, user, password);
			ConnectionPool connectionPool = new ConnectionPool(user, password, url);
			
			connection = connectionPool.getConnection();
			PreparedStatement psmt = connection.prepareStatement(sql);
			ResultSet rs = psmt.executeQuery();
			
			while (rs.next()) {
				galaxies.add(new Galaxy(connectionPool, rs.getInt("GID"), rs.getString("NAME")));
			}
			
			connection.close();
			psmt.close();
			
		}
		
		return galaxies;
	}
	public static void gameOver() {
		for (Thread thread : threads) {
			if (!thread.isAlive())
				continue;
			thread.interrupt();
		}
	}
}
