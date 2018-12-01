package pfrommer.necro.client;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Knight;
import pfrommer.necro.game.Player;
import pfrommer.necro.game.Unit.State;
import pfrommer.necro.util.Display;
import pfrommer.necro.util.Renderer;

public class App {
	private Display display;
	private Arena arena;
	
	private Player player;
	
	// Nothing should happen in the constructor
	public App(Display d) {
		display = d;
	}
	
	public Display getDisplay() { return display; }
	
	public void create () {
		arena = new Arena(30, 30);
		player = new Player(0, "Foo");
		
		arena.setBackground("files/background.png");
		
		// Add our player
		arena.addPlayer(player);
		
		// Add a bunch of units
		Knight k = new Knight(5, 5, 0, 100, State.READY, player);
		arena.addEntity(k);
	}

	public void resize (int width, int height) {}

	public void render (Renderer r, float dt) {
		r.orthoCamera(0, 0, 30, 30);

		arena.render(r, player);
	}

	public void pause() {
	}

	public void resume() {
		
	}

	public void dispose() {
	}
}
