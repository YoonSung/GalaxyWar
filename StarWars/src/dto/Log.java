package dto;

import java.util.ArrayList;
import java.util.Collections;
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
		
		for (int i = 0 ; i < logList.size() ; ++i) {
			returnList.add(logList.get(i));
			logList.remove(i);
		}
		
		return returnList;
	}
}
