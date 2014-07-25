package core;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
		
		Map<Integer, Galaxy> galaxies = null;
		
		try {
			galaxies = getGalaxies();
			System.out.println("galaxies : "+galaxies);
			Scanner scanner = new Scanner(System.in);
			JoinThread.setGalaxies(galaxies);
			User.setGalaxies(galaxies);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//회원가입
		JoinThread joinThread = new JoinThread();
		joinThread.start();
		
		threads.add(joinThread);
		
		//공격
		for (int i = 0; i < ATTACK_THREAD_NUM; i++) {
			User user = new User();
			threads.add(user);
			user.start();
		}
		
	}
	
	private static Map<Integer, Galaxy> getGalaxies() throws SQLException, ClassNotFoundException {
		
		Map<Integer, Galaxy> galaxies = new HashMap<Integer, Galaxy>();
		
		for (String ip : IP_LIST) {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection;
			String user = "jedi";
			String password = "jedi";
			String url = "jdbc:mysql://" + ip + "/yoda?noAccessToProcedureBodies=true";
			
			String sql = "SELECT * from galaxy";
			
			//connection = DriverManager.getConnection(url, user, password);
			ConnectionPool connectionPool = new ConnectionPool(user, password, url);
			
			connection = connectionPool.getConnection();
			PreparedStatement psmt = connection.prepareStatement(sql);
			ResultSet rs = psmt.executeQuery();
			
			int galaxyId = rs.getInt("GID");
			
			while (rs.next()) {
				galaxies.put( galaxyId, new Galaxy(connectionPool, galaxyId, rs.getString("NAME")));
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
