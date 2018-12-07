package pfrommer.necro.game;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import pfrommer.necro.util.Point;

public abstract class Controller implements EventProducer {
	protected Arena arena;
	protected long playerID;
	// All units this controller owns
	private Set<EventListener> listeners = new HashSet<>();
	
	public Controller(Arena arena, long playerID) {
		this.arena = arena;				
		this.playerID = playerID;
	}
	
	public void setID(long id) { playerID = id; }
	
	public void addListener(EventListener l) { listeners.add(l); }
	public void removeListener(EventListener l) { listeners.remove(l); }
	
	
	protected void fireEvent(Event e) {
		for (EventListener l : listeners) l.handleEvent(e);
	}
	
	public void command(float x, float y) {
		Collection<Unit> units = arena.getPlayerUnits(playerID);
		// Command the units to go toward the x, y position
		for (Unit u : units) {
			float dx = x - u.getX();
			float dy = y - u.getY();
			
			float theta = (float) Math.atan2(dy, dx);
			float speed = Math.min(u.getMaxSpeed(), dx*dx + dy*dy);
			fireEvent(u.createRunCommand(theta, speed));
		}
	}
	
	public void command(float x, float y, float speed) {
		Collection<Unit> units = arena.getPlayerUnits(playerID);
		// Command the units to go toward the x, y position
		for (Unit u : units) {
			float dx = x - u.getX();
			float dy = y - u.getY();
			
			float theta = (float) Math.atan2(dy, dx);
			fireEvent(u.createRunCommand(theta, speed));
		}
	}
	
	public void stop() {
		Collection<Unit> units = getUnits();
		
		// Command the units to go toward the x, y position
		for (Unit u : units) {
			// Stop them
			fireEvent(u.createRunCommand(u.getTheta(), 0));
		}
	}
	
	public void necromance() {
		Collection<Unit> units = getNecromanceable();
		for (Unit u : units) {
			// NECROMANCE
			fireEvent(u.createNecromanceCommand());
		}
	}
	protected Collection<Unit> getUnits() {
		return arena.getLivingPlayerUnits(playerID);
	}

	protected Collection<Unit> getNecromanceable() {
		Set<Unit> n = new HashSet<Unit>();
		for (Unit u : arena.getPlayerUnits(playerID)) {
			if (u.getHealth() > 0) continue;
			if (!arena.isHordeAlive(u.getHorde()) &&
					arena.getPrimaryHorde(playerID) > 0) {
				n.add(u);
			}
		}
		return n;
	}
	
	// Get the average unit position (for the bot controller)
	protected Point getAveragePosition() {
		Collection<Unit> units = arena.getPlayerUnits(playerID);
		float x = 0, y = 0;
		int n = 0;
		for (Unit u : units) {
			if (u.getHealth() <= 0) continue;
			x += u.getX(); y += u.getY();
			n++;
		}
		if (n == 0) return null;
		return new Point(x / n, y / n);
	}
	
	public abstract void update(float dt);
}
