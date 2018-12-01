package pfrommer.necro.game;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import pfrommer.necro.net.Protocol.Message;
import pfrommer.necro.util.Renderer;

// Represents the game state
// Can be used headlessly (i.e without rendering)
// in the server backend as well

// The Arena controller processes any commands being issued
// and deals with updating the state

// The server arena controller actually does the game logic
// while the client arena controllers just update the state
public class Arena {
	private HashMap<Long, Entity> entities = new HashMap<Long, Entity>();
	private HashMap<Long, Player> players = new HashMap<Long, Player>();
	
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
		for (EventListener l : listeners) l.onEvent(e);
	}
	
	public Player getPlayer(long playerId) { return players.get(playerId); }
	
	public boolean hasPlayer(String name) {
		for (Player p : players.values()) {
			if (p.getName().equals(name)) return true;
		}
		return false;
	}
	
	public void addPlayer(Player p) {
		players.put(p.getID(), p);
		// Fire a player added event
		fireEvent(new PlayerAddedEvent(p));
	}
	
	public void removePlayer(Player p) {
		players.remove(p.getID());
		// Fire a player removed event
		fireEvent(new PlayerRemovedEvent(p));
	}
	
	public void addEntity(Entity e) {
		e.setArena(this);
		entities.put(e.getID(), e);		
	}
	
	public void removeEntity(Entity e) {
		entities.remove(e.getID());		
	}
	
	// Will render the arena from the perspective of a given player
	// Not used on the server side, only the client
	public void render(Renderer r, Player player) {
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
	
	// All of the Arena related events
	// all private, since they should not be
	// instantiated outside of the parser
	// or the arena class itself
	
	private static class PlayerAddedEvent extends Event {
		private Player player;
		
		public PlayerAddedEvent(Player p) {
			this.player = p;
		}
		
		public void apply(Arena a) {
			a.addPlayer(player);
		}

		@Override
		public void pack(Message.Builder msg) {
		}
	}
	
	private static class PlayerRemovedEvent extends Event {
		private Player player;
		
		public PlayerRemovedEvent(Player p) {
			this.player = p;
		}
		
		public void apply(Arena a) {
			a.removePlayer(player);
		}

		@Override
		public void pack(Message.Builder msg) {
		}
	}
	
	private static class EntityAddedEvent extends Event {
		private Entity entity;
		
		public EntityAddedEvent(Entity e) {
			this.entity = e;
		}
		
		@Override
		public void apply(Arena a) {
			a.addEntity(entity);
		}

		@Override
		public void pack(Message.Builder msg) {
		}
	}
	
	private static class EntityRemovedEvent extends Event {
		private Entity entity;
		
		public EntityRemovedEvent(Entity e) {
			this.entity = e;
		}
		
		@Override
		public void apply(Arena a) {
			a.removeEntity(entity);
		}

		@Override
		public void pack(Message.Builder msg) {
		}
	}
}
