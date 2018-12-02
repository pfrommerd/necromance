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
import pfrommer.necro.net.Protocol.Event.Builder;

public class Client implements EventListener {
	private String host;
	private int port;
	// For reading/writing things
	private IOManager io;
	
	private Queue<Event> outbound = new LinkedList<Event>();
	private HashSet<EventListener> listeners = new HashSet<EventListener>();
	
	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void addListener(EventListener l) { listeners.add(l); }
	public void removeListener(EventListener l) { listeners.remove(l); }
	
	public void handleEvent(Event e) {
		outbound.add(e);
	}
	
	// Opens the socket
	public void open() throws IOException {
		InetSocketAddress addr = new InetSocketAddress(host, port);
		SocketChannel client = SocketChannel.open(addr);
		client.configureBlocking(false);
		io = new IOManager(client);
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

		@Override
		public void apply(Arena a) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void pack(Builder msg) {
			
		}
		
	}
}

