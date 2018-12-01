package pfrommer.necro.net;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Event;
import pfrommer.necro.game.EventParser;
import pfrommer.necro.net.Protocol.Event.Type;

public class Connected extends Event {
	private boolean success;
	private long id; 
	
	public Connected(boolean success, long id) {
		this.success = success;
		this.id = id;
	}
	
	public long getID() { return id; }
	public boolean isSuccess() { return success; }
	
	@Override
	public void apply(Arena a) {
		// Do nothing!
		// will be handled separately by the clienthandler/client
	}

	@Override
	public void pack(Protocol.Message.Builder msg) {
		msg.addEventsBuilder().setType(Type.CONNECTED)
							  .getConnectedBuilder()
							  .setSuccess(success)
							  .setId(id);
	}
	
	// add unpacker
	static {
		EventParser.addParser(Protocol.Event.Type.CONNECTED,
				new EventParser() {
			@Override
			public Event parse(Protocol.Event e) {
				if (!e.hasConnected()) throw new IllegalArgumentException("Does not have connected!");
				return new Connected(e.getConnected().getSuccess(),
									 e.getConnected().getId());
			}			
		});
	}
}
