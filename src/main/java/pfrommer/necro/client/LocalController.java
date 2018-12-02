package pfrommer.necro.client;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Controller;
import pfrommer.necro.util.Display;

public class LocalController extends Controller {	
	public LocalController(Arena arena) {
		super(arena, 0);
	}

	public void update(float dt) {}
	
	public void update(Display display, float cx, float cy,
						float dt) {
		// Check if mouse is down and if so, where it is
		if (display.isLeftButtonDown()) {
			// Command units to go to the mouse position
			// calculated from the camera position and the width
			float rx = display.getMouseX() * arena.getWidth() / 2;
			float ry = display.getMouseY() * arena.getHeight() / 2;
			float x = cx + rx;
			float y = cy + ry;
			command(x, y);
		}
		if (display.isLeftButtonLifted()) {
			stop(); // Stop units
		}
		
		if (display.isRightButtonPressed()) {
			System.out.println("NECROMANCE!");
		}
	}
}
