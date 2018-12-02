package pfrommer.necro.client;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Controller;
import pfrommer.necro.game.Knight;
import pfrommer.necro.net.Client;
import pfrommer.necro.net.Server;
import pfrommer.necro.util.Display;
import pfrommer.necro.util.Point;
import pfrommer.necro.util.Renderer;

public class App {
	public static final float ARENA_WIDTH = 100;
	public static final float ARENA_HEIGHT = 100;
	public static final float CAMERA_WIDTH = 30;
	public static final float CAMERA_HEIGHT = 30;
	
	private Set<Controller> controllers = new HashSet<>();
	private LocalController input;
	
	private Display display;
	private Arena clientArena;
	private Arena serverArena;
	
	// The internal server
	// only set if not connected
	private Server server;
	private Client client;
		
	// Nothing should happen in the constructor
	public App(Display d) {
		display = d;
	}
	
	public Display getDisplay() { return display; }
	
	public void create () {
		clientArena = new Arena(ARENA_WIDTH, ARENA_HEIGHT);
		serverArena = new Arena(ARENA_WIDTH, ARENA_HEIGHT);
		
		input = new LocalController(clientArena);

		server = new Server(serverArena, "localhost", 6000);
		client = new Client("localhost", 6000);
		
		// Send output commands to the client
		input.addListener(client);

		// Set client arena to get updates from the client
		client.addListener(clientArena);
		
		try {
			server.open();
			client.open();
			server.read(); // Process the client connection
		} catch (IOException e) { e.printStackTrace(); }
		
		
		serverArena.addEntity(new Knight(0, 0, 5f, 5f, 0f, 0f, 10f, 100f));
		clientArena.setBackground("files/background.png");
	}

	public void resize (int width, int height) {}

	public void render (Renderer r, float dt) {
		try {
			// Write out any server updates to the
			// client
			server.write();
			client.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Point cameraPos = clientArena.calcCameraPos(0, CAMERA_WIDTH, CAMERA_HEIGHT);
		r.orthoCamera(cameraPos.getX(), cameraPos.getY(), CAMERA_WIDTH, CAMERA_HEIGHT);
		
		clientArena.render(r);
		
		// Handle the local controller
		input.update(display, cameraPos.getX(), cameraPos.getY(), dt);

		try {
			client.write();
			server.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Run all of the controllers
		// for both the client and (maybe) the server
		for (Controller c : controllers) c.update(dt);
		
		// Update on the server
		serverArena.update(dt);
	}

	public void pause() {
	}

	public void resume() {
		
	}

	public void dispose() {
	}
}
