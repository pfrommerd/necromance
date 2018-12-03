package pfrommer.necro.game;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	private Map<Long, Entity> entities = new HashMap<Long, Entity>();
	
	// Manages ownership by players and hordes
	private Map<Long, Set<Unit>> owners = new HashMap<Long, Set<Unit>>();
	private Map<Long, Set<Unit>> hordes = new HashMap<Long, Set<Unit>>();
	
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
	
	public Entity getEntity(long entityID) { return entities.get(entityID); }
	public Collection<Entity> getEntities() { return entities.values(); }
	
	public void addEntity(Entity e) {
		if (entities.containsKey(e.getID())) return;
		e.setArena(this);
		entities.put(e.getID(), e);	
		
		fireEvent(new EntityAdded(e));
	}
	
	public void removeEntity(Entity e) {
		e.setArena(null);
		entities.remove(e.getID());
		
		fireEvent(new EntityRemoved(e.getID()));
	}
	
	public void addEntities(Collection<? extends Entity> entities) {
		for (Entity e : entities) {
			addEntity(e);
		}
	}
	
	public Set<Unit> getPlayerUnits(long player) {
		Set<Unit> s = owners.get(player);
		if (s == null) return Collections.emptySet();
		return s;
	}
	
	
	public Set<Unit> getHordeUnits(long hordeID) {
		Set<Unit> s = hordes.get(hordeID);
		if (s == null) return Collections.emptySet();
		return s;
	}
	
	public boolean isHordeAlive(long hordeID) {
		Set<Unit> s = getHordeUnits(hordeID);
		for (Unit u : s) {
			if (u.getHealth() > 0) return true;
		}
		return false;
	}
	
	public long getPrimaryHorde(long playerID) {
		Set<Unit> units = getPlayerUnits(playerID);
		for (Unit u : units) {
			if (u.getHealth() > 0) return u.getHorde();
		}
		return -1; // No such player or player does not control a horde
	}
	
	// Called to add a player to the owner registry
	// (that way it can be picked up by the spawn manager)
	public void addPlayer(long player) {
		if (!owners.containsKey(player)) {
			owners.put(player, new HashSet<Unit>());
			// Fire player added event
		}
	}
	
	public Collection<Long> getPlayers() {
		return owners.keySet();
	}
	
	// Gets the first open positive id
	public long createPlayer() {
		long id = 0;
		while (owners.containsKey(id)) {
			id++;
		}
		addPlayer(id);
		return id;
	}
	
	public long createBot() {
		long id = -1;
		while (owners.containsKey(id)) {
			id--;
		}
		addPlayer(id);
		return id;
	}
	
	public long createHorde() {
		long id = 0;
		while (hordes.containsKey(id)) {
			id++;
		}
		hordes.put(id, new HashSet<>());
		return id;
	}
	
	// Will also remove all entities associated
	// with this player (done first)
	public void removePlayer(long player) {
		if (owners.containsKey(player)) {
			Set<Unit> units = owners.get(player);
			for (Unit u : units) removeEntity(u);
			
			// Remove the player entirely
			owners.remove(player);
			// Fire player removed event
			
		}
	}
	
	// The following are package-protected
	// functions for internal use by entities
	// to fire events in the arena and register their ownership
	void registerOwner(Unit u, long owner) {
		if (!owners.containsKey(owner))
			owners.put(owner, new HashSet<Unit>());
		owners.get(owner).add(u);
	}
	void deregisterOwner(Unit u, long owner) {
		if (owners.containsKey(owner)) {
			owners.get(owner).remove(u);
		}
	}
	
	void registerHorde(Unit u, long horde) {
		if (!hordes.containsKey(horde))
			hordes.put(horde, new HashSet<Unit>());
		hordes.get(horde).add(u);
	}
	
	void deregisterHorde(Unit u, long hordeID) {
		if (hordes.containsKey(hordeID)) {
			hordes.get(hordeID).remove(u);
			if (hordes.get(hordeID).size() == 0)
				hordes.remove(hordeID);
		}
	}
	
	// To be called by entities who want events to be
	// sent out to the clients
	void fireEvent(Event e) {
		for (EventListener l : listeners) l.handleEvent(e);
	}

	// Arena is an event listener that consumes events and applies them
	// to the state. Normally an event that is consumed will itself
	// be emitted by the Arena
	public void handleEvent(Event e) {
		e.apply(this);
	}

	public void render(Renderer r, float dt) {
		if (background != null) r.drawImage(background, 0, 0, width, height, 0);
		// Draw all of the entities, sorted by their
		// y coordinate
		Set<Entity> sortedEntities = new TreeSet<Entity>(new Comparator<Entity>() {
			@Override
			public int compare(Entity o, Entity t) {
				return o.getZ() == t.getZ() ? o.compareTo(t) :
							o.getZ() > t.getZ() ? -1 : 1;
			}
		});
		sortedEntities.addAll(entities.values());
		
		// Render the entities
		for (Entity e : sortedEntities) {
			e.render(r, dt);
		}
	}
	
	// Should only be called on the server (or whoever is doing the game logic)
	// this actually makes all the units' actions/state changes happen
	public void update(float dt) {
		for (Entity e : entities.values()) e.update(this, dt);
	}
	
	// Utility functions
	public Point calcCameraPos(long playerID, float camWidth, float camHeight) {
		// Just go through everything associated with this player
		// and average the position
		int n = 0;
		float x = 0;
		float y = 0;
		for (Unit u : getPlayerUnits(playerID)) {
			if (u.getHealth() <= 0) continue;
			x += u.getX();
			y += u.getY();
			n++;
		}
		if (n > 0) x /= n;
		if (n > 0) y /= n;
		
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
	
	// Arena-related events
	// here it is only entity added and entity removed
	
	public static class EntityAdded extends Event {
		private Entity entity;
		
		public EntityAdded(Entity e) {
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
					return new EntityAdded(
							Entity.unpack(e.getEntityAdded().getEntity()));
				}
			});
		}
	}
	
	public static class EntityRemoved extends Event {
		private long entityID;
		
		public EntityRemoved(long entityID) {
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
					return new EntityRemoved(e.getEntityRemoved().getId());
				}
			});
		}
	}
	
	// On player added
	
	public static class PlayerAdded extends Event {
		private long playerID;
		
		public PlayerAdded(long id) {
			this.playerID = id;
		}
		
		@Override
		public void apply(Arena a) {
			a.addPlayer(playerID);
		}

		@Override
		public void pack(Protocol.Event.Builder msg) {
			msg.getPlayerAddedBuilder().setId(playerID);
		}
		
		static {
			Event.Parser.add(TypeCase.PLAYERADDED, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					return new PlayerAdded(e.getPlayerAdded().getId());
				}
			});
		}
	}
	
	public static class PlayerRemoved extends Event {
		private long playerID;
		
		public PlayerRemoved(long id) {
			this.playerID = id;
		}
				
		@Override
		public void apply(Arena a) {
			a.removePlayer(playerID);
		}

		@Override
		public void pack(Protocol.Event.Builder msg) {
			msg.getPlayerRemovedBuilder().setId(playerID);
		}
		
		static {
			Event.Parser.add(TypeCase.PLAYERREMOVED, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					return new PlayerRemoved(e.getPlayerRemoved().getId());
				}
			});
		}
	}
}
