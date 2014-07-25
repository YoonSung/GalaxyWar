package threads;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class User extends Thread {

	private final int ATTACK_NUM = 25000;
	private static Map<Integer, Galaxy> galaxies;

	@Override
	public void run() {
		for (int i = 0; i < ATTACK_NUM && !this.isInterrupted(); i++) {
			operation();
		}
	}

	private void operation() {
		Galaxy attackerGalaxy = getRandomGalaxy();
		System.out.println("attackerGalaxy : "+attackerGalaxy);
		Galaxy targetGalaxy = getTargetGalaxy(attackerGalaxy);
		System.out.println("targetGalaxy : "+targetGalaxy);
		Connection targetConnection = null;
		Connection connection = null;
		try {
			targetConnection = targetGalaxy.getConnection();
			connection = attackerGalaxy.getConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		String sql = "{CALL RANDOM_ATTACKER(?)}";
		CallableStatement callableStatement;
		try {
			callableStatement = connection.prepareCall(sql);
			callableStatement.setInt(1, attackerGalaxy.id);
			callableStatement.registerOutParameter(1, Types.INTEGER);
			callableStatement.registerOutParameter(2, Types.INTEGER);

			callableStatement.execute();

			int attackerId = (int) callableStatement.getObject(1);
			int damage = (int) callableStatement.getObject(2);

			boolean isSuccess = attack(attackerId, damage, targetGalaxy.id,
					targetConnection);
			callableStatement.close();

			if (isSuccess) {
				sql = "{CALL LOG(?, ?)}";
				String logText = String.format(
						"User %d attacked %s! %s damaged %d!", attackerId,
						targetGalaxy.name, targetGalaxy.name, damage);
				callableStatement = connection.prepareCall(sql);
				callableStatement.setInt(1, attackerId);
				callableStatement.setString(2, logText);

				callableStatement.execute();
				callableStatement.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean attack(int attackerId, int damage, int targetId,
			Connection targetConnection) throws SQLException {
		String sql = "UPDATE galaxy SET HP = HP-? WHERE GID = ?";
		PreparedStatement pstmt = targetConnection.prepareStatement(sql);
		pstmt.setInt(1, damage);
		pstmt.setInt(2, targetId);

		return (pstmt.executeUpdate() == 1);
	}

	private Galaxy getTargetGalaxy(Galaxy attackerGalaxy) {
		Galaxy galaxy = attackerGalaxy;
		while (galaxy == attackerGalaxy) {
			galaxy = getRandomGalaxy();
		}
		return galaxy;
	}

	private Galaxy getRandomGalaxy() {
		int MINIMUM = 1;
		int MAXIMUM = 4;
		Random rn = new Random();
		int randomNum = rn.nextInt((MAXIMUM - MINIMUM) + 1) + MINIMUM;
		return galaxies.get(randomNum);
	}

	public static void setGalaxies(Map<Integer, Galaxy> galaxies) {
		User.galaxies = galaxies;
	}
}
