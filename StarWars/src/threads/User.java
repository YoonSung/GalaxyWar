package threads;

import java.util.Random;

public class User {
	int gid;
	int uid;
	
	public int getRandomTargetGID(int min, int max) {
		Random random = new Random();
		int targetId = gid;
		while (targetId == gid) {
			targetId = random.nextInt((max - min) + 1) + min;
		}
		return targetId;
	}
}
