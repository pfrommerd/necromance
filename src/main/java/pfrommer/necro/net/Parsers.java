package pfrommer.necro.net;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Entity;
import pfrommer.necro.game.Knight;
import pfrommer.necro.game.Mage;
import pfrommer.necro.game.Rock;
import pfrommer.necro.game.Spell;
import pfrommer.necro.game.Unit;

public class Parsers {
	// Register all entity/event parsers
	public static void registerAll() {
		Client.AssignID.register();
		Arena.ArenaInfo.register();
		
		Entity.Moved.register();
		
		Arena.EntityAdded.register();
		Arena.EntityRemoved.register();
		Arena.PlayerAdded.register();
		Arena.PlayerRemoved.register();
		
		Unit.Run.register();
		Unit.Damage.register();
		Unit.Necromance.register();
		Unit.Target.register();
		Unit.OwnerChange.register();
		
		Knight.register();
		Rock.register();
		Mage.register();
		Spell.register();
	}
}
