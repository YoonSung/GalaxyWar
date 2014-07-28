package threads;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import db.ConnectionPool;

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
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return galaxyIDToDBID;
	}

	@Override
	public void run() {
		for (int i = 0; i < ATTACK_NUM && !this.isInterrupted(); i++) {
			operation();
		}
	}

	private void operation() {
		User attacker = getRandomUser();
		if (attacker == null) {
			System.out.println("NULL PTR ECPT!");
			return;
		}
		int attackerDBID = galaxyIDToDBID.get(attacker.gid);
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
			callableStatement.setInt(1, attacker.uid);
			callableStatement.registerOutParameter(2, Types.INTEGER);
			callableStatement.execute();

			int damage = (int) callableStatement.getObject(2);

			boolean isSuccess = attack(attacker.uid, damage, targetGalaxyID,
					targetConnection);
			callableStatement.close();

			if (isSuccess) {
				sql = "{CALL LOG(?, ?, ?)}";
				callableStatement = attackConnection.prepareCall(sql);
				callableStatement.setInt(1, attacker.uid);
				callableStatement.setInt(2, targetGalaxyID);
				callableStatement.setInt(3, damage);

				callableStatement.execute();
				callableStatement.close();
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

	private boolean attack(int attackerId, int damage, int targetGalaxyID,
			Connection targetConnection) throws SQLException {
		String sql = "UPDATE galaxy SET HP = HP-? WHERE GID = ?";
		PreparedStatement pstmt = targetConnection.prepareStatement(sql);
		pstmt.setInt(1, damage);
		pstmt.setInt(2, targetGalaxyID);

		return (pstmt.executeUpdate() == 1);
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
			user.gid = (int) callableStatement.getObject(1);
			user.uid = (int) callableStatement.getObject(2);
			System.out.println(user.gid);
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
