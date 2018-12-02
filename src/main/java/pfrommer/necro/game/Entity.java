package pfrommer.necro.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.util.Rectangle;
import pfrommer.necro.util.Renderer;

public abstract class Entity implements Comparable<Entity>, EventProducer {
	private long id;
	private Arena arena;
	
	private HashSet<EventListener> listeners = new HashSet<>();
		
	public Entity(long id) {
		this.arena = null;
		this.id = id;
	}
	
	public void setArena(Arena a) { this.arena = a; }
	public Arena getArena() { return arena; }
	
	public void addListener(EventListener l) { listeners.add(l); }
	public void removeListener(EventListener l) { listeners.remove(l); }
	
	public long getID() { return id; }
	
	public abstract float getX();
	public abstract float getY();
	public abstract float getZ(); // Used for the draw order
	
	// Gets the collision rectangle
	public abstract Rectangle getCollider();
	
	public abstract void render(Renderer r);
	
	// Runs the game logic for this entity
	public abstract void update(Arena a, float dt);
	
	// Serialization
	public abstract void pack(Protocol.Entity.Builder builder);
	
	// To be called by the update logic
	// whenever an event should be fired
	protected void fireEvent(Event e) {
		if (arena != null) arena.fireEvent(e);
		
		for (EventListener l : new ArrayList<EventListener>(listeners)) {
			l.handleEvent(e);
		}
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
	
	// For deserialization
	private static HashMap<Protocol.Entity.TypeCase, Parser> parsers = new HashMap<>();
	
	@FunctionalInterface
	public interface Parser {
		public Entity unpack(Protocol.Entity e);
		
		public static void add(Protocol.Entity.TypeCase t, Parser p) {
			parsers.put(t, p);
		}
		
		public static Parser get(Protocol.Entity.TypeCase t) {
			return parsers.get(t);
		}
	}
	
	public static Entity unpack(Protocol.Entity e) {
		Parser p = Parser.get(e.getTypeCase());
		if (p == null)
			throw new IllegalArgumentException("Could not unpack entity type");
		return p.unpack(e);
	}
}
