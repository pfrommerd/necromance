package pfrommer.necro.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IOManager {
	private SocketChannel channel;
	
	private ByteBuffer header = ByteBuffer.allocate(4);
	private ByteBuffer message = null;
	private ByteBuffer data = ByteBuffer.allocate(256);
	
	public IOManager(SocketChannel c) {
		channel = c;
	}
	
	public void write(ByteBuffer msg) throws IOException {
		byte[] data = msg.array();
		ByteBuffer m = ByteBuffer.allocate(4 + data.length);
		m.putInt(data.length);
		m.put(data);
		m.rewind();
		channel.write(m);
	}
	
	public ByteBuffer read() throws IOException {
		while (channel.read(data) > 0) {
			int bytes = data.position();
			data.rewind();
			
			if (header.remaining() > 0) {
				data.limit(Math.min(bytes, header.remaining()));
				header.put(data); // This will advance the data postition
				// Check if we are done with the header
				if (header.remaining() == 0) {
					header.rewind();
					int length = header.getInt();
					message = ByteBuffer.allocate(length);
				}
			}
			
			// If we haven't finished with the header still, try and read again
			if (message == null) {
				data.rewind();
				data.limit(data.capacity());
				continue;
			}
			
			// Limit the data to the remaining message bytes
			// (+ any header bytes already read from the data buffer)
			// but don't make it any larger than the current limit of the buffer
			data.limit(Math.min(bytes,
								data.position() + message.remaining()));
			
			// Add the data to the message
			message.put(data);
			
			// rewind the data for the next read
			data.rewind();
			data.limit(data.capacity());
			
			// Check if we are done
			if (message.remaining() == 0) {
				ByteBuffer m = message;
				m.rewind();
				message = null;
				header.rewind();
				// return the message
				return m;
			}
		}
		return null;
	}
}
