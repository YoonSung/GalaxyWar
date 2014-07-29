package threads;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import core.Main;
import db.ConnectionPool;
import dto.Log;
import dto.User;

public class AttackThread extends Thread {

	private final int ATTACK_NUM = 25000;
	private final ConnectionPool globalConnectionPool;
	private final Map<Integer, ConnectionPool> shardConnectionPools;
	private final Map<Integer, Integer> galaxyIDToDBID;
	
	public AttackThread(ConnectionPool globalConnectionPool,
			Map<Integer, ConnectionPool> shardConnectionPools) {
		this.globalConnectionPool = globalConnectionPool;
		this.shardConnectionPools = shardConnectionPools;
		this.galaxyIDToDBID = getGalaxyIDMap();
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
			rs.close();
			pstmt.close();
			globalConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return galaxyIDToDBID;
	}

	@Override
	public void run() {
		System.out.println("ATTACK THREAD START!");
		for (int i = 0; i < ATTACK_NUM && !this.isInterrupted(); i++) {
			operation();
		}
		
		System.out.println("ATTACK THREAD STOP!");
	}

	private void operation() {
		User attacker = getRandomUser();
		if (attacker == null) {
			System.out.println("NULL PTR ECPT!");
			return;
		}
		
		int attackerDBID = galaxyIDToDBID.get(attacker.getGid());
		int targetGalaxyID = attacker.getRandomTargetGID(1, 4);
		int targetDBID = galaxyIDToDBID.get(targetGalaxyID);
//		System.out.println("targetGalaxy : "+ targetGalaxyID);
		
		String sql = "{CALL GET_ATTACK_POWER(?, ?)}";
		Connection attackConnection = null;
		Connection targetConnection = null;

		try {
			attackConnection = shardConnectionPools.get(attackerDBID).getConnection();
			targetConnection = shardConnectionPools.get(targetDBID).getConnection();
			CallableStatement callableStatement;
			callableStatement = attackConnection.prepareCall(sql);
			callableStatement.setInt(1, attacker.getUid());
			callableStatement.registerOutParameter(2, Types.INTEGER);
			callableStatement.execute();

			int damage = callableStatement.getInt(2);
			//System.out.println(damage);
			int remainHp = attack(attacker.getUid(), damage, targetGalaxyID,
					targetConnection);
			callableStatement.close();

			if (remainHp > 0) {
				sql = "{CALL LOG(?, ?, ?, ?)}";
				callableStatement = attackConnection.prepareCall(sql);
				callableStatement.setInt(1, attacker.getUid());
				callableStatement.setInt(2, targetGalaxyID);
				callableStatement.setInt(3, damage);
				callableStatement.registerOutParameter(4, Types.VARCHAR);
				
				callableStatement.execute();
				
				String log = callableStatement.getString(4);
				Log.getInstance().addList(log);
				callableStatement.close();
			} else {
				System.out.println("GAME OVER!");
				Main.gameOver();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (attackConnection != null)
					attackConnection.close();
				if (targetConnection != null)
					targetConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private int attack(int attackerId, int damage, int targetGalaxyID,
			Connection targetConnection) throws SQLException {
		
		String sql = "{CALL ATTACK(?, ?, ?)}";
		CallableStatement callableStatement = null;
		int remainHp = 0;
		try {
			callableStatement = targetConnection.prepareCall(sql);
			callableStatement.setInt(1, targetGalaxyID);
			callableStatement.setInt(2, damage);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			callableStatement.execute();
			
			remainHp = callableStatement.getInt(3);
		} catch (Exception e) {
			
		} finally {
			if (callableStatement != null)
				callableStatement.close();
		}
		
		return remainHp;
	}

	private User getRandomUser() {
		User user = new User();
		String sql = "{CALL RANDOM_ATTACKER(?, ?)}";
		Connection connection = null;
		try {
			connection = globalConnectionPool.getConnection();
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.registerOutParameter(1, Types.TINYINT);
			callableStatement.registerOutParameter(2, Types.INTEGER);
			callableStatement.execute();
			
			user.setGid(callableStatement.getInt(1));
			user.setUid(callableStatement.getInt(2));
			
			callableStatement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
				}
		}
		return user;
	}

}
