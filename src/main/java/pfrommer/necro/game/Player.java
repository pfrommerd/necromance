package pfrommer.necro.game;

public class Player {
	private long id;
	private String name;
	
	public Player(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getName() { return name; }
	public long getID() { return id; }
}
