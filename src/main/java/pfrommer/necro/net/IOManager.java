package pfrommer.necro.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IOManager {
	private SocketChannel channel;
	
	private ByteBuffer header = ByteBuffer.allocate(4);
	private ByteBuffer message = null;
	private ByteBuffer data = ByteBuffer.allocate(256);
	private ByteBuffer processed = data.slice(); // The processed portion of the data buffer
	public IOManager(SocketChannel c) {
		channel = c;
	}
	
	public boolean isClosed() {
		return channel.isConnected();
	}
	
	public void close() throws IOException {
		channel.close();
	}
	
	public void write(ByteBuffer msg) throws IOException {
		byte[] data = msg.array();
		ByteBuffer m = ByteBuffer.allocate(4 + data.length);
		m.putInt(data.length);
		m.put(data);
		m.rewind();
	}
	
	public ByteBuffer read() throws IOException {
		// If we have already processed
		// everything in the data buffer,
		// rewind reverything and reset the limits
		if (data.position() > 0 &&
				data.position() <= processed.position()) {
			data.rewind();
			processed.rewind();
		}
				
		while (processed.position() < data.position() ||
				channel.read(data) > 0) {
			if (header.remaining() > 0) {
				// Limit to either the full amount read into the buffer
				// or up to the remaining bytes in the header
				processed.limit(Math.min(data.position(),
								processed.position() + header.remaining()));
				header.put(processed); // This will advance the processed position
				// Check if we are done with the header
				if (header.remaining() == 0) {
					header.rewind();
					int length = header.getInt();
					if (Math.abs(length) > 100000)
						throw new IllegalArgumentException("Something went terribly wrong");
					message = ByteBuffer.allocate(length);
				}
			}
			// If the header is done
			if (message != null) {
				processed.limit(Math.min(data.position(),
								processed.position() + message.remaining()));
				message.put(processed);
				
				// Check if we are done
				if (message.remaining() == 0) {
					ByteBuffer m = message;
					m.rewind();
					message = null;
					header.rewind();
					return m;
				}
			}

			
			// If we have processed
			// everything in the data buffer read so far,
			// rewind everything and reset the limits
			// allowing new data to be read
			if (data.position() <= processed.position()) {
				data.rewind();
				processed.rewind();
			}
		}
		return null;
	}
}
