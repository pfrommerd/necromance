package pfrommer.necro.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Event.TypeCase;
import pfrommer.necro.util.Circle;
import pfrommer.necro.util.Renderer;


// The entity itself manages the position
// and syncs that using the render system
// everything else needs to be managed by the subtypes
public abstract class Entity implements Comparable<Entity>, EventProducer {
	private long id;
	private Arena arena;
	
	private HashSet<EventListener> listeners = new HashSet<>();

	// Mutating state
	protected float x;
	protected float y;
	
	public Entity(long id, float x, float y) {
		this.arena = null;
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	public void setArena(Arena a) { this.arena = a; }
	
	public Arena getArena() { return arena; }
	
	public void addListener(EventListener l) { listeners.add(l); }
	public void removeListener(EventListener l) { listeners.remove(l); }
	
	public long getID() { return id; }
	
	public float getX() { return x; }
	public float getY() { return y; }
	
	// The Z is used for the rendering order
	public float getZ() { return y; } // Z is just the y in this case
	
	// Gets the collision shape, if any
	public abstract Circle getCollider();
	
	public abstract void render(Renderer r, float dt);
	
	// To be called by the update logic
	// whenever an event should be fired
	protected void fireEvent(Event e) {
		if (arena != null) arena.fireEvent(e);
		
		for (EventListener l : new ArrayList<EventListener>(listeners)) {
			l.handleEvent(e);
		}
	}
	
	protected boolean intersecting() {
		if (getArena() != null && getCollider() != null) {
			for (Entity e : getArena().getEntities()) {
				Circle s = getCollider();
				Circle c = e.getCollider();
				if (e != this && s.intersects(c)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected void move(float x, float y) {		
		// the original x and y, prevents us from moving into things
		// we shouldn't
		float ox = this.x;
		float oy = this.y;		
		this.x = x;
		this.y = y;
		if (intersecting()) {
			this.x = ox;
			this.y = oy;
		}
		// If we are still intersecting even
		// after moving back, this means we spawned
		// ontop of a unit or something
		if (intersecting()) {
			float dx = 1/100f;
			do {
				// Move along x
				this.x += dx;
			} while (intersecting());
		}
		fireEvent(new Moved(getID(), this.x, this.y));
	}
	
	// Runs the game logic for this entity
	public abstract void update(float dt);
	
	// Serialization for this entity (different entity to entity)
	public abstract void pack(Protocol.Entity.Builder builder);

	// Utilities
	
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
	
	// for position change
	public static class Moved extends Event {
		private long unitID;
		private float x;
		private float y;
		
		public Moved(long id, float x, float y) {
			this.unitID = id;
			this.x = x;
			this.y = y;
		}
		
		@Override
		public void apply(Arena a) {
			Entity e = a.getEntity(unitID);
			if (e == null) return;
			e.move(x, y); // Update the state on the client, don't re-trigger an event
		}

		@Override
		public void pack(Protocol.Event.Builder msg) {
			msg.getMovedBuilder()
				.setId(unitID)
				.setX(x)
				.setY(y);
		}
		
		public static void register() {
			Event.Parser.add(TypeCase.MOVED, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					Protocol.Moved m = e.getMoved();
					return new Moved(m.getId(), m.getX(), m.getY());
				}
			});
		}
	}
}
