package threads;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Random;

public class User extends Thread {

	private final int ATTACK_NUM = 25000;
	private static List<Galaxy> galaxies;

	@Override
	public void run() {
		for (int i = 0; i < ATTACK_NUM && !this.isInterrupted(); i++) {
			operation();
		}
	}

	private void operation() {
		Galaxy attackerGalaxy = getRandomGalaxy();
		Galaxy targetGalaxy = getTargetGalaxy(attackerGalaxy);

		Connection targetConnection = targetGalaxy.getConnection();
		Connection connection = attackerGalaxy.getConnection();
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
		Random random = new Random();
		int rand = random.nextInt() % 4;
		return galaxies.get(rand);
	}

	public static void setGalaxies(List<Galaxy> galaxies) {
		User.galaxies = galaxies;
	}
}
