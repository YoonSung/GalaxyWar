package threads;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import db.ConnectionPool;

public class WebThread extends Thread {

	private final static int SERVER_PORT = 3000;
	private final ConnectionPool globalConnectionPool;
	private final Map<Integer, ConnectionPool> shardConnectionPools;
	private final Map<Integer, Integer> galaxyIDToDBID;
	
	public WebThread(ConnectionPool globalConnectionPool, Map<Integer, ConnectionPool> shardConnectionPools) {
		this.globalConnectionPool = globalConnectionPool;
		this.shardConnectionPools = shardConnectionPools;
		this.galaxyIDToDBID = getGalaxyIDMap();
	}

	@Override
	public void run() {
		ServerSocket socket;
		boolean isRefreshRequest = false;
		
		System.out.println("Webserver starting up on port 80");
		System.out.println("(press ctrl-c to exit)");
		try {
			// create the main server socket
			socket = new ServerSocket(SERVER_PORT);
		} catch (Exception e) {
			System.out.println("Error: " + e);
			return;
		}

		System.out.println("Waiting for connection");
		while (!WebThread.interrupted()) {
			try {
				// wait for a connection
				Socket remote = socket.accept();
				// remote is now the connected socket
				System.out.println("Connection, sending data.");
				BufferedReader in = new BufferedReader(new InputStreamReader(
						remote.getInputStream()));
				PrintWriter out = new PrintWriter(remote.getOutputStream());

				// read the data sent. We basically ignore it,
				// stop reading once a blank line is hit. This
				// blank line signals the end of the client HTTP
				// headers.
				String str = ".";
				while (!str.equals("")) {
					str = in.readLine();
					if (str.contains("/refresh")) {
						isRefreshRequest = true;
					} else if (str.contains("/restart")) {
						
					}
				}

				// Send the response
				// Send the headers
				out.println("HTTP/1.0 200 OK");
				if (isRefreshRequest) {
					out.println("Content-Type: text/json");
				} else {
					out.println("Content-Type: text/html");
				}
				// this blank line signals the end of the headers
				out.println("");
				// Send the HTML page

				String result;
				if (isRefreshRequest) {
					result = makeRefreshJson();
				} else {
					result = makeHtmlString();
				}

				out.println(result);
				out.flush();
				remote.close();
			} catch (Exception e) {
				System.out.println("Error: " + e);
			}
		}

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String makeRefreshJson() {
		Map<Integer, Integer> galaxyHpData = null;
		try {
			galaxyHpData = getGalaxyHpData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Gson gson = new Gson(); 
		String json = gson.toJson(galaxyHpData);
		
		return json;
	}

	private Map<Integer, Integer> getGalaxyHpData() throws SQLException {
		Map<Integer, Integer> galaxyHpData = new HashMap<Integer, Integer>();
		for (int galaxyId : galaxyIDToDBID.keySet()) {
			int dbid = galaxyIDToDBID.get(galaxyId);
			String sql = "select HP from galaxy where GID = ?";
			Connection connection = shardConnectionPools.get(dbid).getConnection();
			PreparedStatement pstmt = connection.prepareStatement(sql);
			pstmt.setInt(1, galaxyId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				int hp = rs.getInt(1);
				if (hp < 0) 
					hp = 0;
				galaxyHpData.put(galaxyId, hp);
			}
			rs.close();
			pstmt.close();
			connection.close();
		}
		return galaxyHpData;
	}
	
	private Map<Integer, Integer> getGalaxyIDMap() {
		Map<Integer, Integer> galaxyIDToDBID = new HashMap<Integer, Integer>();
		String sql = "select * from galaxy2db";
		Connection globalConnection;
		try {
			globalConnection = globalConnectionPool.getConnection();
			PreparedStatement pstmt = globalConnection.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int gid = rs.getInt("GID");
				int dbid = rs.getInt("DBID");
				galaxyIDToDBID.put(gid, dbid);
			}
			pstmt.close();
			globalConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return galaxyIDToDBID;
	}

	private String makeHtmlString() throws FileNotFoundException, IOException {
		BufferedReader freader = new BufferedReader(new FileReader(
				"./StarWars/webapp/result.html"));
		String buffer;
		StringBuilder sb = new StringBuilder();
		while ((buffer = freader.readLine()) != null) {
			sb.append(buffer);
		}
		freader.close();
		
		String result = sb.toString();
		
		Map<Integer, Integer> galaxyHpData = null;
		try {
			galaxyHpData = getGalaxyHpData();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		for (int i=1; i<=4; i++) {
			int hp = galaxyHpData.get(i);
			result = result.replace("$HP"+i, String.valueOf(hp));
			result = result.replace("$HPP"+i, String.valueOf(hp/1000));
		}
		return result;
	}
}
