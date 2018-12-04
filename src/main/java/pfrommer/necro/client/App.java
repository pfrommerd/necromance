package pfrommer.necro.client;

import java.io.IOException;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.SpawnManager;
import pfrommer.necro.net.Client;
import pfrommer.necro.net.Parsers;
import pfrommer.necro.net.Server;
import pfrommer.necro.util.Display;
import pfrommer.necro.util.Point;
import pfrommer.necro.util.Renderer;

public class App {
	public static final float ARENA_WIDTH = 200;
	public static final float ARENA_HEIGHT = 200;
	public static final float CAMERA_PER_INCH = 3f;	

	private boolean host;
	private String hostname;
	private int port;
	
	private Arena clientArena;
	
	// The internal server
	// only set if not hosting
	private Arena serverArena;
	private Server server;
	private SpawnManager spawner;

	private Client client;
	private LocalController input;
	private long playerID;

	// Nothing should happen in the constructor
	public App(boolean host, String hostname, int port) {
		this.host = host;
		this.hostname = hostname;
		this.port = port;
		
		// Register all the parsers
		Parsers.registerAll();
	}
	
	public void create(Display display) {
		clientArena = new Arena(ARENA_WIDTH, ARENA_HEIGHT);
		if (host) serverArena = new Arena(ARENA_WIDTH, ARENA_HEIGHT);
		if (host) server = new Server(serverArena, hostname, port);
		
		client = new Client(hostname, port);
		
		if (host) {
			spawner = new SpawnManager(serverArena);
			spawner.addBot();
			spawner.addBot();
			spawner.addBot();
			spawner.addBot();
		}
		
		// Update the client arena from the server
		client.addListener(clientArena);
		
		try {
			if (host) server.open();
			client.open();
			if (host) server.read(); // Process the client connection
			if (host) server.write();
			// ready the client id
			playerID = client.waitForID();
		} catch (IOException e) { e.printStackTrace(); }

		input = new LocalController(clientArena, playerID);
		input.addListener(client);

		clientArena.setBackground("files/background.png");
	}

	public void resize (Display display, int width, int height) {
		
	}

	public void render (Display display, Renderer r, float dt) {
		try {
			// Write out any server updates to the
			// client
			if (host) server.write();
			client.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		float cameraWidth = CAMERA_PER_INCH *  display.getWidth()  / display.getDensity();
		float cameraHeight = CAMERA_PER_INCH * display.getHeight() / display.getDensity();
		
		Point cameraPos = clientArena.calcCameraPos(playerID, cameraWidth, cameraHeight);
		r.orthoCamera(cameraPos.getX(), cameraPos.getY(), cameraWidth, cameraHeight);
		
		clientArena.render(r, dt);
		
		// Handle the local controller
		input.update(display, cameraPos.getX(), cameraPos.getY(),
							  cameraWidth, cameraHeight, dt);

		try {
			client.write();
			if (host) server.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Update the server
		if (host) {
			serverArena.update(dt);
			spawner.update(dt); // Spawn in new things if units have died
		}
	}

	public void pause(Display display) {
	}

	public void resume(Display display) {
		
	}

	public void dispose(Display display) {
	}
}
