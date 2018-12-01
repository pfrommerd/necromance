package pfrommer.necro.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Event;
import pfrommer.necro.game.EventListener;
import pfrommer.necro.game.EventParser;
import pfrommer.necro.game.Player;

public class ClientHandler implements EventListener {
	private Arena arena;
	private Server server;
	private SocketChannel client;
	
	private Player player;
	
	// For parsing the current message
	private ByteBuffer header = ByteBuffer.allocate(4);
	private ByteBuffer message = null; // Not null if in message parsing
	
	private Queue<Event> outbound = new LinkedList<Event>();
	
	public ClientHandler(Arena a, Server s, SocketChannel c) {
		this.arena = a;
		this.server = s;
		this.client = c;
	}
	
	// When an event occurrs in-game
	// add to the outbound queue to
	// be written on the next handle()
	public void onEvent(Event e) {
		outbound.add(e);
	}
		
	private void onMessage(Protocol.Message msg) {
		// Convert to an event
		for (Protocol.Event e : msg.getEventsList()) {
			EventParser p = EventParser.getParser(e.getType());
			if (p == null) {
				throw new IllegalArgumentException("Could not find parser for type" + e.getType());
			}
			Event event = p.parse(e);
			if (event == null) {
				throw new IllegalArgumentException("Failed to parse event!");
			}
			
			// Handle the special case of a connect
			// message that we need to handle separately
			if (event instanceof Connect) {
				Connect c = (Connect) event;
				String name = c.getName();
				if (!arena.hasPlayer(name)) {
					// Add a player
					player = new Player(server.nextID(), name);
					// send back an ack
					outbound.add(new Connected(true, player.getID()));
					// then add to the arena
					arena.addPlayer(player);
				} else {
					// Send back a nack, needs another username
					outbound.add(new Connected(false, 0));
				}
			} else {
				event.apply(arena);
			}
		}
		
	}
	
	// Handle low-level IO interaction with the server/sockets
	
	public void sendData() throws IOException {
		// Go through the outbound queue
		// and write everything as a big message
		if (outbound.isEmpty()) return;
		Protocol.Message.Builder builder = Protocol.Message.newBuilder();
		
		Event e = null;
		while ((e = outbound.poll()) != null) {
			e.pack(builder);
		}
		// send the message
		Protocol.Message msg  = builder.build();
		byte[] data = msg.toByteArray();
		ByteBuffer b = ByteBuffer.allocate(data.length + 4);
		b.putInt(data.length);
		b.put(data);
		b.rewind();
		
		// Write the byte buffer
		client.write(b);
	}
	
	public void receivedData(ByteBuffer data) throws IOException {
		if (header.remaining() > 0) {
			// Don't try and take more than the
			// bytes left to read in the header
			int lim = data.limit();
			data.limit(Math.min(lim, header.remaining()));
			header.put(data); // This will advance the data postition
			// Reset the limit to what it was before
			data.limit(lim);
			
			// Check if we are done with the header
			if (header.remaining() == 0) {
				int length = header.getInt();
				message = ByteBuffer.allocate(length);
			}
		}
		// If we haven't finished with the header still, return
		if (message == null) return;
		
		// Limit the data to the remaining message bytes
		// (+ any header bytes already read from the data buffer)
		// but don't make it any larger than the current limit of the buffer
		data.limit(Math.min(data.limit(),
							data.position() + message.remaining()));
		
		// Add the data to the message
		message.put(data);
		
		// Check if we are done
		if (message.remaining() == 0) {
			// Now parse the buffer and reset
			// the message and header states
			Protocol.Message m = Protocol.Message.parseFrom(message.array());
			onMessage(m);
			
			message = null;
			header.rewind();
		}
	}
}
