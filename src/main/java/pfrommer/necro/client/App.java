package pfrommer.necro.client;

import java.io.IOException;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.SpawnManager;
import pfrommer.necro.net.Client;
import pfrommer.necro.net.Server;
import pfrommer.necro.util.Display;
import pfrommer.necro.util.Point;
import pfrommer.necro.util.Renderer;

public class App {
	public static final float ARENA_WIDTH = 200;
	public static final float ARENA_HEIGHT = 200;
	public static final float CAMERA_WIDTH = 50;
	public static final float CAMERA_HEIGHT = 50;
	

	private Arena clientArena;
	private Arena serverArena;
	
	// The internal server
	// only set if not connected
	private Server server;
	private SpawnManager spawner;

	private Client client;
	private LocalController input;

	// Nothing should happen in the constructor
	public App() {}
	
	public void create(Display display) {
		clientArena = new Arena(ARENA_WIDTH, ARENA_HEIGHT);
		serverArena = new Arena(ARENA_WIDTH, ARENA_HEIGHT);

		server = new Server(serverArena, "localhost", 6000);
		client = new Client("localhost", 6000);
		
		spawner = new SpawnManager(serverArena);
		spawner.addBot();
		spawner.addBot();
		spawner.addBot();
		spawner.addBot();
		
		// Update the client arena from the server
		client.addListener(clientArena);
		
		long playerID = 0;
		try {
			server.open();
			client.open();
			server.read(); // Process the client connection
			server.write();
			// ready the client id
			playerID = client.waitForID();
		} catch (IOException e) { e.printStackTrace(); }

		input = new LocalController(clientArena, playerID);
		input.addListener(client);

		clientArena.setBackground("files/background.png");
	}

	public void resize (Display display, int width, int height) {}

	public void render (Display display, Renderer r, float dt) {
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
		
		clientArena.render(r, dt);
		
		// Handle the local controller
		input.update(display, cameraPos.getX(), cameraPos.getY(),
							  CAMERA_WIDTH, CAMERA_HEIGHT, dt);

		try {
			client.write();
			server.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Update the server
		serverArena.update(dt);
		spawner.update(dt); // Spawn in new things if units have died
	}

	public void pause(Display display) {
	}

	public void resume(Display display) {
		
	}

	public void dispose(Display display) {
	}
}
