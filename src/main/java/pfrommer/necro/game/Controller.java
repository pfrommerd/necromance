package pfrommer.necro.game;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pfrommer.necro.game.Arena.EntityAddedEvent;
import pfrommer.necro.game.Arena.EntityRemovedEvent;

public abstract class Controller implements EventListener, EventProducer {
	protected Arena arena;
	private long playerID;
	// All units this controller owns
	private Map<Long, Unit> owned = new HashMap<>();
	private Set<EventListener> listeners = new HashSet<>();
	
	public Controller(Arena arena) {
		this.playerID = -1;
		this.arena = arena;		
		this.arena.addListener(this);
	}
	
	public Controller(Arena arena, long playerID) {
		this.arena = arena;		
		this.arena.addListener(this);
		
		setPlayerID(playerID);
	}
	
	public void addListener(EventListener l) { listeners.add(l); }
	public void removeListener(EventListener l) { listeners.remove(l); }
	
	protected void fireEvent(Event e) {
		for (EventListener l : listeners) l.handleEvent(e);
	}
	
	// Will clear the ownership map and reload from the
	// arena
	protected void setPlayerID(long id) {
		this.playerID = id;
		owned.clear();
		for (Unit u : arena.getUnitsFor(playerID)) {
			owned.put(u.getID(), u);
		}
	}

	protected Collection<Unit> getOwned() { return owned.values(); }
	
	// Will get changes in the ownership for the commands
	@Override
	public void handleEvent(Event e) {
		// This is moderately bad style, but this is the
		// most elegant way we currently have, unfortunately
		// since it design-wise doesn't make sense for the events
		// to interact with the controllers
		
		if (e instanceof Arena.EntityAddedEvent) {
			EntityAddedEvent a = (EntityAddedEvent) e;
			Entity entity = a.getEntity();
			if (entity instanceof Unit) {
				Unit u = (Unit) entity;
				if (u.getOwner() == playerID) owned.put(u.getID(), u);
			}
		} else if (e instanceof Arena.EntityRemovedEvent) {
			EntityRemovedEvent r = (EntityRemovedEvent) e;
			if (owned.containsKey(r.getEntityID()))
				owned.remove(r.getEntityID());
		}
	}
	
	public void command(float x, float y) {
		Collection<Unit> units = getOwned();
		// Command the units to go toward the x, y position
		for (Unit u : units) {
			float dx = x - u.getX();
			float dy = y - u.getY();
			
			float theta = (float) Math.atan2(dy, dx);
			float speed = Math.min(u.getMaxSpeed(), dx*dx + dy*dy);
			fireEvent(u.createRunCommand(theta, speed));
		}
	}
	
	public void stop() {
		Collection<Unit> units = getOwned();
		// Command the units to go toward the x, y position
		for (Unit u : units) {
			// Stop them
			fireEvent(u.createRunCommand(u.getTheta(), 0));
		}
	}
	
	public abstract void update(float dt);
}
