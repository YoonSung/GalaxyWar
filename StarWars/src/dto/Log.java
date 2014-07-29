package dto;

import java.util.ArrayList;
import java.util.List;

public class Log {
	
	private static Log instance = new Log();
	
	private Log() {}
	
	public static Log getInstance() {
		return instance;
	}
	
	private List<String> logList = new ArrayList<String>();
	
	public void addList(String log) {
		logList.add(log);
	}
	
	public List<String> getLogAll() {
		ArrayList<String> returnList = new ArrayList<String>();
		logList.removeAll(returnList);
		
		return returnList;
	}
}
