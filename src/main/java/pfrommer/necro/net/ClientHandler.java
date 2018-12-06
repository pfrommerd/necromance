package pfrommer.necro.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Event;
import pfrommer.necro.game.EventListener;
import pfrommer.necro.net.Client.AssignID;

public class ClientHandler implements EventListener {
	private Arena arena;
	private long playerID;
	
	// Mnaages sending/receiving packets of data
	private IOManager io;
		
	private Queue<Event> outbound = new LinkedList<Event>();
	
	public ClientHandler(Arena a, SocketChannel c) {
		this.arena = a;
		this.io = new IOManager(c);
		
		// Get a new player ID from the arena
		// for this player
		playerID = a.createPlayer();

		try {
			// Write the assign ID message separately
			Protocol.Message.Builder builder = Protocol.Message.newBuilder();
			(new AssignID(playerID)).pack(builder.addEventsBuilder());			
			io.write(ByteBuffer.wrap(builder.build().toByteArray()));
			
			// Now make one big message containing the entire arena state
			// of all players and all entities
			builder = Protocol.Message.newBuilder();
			arena.createInfo().pack(builder.addEventsBuilder());
			io.write(ByteBuffer.wrap(builder.build().toByteArray()));
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write to the socket channel");
		}
		

	}
	
	public void disconnect() {
		if (io.isClosed()) return;
		arena.removePlayer(playerID);
		try {
			io.close();
		} catch (IOException e) {}
	}
	
	// When an event occurrs in-game
	// add to the outbound queue to
	// be written on the next handle()
	public void handleEvent(Event e) {
		outbound.add(e);
	}
		
	private void onMessage(Protocol.Message msg) throws IOException {
		// Convert to an event
		List<Event> events = Event.unpack(msg);
		for (Event event : events) {
			arena.handleEvent(event);
		}
	}
	
	// Handle low-level IO interaction with the server/sockets
	
	public void sendEvents() throws IOException {
		// Go through the outbound queue
		// and write everything as a big message
		if (outbound.isEmpty()) return;
		Protocol.Message.Builder builder = Protocol.Message.newBuilder();
		
		Event e = null;
		while ((e = outbound.poll()) != null) {
			e.pack(builder.addEventsBuilder());
		}
		// send the message
		Protocol.Message msg  = builder.build();
		// Write the byte buffer
		try {
			io.write(ByteBuffer.wrap(msg.toByteArray()));
		} catch (IOException ex) {
			disconnect();
		}
	}
	
	public void receiveEvents() throws IOException {
		ByteBuffer buf = null;
		try {
			while ((buf = io.read()) != null) {
				Protocol.Message m = Protocol.Message.parseFrom(buf);
				onMessage(m);
			}
		} catch (IOException ex) {
			disconnect();
		}
	}
}
