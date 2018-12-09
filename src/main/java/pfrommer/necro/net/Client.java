package pfrommer.necro.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Event;
import pfrommer.necro.game.EventListener;
import pfrommer.necro.game.EventProducer;
import pfrommer.necro.net.Protocol.Event.Builder;
import pfrommer.necro.net.Protocol.Event.TypeCase;

public class Client implements EventListener, EventProducer {
	private String host;
	private int port;
	// For reading/writing things
	private IOManager io;
	
	private Queue<Event> outbound = new LinkedList<Event>();
	private HashSet<EventListener> listeners = new HashSet<EventListener>();
	
	public Client(String host, int port) {
		this.host = host;
		this.port = port;
		
		// Register all the parsers
		Parsers.registerAll();
	}
	
	public void addListener(EventListener l) { listeners.add(l); }
	public void removeListener(EventListener l) { listeners.remove(l); }
	
	public void handleEvent(Event e) {
		outbound.add(e);
	}
	
	// Opens the socket and returns the assigned id
	public void open() throws IOException {
		InetSocketAddress addr = new InetSocketAddress(host, port);
		SocketChannel client = SocketChannel.open(addr);
		client.configureBlocking(false);
		io = new IOManager(client);
	}
	
	public long waitForID() throws IOException {
		ByteBuffer buf = null;
		do {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				break;
			}
		} while ((buf = io.read()) == null);
		
		Protocol.Message m = Protocol.Message.parseFrom(buf);
		List<Event> e = Event.unpack(m);
		if (e.size() != 1) throw new IllegalArgumentException("Expected single AssignID");
		Event event = e.get(0);
		if (!(event instanceof AssignID)) throw new IllegalArgumentException("Not an assignid");
		return ((AssignID) event).getID();
	}
	
	public void read() throws IOException {
		ByteBuffer buf = null;
		while ((buf = io.read()) != null) {
			Protocol.Message m = Protocol.Message.parseFrom(buf);
			List<Event> e = Event.unpack(m);
			for (Event event : e) {
				for (EventListener l : listeners) l.handleEvent(event);
			}
		}
	}
	
	// Write any outbound events (commands)
	public void write() throws IOException {
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
	
	// Received by the client upon connecting
	public static class AssignID extends Event {
		private long id;
		
		public AssignID(long id) {
			this.id = id;
		}
		
		public long getID() { return id; }
		
		@Override
		public void apply(Arena a) {}

		@Override
		public void pack(Builder msg) {
			msg.getAssignIDBuilder().setId(id);
		}
		
		public static void register() {
			Event.Parser.add(TypeCase.ASSIGNID, new Event.Parser() {
				@Override
				public Event unpack(pfrommer.necro.net.Protocol.Event e) {
					return new AssignID(e.getAssignID().getId());
				}
			});
		}
	}
}

