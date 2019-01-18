package pfrommer.necro.game;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import pfrommer.necro.net.Client;
import pfrommer.necro.util.Color;
import pfrommer.necro.util.Display;
import pfrommer.necro.util.Point;
import pfrommer.necro.util.Renderer;
import pfrommer.necro.util.Renderer.TextMode;
import pfrommer.necro.util.Sprite;

public class App {
	public static final float CAMERA_DIMENSION = 50f;	
	
	public static final Sprite BACKGROUND_SPRITE = Sprite.load("background.png");
	
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
			client = null; // So we don't try to read further
			JOptionPane.showMessageDialog(null, "Disconnected!");
		}
		
		// Render
		
		float cameraAspect = CAMERA_DIMENSION / Math.min(display.getWidth(), display.getHeight());
		float cameraWidth = cameraAspect *  display.getWidth();
		float cameraHeight = cameraAspect * display.getHeight();
		
		Point cameraPos = arena.calcCameraPos(playerID, cameraWidth, cameraHeight);
		r.orthoCamera(cameraPos.getX(), cameraPos.getY(), cameraWidth, cameraHeight);
		
		// Draw background
		r.drawImage(BACKGROUND_SPRITE, 0, 0, arena.getWidth(), arena.getHeight());
		
		// Draw the arena
		arena.render(r, dt);
		
		// Handle the local controller
		input.update(display, cameraPos.getX(), cameraPos.getY(),
							  cameraWidth, cameraHeight, dt);

		// Draw the text
		r.resetCamera();
		r.drawText(1, 1, 0.05f, new Color(1, 1, 1),
					TextMode.TOP_RIGHT, "Units: " + input.getUnits().size() +
									   " Necromanceable: " + input.getNecromanceable().size());
		
		try {
			// Write any events as a result of the controller
			if (client != null) client.write();
		} catch (IOException e) {
			client = null; // So we don't try to read further
			JOptionPane.showMessageDialog(null, "Disconnected!");
		}
	}
	

	public static class LocalController extends Controller {
		private boolean justDied = false;
		
		public LocalController(Arena arena, long playerID) {
			super(arena, playerID);
			arena.addListener(new EventListener() {
				@Override
				public void handleEvent(Event e) {
					// If we have some reason to check the player
					// health status....
					if (!((e instanceof Arena.EntityRemoved) ||
						 (e instanceof Unit.OwnerChange))) return;
					if (arena.getLivingPlayerUnits(playerID).size() == 0 && !justDied) {
						// The horde associated with this player has changed,
						// show a dialog on death
						// We need to queue this for later or it gets weird
						// if we are using the swing backend
						// and this is in the running thread
						justDied = true;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								JOptionPane.showMessageDialog(null,
									"You died!\nWhat an accomplishment!\nTry again with some more units.");
							}
						});
					}
				}
			});
		}
		
		// We need more things, so ignore this for now
		public void update(float dt) {}
		
		public void update(Display display, float cx, float cy,
											float cw, float ch,
											float dt) {
			if (getUnits().size() > 0) justDied = false; // We're alive again
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
