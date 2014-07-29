package dto;

import java.util.Random;

public class User {
	private int gid;
	private int uid;
	
	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getRandomTargetGID(int min, int max) {
		Random random = new Random();
		int targetId = gid;
		while (targetId == gid) {
			targetId = random.nextInt((max - min) + 1) + min;
		}
		return targetId;
	}
}
