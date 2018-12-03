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
			Point pos = getAveragePosition();
			if (pos == null) return; // No units on field
			Point target = targetPoints.get(currentTarget % targetPoints.size());
			if  (pos.distanceTo(target) < BOT_CONTROL_ACCURACY) {
				currentTarget++;
				target = targetPoints.get(currentTarget % targetPoints.size());
			}
			command(target.getX(), target.getY(), 5f);
		}
	}
	
	private List<Controller> controllers = new ArrayList<>();
	
	private Arena arena;
	private long nextID; // For unit spawning
	
	public SpawnManager(Arena arena) {
		if (arena == null) throw new IllegalArgumentException();
		this.arena = arena;
		this.nextID = 0;
	}
	
	public void addBot() {
		long botID = arena.createBot();
		// Add a bot controller for this bot
		Controller c = new BotController(arena, botID);
		c.addListener(arena);
		controllers.add(c);
	}

	
	private long nextID() {
		return nextID++;
	}
	
	public void update(float dt) {
		// Go through all of the players in the arena
		// and check if any of them need to be respawned
		for (long player : arena.getPlayers()) {
			// Ignore players that have units
			if (arena.getPlayerUnits(player).size() > 0) continue;
			
			long newHorde = arena.createHorde();
			
			if (player >= 0) {
				arena.addEntities(generateHorde(player, newHorde, 0f, 0f, 0f, 5));
			} else {
				// Spawn new bot horde now that this one has died
				Point spawn  = generateBotSpawnpoint();
				// Generate bot army
				arena.addEntities(generateHorde(player, newHorde, 0f, spawn.getX(), spawn.getY(), 3));
			}
		}
		// Update any bot controllers
		for (Controller c : controllers) {
			c.update(dt);
		}
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
			units.add(generateUnit(playerID, hordeID, difficulty, x, y));
		}
		return units;
	}
	
	private Unit generateUnit(long playerID, long hordeID,
								float difficulty, float x, float y) {
		return new Knight(nextID(), playerID, hordeID, x, y, 10f, 100f);
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