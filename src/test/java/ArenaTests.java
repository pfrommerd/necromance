import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Knight;
import pfrommer.necro.game.Rock;
import pfrommer.necro.game.Unit;

public class ArenaTests {
	private Arena arena = null;
	private Knight[] knights = null;
	
	@Before
	public void setup() {
		Arena a = new Arena();

		Knight k1 = new Knight(0, 0, 1, 0f, 0f);
		Knight k2 = new Knight(1, 0, 1, 0f, 0f);
		Knight k3 = new Knight(2, 1, 2, 0f, 0f);
		Knight k4 = new Knight(3, 1, 2, 0f, 0f);
		Knight k5 = new Knight(4, 1, 2, 0f, 0f);
		knights = new Knight[] {k1, k2, k3, k4, k5};
		a.addEntities(Arrays.asList(knights));
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
		assertTrue(playerA.contains(knights[0]));
		assertTrue(playerA.contains(knights[1]));
		assertTrue(playerB.contains(knights[2]));
		assertTrue(playerB.contains(knights[3]));
		assertTrue(playerB.contains(knights[4]));
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
		assertTrue(hordeA.contains(knights[0]));
		assertTrue(hordeA.contains(knights[1]));
		assertTrue(hordeB.contains(knights[2]));
		assertTrue(hordeB.contains(knights[3]));
		assertTrue(hordeB.contains(knights[4]));
	}
}
