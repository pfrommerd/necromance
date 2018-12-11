import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Knight;
import pfrommer.necro.game.Mage;
import pfrommer.necro.game.Rock;
import pfrommer.necro.game.Unit;

public class ArenaTests {
	private Arena arena = null;
	private Unit[] units = null;
	
	@Before
	public void setup() {
		arena = new Arena();
		// Make a mix of mages and knights
		Knight k1 = new Knight(0, 0, 1, 0f, 0f);
		Mage k2 = new Mage(1, 0, 1, 0f, 0f);
		Knight k3 = new Knight(2, 1, 2, 0f, 0f);
		Knight k4 = new Knight(3, 1, 2, 0f, 0f);
		Mage k5 = new Mage(4, 1, 2, 0f, 0f);
		units = new Unit[] {k1, k2, k3, k4, k5};
		// Should auto-add the players and hordes
		arena.addEntities(Arrays.asList(units));
	}
	
	@Test
	public void testArenaCreation() {
		Arena a = new Arena();
		// Assert no entites, no hordes, no players,
		// and a 0x0 width, height
		assertEquals(0, a.getEntities().size());
		assertEquals(0, a.getPlayers().size());
		assertEquals(0, a.getHordes().size());
		assertTrue(0 == a.getWidth());
		assertTrue(0 == a.getHeight());
	}
	
	@Test
	public void testArenaDimensions() {
		Arena a = new Arena();
		a.setWidth(10f);
		a.setHeight(20f);
		assertTrue(10f == a.getWidth());
		assertTrue(20f == a.getHeight());
	}
	
	@Test
	public void testEntitiesSameID() {
		Arena a = new Arena();
		
		a.addEntity(new Rock(0, 10f, 10f));
		a.addEntity(new Rock(1, 20f, 20f));
		
		assertEquals(2, a.getEntities().size());
		a.addEntity(new Rock(1, 15f, 15f));
		
		// Should not have added an entity
		assertEquals(2, a.getEntities().size());
	}
	
	@Test
	public void testPlayerUnits() {
		// Check that there are two players
		// and one player has 2 entities, another has 3 (same for the hordes)
		assertEquals(5, arena.getEntities().size());
		assertEquals(2, arena.getPlayers().size());
		
		// Check that the players and hordes contain the actual
		// entities
		Set<Unit> playerA = arena.getPlayerUnits(0);
		Set<Unit> playerB = arena.getPlayerUnits(1);
		assertEquals(2, playerA.size());
		assertEquals(3, playerB.size());
		// Check the contains
		assertTrue(playerA.contains(units[0]));
		assertTrue(playerA.contains(units[1]));
		assertTrue(playerB.contains(units[2]));
		assertTrue(playerB.contains(units[3]));
		assertTrue(playerB.contains(units[4]));
	}
	
	@Test
	public void testEmptyPlayer() {
		// If a player has no units, they
		// should still be part of the players
		arena.removeEntities(Arrays.asList(units));
		Set<Long> players = arena.getPlayers();
		assertEquals(2, players.size());
		assertTrue(players.contains(0l));
		assertTrue(players.contains(1l));
	}

	
	@Test
	public void testHordeUnits() {
		assertEquals(5, arena.getEntities().size());
		assertEquals(2, arena.getHordes().size());

		Set<Unit> hordeA = arena.getHordeUnits(1);
		Set<Unit> hordeB = arena.getHordeUnits(2);
		assertEquals(2, hordeA.size());
		assertEquals(3, hordeB.size());
		// Check the contains
		assertTrue(hordeA.contains(units[0]));
		assertTrue(hordeA.contains(units[1]));
		assertTrue(hordeB.contains(units[2]));
		assertTrue(hordeB.contains(units[3]));
		assertTrue(hordeB.contains(units[4]));
	}
	
	@Test
	public void testEmptyHordes() {
		// If horde has no units,
		// it should be removed
		arena.removeEntities(Arrays.asList(units));
		assertEquals(0, arena.getHordes().size());
	}
	
