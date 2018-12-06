package pfrommer.necro.game;

import java.io.IOException;

import javax.swing.JOptionPane;

import pfrommer.necro.net.Client;
import pfrommer.necro.util.Display;
import pfrommer.necro.util.Point;
import pfrommer.necro.util.Renderer;

public class App {
	public static final float ARENA_WIDTH = 200;
	public static final float ARENA_HEIGHT = 200;
	public static final float CAMERA_DIMENSION = 50f;	
	
	private Arena arena;
	private Client client;
	private LocalController input;
	private long playerID;

	// Nothing should happen in the constructor
	// besides take a client
	public App(Display display, Client client) {
		if (client == null)
			throw new IllegalArgumentException();
		
		this.client = client;
		this.arena = new Arena();
		
		arena.setBackground("background.png");

		// Update the client arena from the client events
		client.addListener(arena);
		
		try {
			// read the client id
			playerID = client.waitForID();
			// read the arena information
		} catch (IOException e) { e.printStackTrace(); }

		// have the controller push events to the client
		input = new LocalController(arena, playerID);
		input.addListener(client);

	}


	public void render (Display display, Renderer r, float dt) {
		try {
			// Read any updates
			if (client != null) client.read();
		} catch (IOException e) {
			// Assume the client disconnected
			JOptionPane.showMessageDialog(null, "Disconnected!");
			client = null; // So we don't try to read further
		}
		
		// Render
		
		float cameraAspect = CAMERA_DIMENSION / Math.min(display.getWidth(), display.getHeight());
		float cameraWidth = cameraAspect *  display.getWidth();
		float cameraHeight = cameraAspect * display.getHeight();
		
		Point cameraPos = arena.calcCameraPos(playerID, cameraWidth, cameraHeight);
		r.orthoCamera(cameraPos.getX(), cameraPos.getY(), cameraWidth, cameraHeight);
		
		// Draw the arena
		arena.render(r, dt);
		
		// Handle the local controller
		input.update(display, cameraPos.getX(), cameraPos.getY(),
							  cameraWidth, cameraHeight, dt);

		try {
			// Write any events as a result of the controller
			if (client != null) client.write();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Disconnected!");
			client = null; // So we don't try to read further
		}
	}
	
	public static class LocalController extends Controller {	
		public LocalController(Arena arena, long playerID) {
			super(arena, playerID);
		}
		
		// We need more things, so ignore this for now
		public void update(float dt) {}
		
		public void update(Display display, float cx, float cy,
											float cw, float ch,
											float dt) {
			// Check if mouse is down and if so, where it is
			if (display.isLeftButtonDown()) {
				// Command units to go to the mouse position
				// calculated from the camera position and the width
				float rx = display.getMouseX() * cw / 2;
				float ry = display.getMouseY() * ch / 2;
				float x = cx + rx;
				float y = cy + ry;
				command(x, y);
			}
			
			if (display.isLeftButtonLifted()) {
				stop(); // Stop units
			}
			
			if (display.isRightButtonPressed()) {
				necromance();
			}
		}
	}
}
