package pfrommer.necro.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map.Entry;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Event;
import pfrommer.necro.game.EventListener;
import pfrommer.necro.game.SpawnManager;

public class Server implements EventListener, Runnable {
	public static final int SERVER_REFRESH_RATE = 60; // Try and process events about 60 times
													  // a second, meaning way 1000/60 ms between
													  // handling events
	
	private String host;
	private int port;
	
	private ServerSocketChannel socket;

	private HashMap<SocketChannel, ClientHandler> clientHandlerMap = new HashMap<>();
	
	// For creating players/entities
	private Arena arena;
	private SpawnManager spawner;
	
	public Server(Arena arena, SpawnManager spawner, 
					String host, int port) {
		if (host == null) throw new IllegalArgumentException();
		this.arena = arena;
		this.spawner = spawner;
		this.host = host;
		this.port = port;
		
		// Add a listener to the arena 
		this.arena.addListener(this);
		
		// Register all the parsers for the server
		Parsers.registerAll();
	}
	
	
	@Override
	public void handleEvent(Event e) {
		for (ClientHandler c : this.clientHandlerMap.values()) {
			c.handleEvent(e);
		}
	}
	
	public void open() throws IOException {
		socket = ServerSocketChannel.open();
		InetSocketAddress addr = new InetSocketAddress(host, port);
		socket.bind(addr);
		socket.configureBlocking(false);
	}
	
	public void close() throws IOException {
		if (socket != null) socket.close();
	}
	
	// Write out any queued events
	public void write() throws IOException {
		for (ClientHandler h : clientHandlerMap.values()) {
			h.sendEvents();
		}
	}
	
	// Handle any new clients, incoming events, etc.
	public void read() throws IOException {
		if (socket == null)
			throw new IOException("Socket not open!");
		// Read new commands and populate updates
		SocketChannel client = null;
		while ((client = socket.accept()) != null) {
			// We got a new connection!
			client.configureBlocking(false);
			// Add a string builder for this client
			clientHandlerMap.put(client, new ClientHandler(arena, client));
		}

		for (Entry<SocketChannel, ClientHandler> e : clientHandlerMap.entrySet()) {
			// We received some bytes!
			client = e.getKey();
			ClientHandler handler = e.getValue();
			if (handler == null)
				throw new IOException("Unknown client!");
			if (!client.isConnected()) {
				handler.disconnect();
				clientHandlerMap.remove(client);
			} else {
				handler.receiveEvents();
			}
		}
	}

	// Will run the server in a standalone
	// settting
	@Override
	public void run() {
		try {						
			long time = System.currentTimeMillis();
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(1000/SERVER_REFRESH_RATE);
				} catch (InterruptedException e)  {} // Will be handled by !interrupted
				
				read();
				
				// Move the clock
				long currentTime = System.currentTimeMillis();
				float dt = (currentTime - time) / 1000f;
				time = currentTime;
				if (arena != null) arena.update(dt);
				if (spawner != null) spawner.update(dt);
				
				// Write out the events
				write();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Received an exception, exiting server...");
		} finally {
			try {
				close();
			} catch (IOException e) {} // We don't care if there is an exception
		}
	}
}