	@Test
	public void testNecromance() {
		// Kill knight 0 and knight 1
		while (units[0].getHealth() > 0)
			units[3].launchAttack(units[0]);
		while (units[1].getHealth() > 0)
			units[3].launchAttack(units[1]);
		
		// Ownership should have changed here,
		// but horde not
		assertEquals(1, units[0].getOwner());
		assertEquals(1, units[1].getOwner());
		assertEquals(1, units[0].getHorde());
		assertEquals(1, units[1].getHorde());

		// Player 0 should have no units, player 1 should have 5
		assertEquals(2, arena.getPlayers().size());
		assertEquals(0, arena.getPlayerUnits(0).size());
		assertEquals(5, arena.getPlayerUnits(1).size());

		// Horde 1 should now be dead
		assertFalse(arena.isHordeAlive(1));
		assertTrue(arena.isHordeAlive(2));
		
		// Necromance unit 0 and unit 1
		units[0].createNecromanceCommand().apply(arena);
		units[1].createNecromanceCommand().apply(arena);
		
		// Knight 0 and knight 1 should now be
		// part of player 2 and horde 1 should
		// be removed
		assertFalse(arena.getHordes().contains(1l));
		assertEquals(5, arena.getHordeUnits(2).size());
		
		// Units should now belong to player 1
		// horde and have full health
		assertEquals(2, units[0].getHorde());
		assertEquals(2, units[1].getHorde());
		assertTrue(Knight.KNIGHT_MAX_HEALTH == units[0].getHealth());
		assertTrue(Knight.KNIGHT_MAX_HEALTH == units[1].getHealth());
	}
	
	// Test that we can't necromance
	// a unit that shouldn't be necromanced
	@Test
	public void testNoNecromance() {
		assertEquals(0, units[0].getOwner());
		while (units[0].getHealth() > 0)
			units[3].launchAttack(units[0]);
		// Should be different owner, same horde
		assertEquals(1, units[0].getOwner());
		assertEquals(1, units[0].getHorde());
		
		units[0].createNecromanceCommand().apply(arena);
		
		// Since not all units in the horde are dead
		// this should do nothing
		assertEquals(1, units[0].getOwner());
		assertEquals(1, units[0].getHorde());
		assertTrue(arena.getHordeUnits(1).contains(units[0]));
		assertTrue(0 == units[0].getHealth());
	}
	
	@Test(timeout = 1000)
	public void testEngage() {
		arena.update(1);
		// Check that all the units are engaged
		for (Unit u : units) {
			assertTrue(u.getTarget() >= 0);
		}
		// run until someone dies
		// if that doesn't happen, this will run forever
		// and indicate that the fighting isn't working
		while (arena.isHordeAlive(1) &&
				arena.isHordeAlive(2)) arena.update(0.1f);
		// yay! one of the hordes is dead
	}
	
	@Test
	public void testMove() {
		Arena a = new Arena();
		Knight k = new Knight(0, 0, 0, 0f, 0f);
		a.addEntity(k);
		
		for (float theta = 0; theta < 1f; theta += 0.1f) {
			for (float speed = 0; speed < 1f; speed += 0.1f) {
				k.createRunCommand(theta, speed).apply(a);
				if (speed > 0) assertTrue(Math.abs(theta - k.getTheta()) < 0.001f);
				assertTrue(Math.abs(speed - k.getSpeed()) < 0.001f);
				for (int i = 0; i < 5; i++) {
					float x = k.getX();
					float y = k.getY();
					a.update(0.1f);
					// check that we've moved appropriately
					x += speed * (float) Math.cos(theta) * 0.1f;
					y += speed * (float) Math.sin(theta) * 0.1f;
					assertTrue(Math.abs(x - k.getX()) < 0.001f);
					assertTrue(Math.abs(y - k.getY()) < 0.001f);
				}
			}
		}
	}
}
