package pfrommer.necro.game;

import pfrommer.necro.util.Renderer;

public abstract class Entity implements Comparable<Entity> {
	private long id;
	private Arena arena;
		
	public Entity(long id) {
		this.arena = null;
		this.id = id;
	}
	
	public void setArena(Arena a) { this.arena = a; }
	
	public long getID() { return id; }
	
	public abstract float getX();
	public abstract float getY();
	public abstract float getZ(); // Used for the draw order
	
	public abstract void render(Renderer r);
	
	// Runs the game logic
	public abstract void update(Arena a, float dt);
	
	// To be called by the update logic
	// whenever an event should be fired
	protected void fireEvent(Event e) {
		// Notifies all the arena listeners
		if (arena != null) arena.fireEvent(e);
	}
	
	@Override
	public int hashCode() {
		return (int) id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof Entity)) return false;
		Entity e = (Entity) o;
		return e.getID() == id;
	}
	
	@Override
	public int compareTo(Entity other) {
		long oid = other.getID();
		return oid == id ? 0 : oid > id ? -1 : 1;
	}
}
