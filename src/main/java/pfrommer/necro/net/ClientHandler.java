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

public class ClientHandler implements EventListener {
	private Arena arena;
	private Server server;

	// Mnaages sending/receiving packets of data
	private IOManager io;
		
	private Queue<Event> outbound = new LinkedList<Event>();
	
	public ClientHandler(Arena a, Server s, SocketChannel c) {
		this.arena = a;
		this.server = s;
		this.io = new IOManager(c);
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
		io.write(ByteBuffer.wrap(msg.toByteArray()));
	}
	
	public void receiveEvents() throws IOException {
		ByteBuffer buf = null;
		while ((buf = io.read()) != null) {
			Protocol.Message m = Protocol.Message.parseFrom(buf);
			onMessage(m);
		}
	}
}
