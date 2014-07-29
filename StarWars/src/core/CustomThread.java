package core;

public class CustomThread extends Thread {
	private boolean isOperation = false;
	
	@Override
	public void run() {
		isOperation = true;
	}
	
	public boolean isOperation() {
		return isOperation;
	}
	
	public void operationStart() {
		isOperation = true;
	}
	
	public void operationEnd() {
		isOperation = false;
	}
}
