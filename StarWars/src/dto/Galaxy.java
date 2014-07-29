package dto;


public class Galaxy {

	private int id;
	private String name;
	private int hp;
	
	public int getHp() {
		return hp;
	}
	
	public String getName() {
		return name;
	}
	
	public Galaxy(int id, String name, int hp) {
		this.id = id;
		this.name = name;
		this.hp = hp;
	}
}
