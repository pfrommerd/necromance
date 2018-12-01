package pfrommer.necro.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Set;

import pfrommer.necro.game.Arena;

public class Server {
	private String host;
	private int port;
	
	private ServerSocketChannel socket;
	private Selector selector;
	
	private HashMap<SocketChannel, ClientHandler> clientHandlerMap = new HashMap<>();
	
	// For creating players/entities
	private Arena arena;
	private long nextID;
	
	public Server(Arena arena, String host, int port) {
		if (host == null) throw new IllegalArgumentException();
		this.arena = arena;
		this.host = host;
		this.port = port;
		
		// Add a listener to the arena 
	}
	
	public long nextID() { return nextID++; }
	
	public void open() throws IOException {
		selector = Selector.open();
		
		socket = ServerSocketChannel.open();
		InetSocketAddress addr = new InetSocketAddress(host, port);
		socket.bind(addr);
		socket.configureBlocking(false);
		
		int ops = socket.validOps();
		socket.register(selector, ops, null);
	}
	
	// Write out any queued events
	public void write() throws IOException {
		for (ClientHandler h : clientHandlerMap.values()) {
			h.sendData();
		}
	}
	
	// Handle any new clients, incoming events, etc.
	public void read() throws IOException {
		if (socket == null || selector == null)
			throw new IOException("Socket not open!");
		
		// Read new commands and populate updates
		selector.select();
		
		Set<SelectionKey> keys = selector.selectedKeys();
		
		for (SelectionKey k : keys) {
			if (k.isAcceptable()) {
				// We got a new connection!
				SocketChannel client = socket.accept();
				client.configureBlocking(false);
				client.register(selector, SelectionKey.OP_READ);
				// Add a string builder for this client
				clientHandlerMap.put(client, new ClientHandler(arena, this, client));
			} else if (k.isReadable()) {
				// We received some bytes!
				SocketChannel client = (SocketChannel) k.channel();				
				ClientHandler handler = clientHandlerMap.get(client);
				
				ByteBuffer buf = ByteBuffer.allocate(256);
				while (client.read(buf) > 0) {
					// Limit the buffer to what we read
					buf.limit(buf.position());
					buf.rewind(); // Rewind for reading
					
					// Process the buffer
					handler.receivedData(buf);
					
					// Get ready for the next read
					buf.rewind();
					buf.limit(buf.capacity());
				}
			}
		}
	}
}
