package pfrommer.necro.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pfrommer.necro.util.Point;

// Handles the creation of new units
// for players to control, enemies, etc.

// Only on the server,
// listens in on the ownership changes
public class SpawnManager {
	public static final float BOT_SPAWN_OFFSET = 5f;
	public static final float BOT_CONTROL_OFFSET = 5f;
	public static final float BOT_CONTROL_ACCURACY = 5f;
	public static final float PER_UNIT_SPAWN_RADIUS = 3.0f;
	
	// The bot controller
	public static class BotController extends Controller {		
		private List<Point> targetPoints = new ArrayList<Point>();
		private int currentTarget = 0;
		
		public BotController(Arena arena, long playerID) {
			super(arena, playerID);
			// Generate 10 random points in the arena
			for (int i = 0; i < 10; i++) {
				float rx = 2 * ((float)Math.random() - 0.5f);
				float ry = 2 * ((float)Math.random() - 0.5f);
				
				targetPoints.add(new Point(rx * arena.getWidth()/2f,
										   ry * arena.getHeight()/2f));
			}
		
		}

		@Override
		public void update(float dt) {
			Point target = targetPoints.get(currentTarget % targetPoints.size());
			for (Unit u : getUnits() ) {
				// If any of the units are close enough, move
				// somewhere else
				Point pos = new Point(u.getX(), u.getY());
				if  (pos.distanceTo(target) < BOT_CONTROL_ACCURACY) {
					currentTarget++;
				}
			}
			target = targetPoints.get(currentTarget % targetPoints.size());
			command(target.getX(), target.getY(), 5f);
		}
	}
	
	private List<Controller> controllers = new ArrayList<>();
	private Arena arena;
	private float difficulty = 1f;
	
	public SpawnManager(Arena arena) {
		if (arena == null) throw new IllegalArgumentException();
		this.arena = arena;
	}
	
	public void addBot() {
		long botID = arena.createBot();
		// Add a bot controller for this bot
		Controller c = new BotController(arena, botID);
		c.addListener(arena); // The arena to list to this controller
		controllers.add(c);
	}
	
	public void update(float dt) {
		difficulty += dt; // Every second- increase difficulty by 1

		// Go through all of the players in the arena
		// and check if any of them need to be respawned
		for (long player : arena.getPlayers()) {
			boolean skip = false;
			for (Unit u : arena.getPlayerUnits(player)) {
				if (u.getHealth() > 0) {
					skip = true;
					break;
				}
			}
			if (skip) continue;
			
			// The player is dead, but
			// remove any units the player owns (which are dead but
			// we don't want the player reviving the units)
			for (Unit u : arena.getPlayerUnits(player)) {
				arena.removeEntity(u);
			}
			
			long newHorde = arena.createHorde();

			if (player >= 0) {
				arena.addEntities(generateHorde(player, newHorde, 10f, 0f, 0f, 5));
			} else {
				// Spawn new bot horde now that this one has died
				Point spawn  = generateBotSpawnpoint();
				int num = (int) Math.log(8 * difficulty);
				// Generate bot army, for now all difficulty 0
				arena.addEntities(generateHorde(player, newHorde, 10f,
												spawn.getX(), spawn.getY(), num));
			}
		}
		// Update any bot controllers
		for (Controller c : controllers) c.update(dt);
	}
	
	private Collection<Unit> generateHorde(long playerID, long hordeID,
											float difficulty,
											float cx, float cy, int num) {
		List<Unit> units = new ArrayList<Unit>();
		// Calculate the generation radius based on the number of units
		float radius = PER_UNIT_SPAWN_RADIUS * num;
		for (int i = 0; i < num; i++) {
			// Generate random theta and radius
			float x = radius * (float) Math.random() + cx;
			float y = radius * (float) Math.random() + cy;
			units.add(generateUnit(arena.getLargestID() + 1 + i,
									playerID, hordeID, x, y));
		}
		return units;
	}
	
	private Unit generateUnit(long id, long playerID, long hordeID, float x, float y) {
		// Make ~1/7 units a mage
		return Math.random() < 0.85 ?
							 new Knight(id, playerID, hordeID, x, y) :
							 new Mage(id, playerID, hordeID, x, y);
	}
	
	
	private Point generateBotSpawnpoint() {
		int side = (int) (4 * Math.random());
		float rand = (float) Math.random() - 0.5f;
		
		if (side == 0) { // Top edge
			return new Point(rand * arena.getWidth(),
			 				 arena.getHeight()/2f + BOT_SPAWN_OFFSET);
		} else if (side == 1) { // Bottom edge
			return new Point(rand * arena.getWidth(),
			 				 -arena.getHeight()/2f - BOT_SPAWN_OFFSET);
		} else if (side == 2) { // Left edge
			return new Point(-arena.getWidth()/2f - BOT_SPAWN_OFFSET,
					 		 rand * arena.getHeight());
		} else { // Right edge
			return new Point(arena.getWidth()/2f + BOT_SPAWN_OFFSET,
					 	 	 rand * arena.getHeight());
		}
	}
}
