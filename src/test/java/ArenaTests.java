import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Knight;
import pfrommer.necro.game.Rock;
import pfrommer.necro.game.Unit;

public class ArenaTests {
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
	public void testUnitsHordesAndPlayers() {
		Arena a = new Arena();

		
		Knight k1 = new Knight(0, 0, 1, 0f, 0f);
		Knight k2 = new Knight(1, 0, 1, 0f, 0f);
		Knight k3 = new Knight(2, 1, 2, 0f, 0f);
		Knight k4 = new Knight(3, 1, 2, 0f, 0f);
		Knight k5 = new Knight(4, 1, 2, 0f, 0f);
		a.addEntities(Arrays.asList(k1, k2, k3, k4, k5));
		
		// Check that there are two players, two hordes
		// and one player has 2 entities, another has 3 (same for the hordes)
		assertEquals(5, a.getEntities().size());
		assertEquals(2, a.getPlayers().size());
		assertEquals(2, a.getHordes().size());
		
		// Check that the players and hordes contain the actual
		// entities
		Set<Unit> playerA = a.getPlayerUnits(0);
		Set<Unit> playerB = a.getPlayerUnits(1);
		assertEquals(2, playerA.size());
		assertEquals(3, playerB.size());
		// Check the contains
		assertTrue(playerA.contains(k1));
		assertTrue(playerA.contains(k2));
		assertTrue(playerB.contains(k3));
		assertTrue(playerB.contains(k4));
		assertTrue(playerB.contains(k5));
	}
}
