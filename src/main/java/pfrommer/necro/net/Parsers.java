package pfrommer.necro.net;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Entity;
import pfrommer.necro.game.Knight;
import pfrommer.necro.game.Unit;

public class Parsers {
	// Register all entity/event parsers
	public static void registerAll() {
		Client.AssignID.register();
		
		Entity.Moved.register();
		
		Arena.EntityAdded.register();
		Arena.EntityRemoved.register();
		
		Unit.Run.register();
		Unit.Damage.register();
		Unit.Necromance.register();
		Unit.Target.register();
		Unit.OwnerChange.register();
		
		Knight.register();
	}
}