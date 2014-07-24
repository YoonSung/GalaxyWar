import java.util.ArrayList;


public class Main {
	
	private static final int ATTACK_THREAD_NUM = 4; 
	private static ArrayList<Thread> threads = new ArrayList<Thread>();
	
	public static void main(String[] args) {
		
		SocketThread socketThread = new SocketThread();
		socketThread.start();
		
		//회원가입
//		JoinThread joinThread = new JoinThread();
//		joinThread.start();
		
//		threads.add(joinThread);
		
		//공격
		
//		for (int i = 0; i < ATTACK_THREAD_NUM; i++) {
//			AttackThread attackThread = new AttackThread();
//			threads.add(attackThread);
//			attackThread.start();
//		}
		
	}
	
	public static void gameOver() {
		for (Thread thread : threads) {
			if (!thread.isAlive())
				continue;
			thread.interrupt();
		}
	}
}
