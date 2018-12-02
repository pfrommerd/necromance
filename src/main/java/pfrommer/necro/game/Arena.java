package pfrommer.necro.game;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Event.TypeCase;
import pfrommer.necro.util.Point;
import pfrommer.necro.util.Renderer;

// Represents the game state
// Can be used headlessly (i.e without rendering)
// in the server backend as well

// The Arena controller processes any commands being issued
// and deals with updating the state

// The server arena controller actually does the game logic
// while the client arena controllers just update the state
public class Arena implements EventListener, EventProducer {
	private HashMap<Long, Entity> entities = new HashMap<Long, Entity>();
	
	private Set<EventListener> listeners = new HashSet<EventListener>();
	
	// The arena width and height
	private float width, height;
	
	private String background;
	
	public Arena(float w, float h) {
		this.width = w;
		this.height = h;
	}
	
	public void setBackground(String img) { background = img; }
	
	public float getWidth() { return width; }
	public float getHeight() { return height; }
	
	public void addListener(EventListener l) { listeners.add(l); }
	public void removeListener(EventListener l) { listeners.remove(l); }
	
	public void fireEvent(Event e) {
		for (EventListener l : listeners) l.handleEvent(e);
	}
	
	public void handleEvent(Event e) {
		e.apply(this);
	}
	
	public Entity getEntity(long entityID) { return entities.get(entityID); }
	public Collection<Entity> getEntities() { return entities.values(); }
	
	public void addEntity(Entity e) {
		e.setArena(this);
		entities.put(e.getID(), e);	
		
		fireEvent(new EntityAddedEvent(e));
	}
	
	public void removeEntity(Entity e) {
		entities.remove(e.getID());
		
		fireEvent(new EntityRemovedEvent(e.getID()));
	}
	
	public Set<Unit> getUnitsFor(long player) {
		Set<Unit> units = new HashSet<Unit>();
		for (Entity e : entities.values()) {
			if (e instanceof Unit) {
				Unit u = (Unit) e;
				if (u.getOwner() == player) units.add(u);
			}
		}
		return units;
	}
	
	public Point calcCameraPos(long playerID, float camWidth, float camHeight) {
		// Just go through everything associated with this player
		// and average the position
		int n = 0;
		float x = 0;
		float y = 0;
		for (Unit u : getUnitsFor(playerID)) {
			x += u.getX();
			y += u.getY();
			n++;
		}
		
		x /= n;
		y /= n;
		
		// This is the camera x, y, but we need to bound it so it doesn't go
		// over the edge of the arena
		if (camWidth < width) x = Math.max(-width/2 + camWidth/2,
								 Math.min(width/2 - camWidth/2, x));
		else x = 0;
		if (camHeight < height) y = Math.max(-height/2 + camHeight/2,
									Math.min(height/2 - camHeight/2, y));
		else y = 0;
		return new Point(x, y);
	}

	public void render(Renderer r) {
		if (background != null) r.drawImage(background, 0, 0, width, height, 0);
		
		// Draw all of the entities, sorted by their
		// y coordinate
		Set<Entity> sortedEntities = new TreeSet<Entity>(new Comparator<Entity>() {
			@Override
			public int compare(Entity o, Entity t) {
				return o.getZ() == t.getZ() ? 0 : o.getZ() > t.getZ() ? 1 : -1;
			}
		});
		sortedEntities.addAll(entities.values());
		
		// Render the entities
		for (Entity e : sortedEntities) {
			e.render(r);
		}
	}
	
	// Should only be called on the server (or whoever is doing the game logic)
	// this actually makes all the units' actions/state changes happen
	public void update(float dt) {
		for (Entity e : entities.values()) e.update(this, dt);
	}
	
	// Arena-related events
	public static class EntityAddedEvent extends Event {
		private Entity entity;
		
		public EntityAddedEvent(Entity e) {
			this.entity = e;
		}
		
		public Entity getEntity() { return entity; }
		
		@Override
		public void apply(Arena a) {
			a.addEntity(entity);
		}

		@Override
		public void pack(Protocol.Event.Builder msg) {
			entity.pack(msg.getEntityAddedBuilder().getEntityBuilder());
		}
		
		static {
			Event.Parser.add(TypeCase.ENTITYADDED, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					return new EntityAddedEvent(
							Entity.unpack(e.getEntityAdded().getEntity()));
				}
			});
		}
	}
	
	public static class EntityRemovedEvent extends Event {
		private long entityID;
		
		public EntityRemovedEvent(long entityID) {
			this.entityID = entityID;
		}
		
		public long getEntityID() { return entityID; }
		
		@Override
		public void apply(Arena a) {
			a.removeEntity(a.getEntity(entityID));
		}

		@Override
		public void pack(Protocol.Event.Builder msg) {
			msg.getEntityRemovedBuilder().setId(entityID);
		}
		
		static {
			Event.Parser.add(TypeCase.ENTITYREMOVED, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					return new EntityRemovedEvent(e.getEntityRemoved().getId());
				}
			});
		}
	}
}
