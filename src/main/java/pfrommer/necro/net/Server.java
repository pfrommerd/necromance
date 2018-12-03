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

public class Server implements EventListener {
	private String host;
	private int port;
	
	private ServerSocketChannel socket;

	private HashMap<SocketChannel, ClientHandler> clientHandlerMap = new HashMap<>();
	
	// For creating players/entities
	private Arena arena;
	
	public Server(Arena arena, String host, int port) {
		if (host == null) throw new IllegalArgumentException();
		this.arena = arena;
		this.host = host;
		this.port = port;
		
		// Add a listener to the arena 
		this.arena.addListener(this);
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
			handler.receiveEvents();
		}
	}
}
