package pfrommer.necro.net;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Event;
import pfrommer.necro.game.EventParser;
import pfrommer.necro.net.Protocol.Event.Type;

public class Connect extends Event {
	private String name;
	
	public Connect(String name) {
		this.name = name;
	}
	
	public String getName() { return name; }
	
	@Override
	public void apply(Arena a) {
		// Do nothing!
		// Will be handled separately by the clienthandler/client
	}
	
	@Override
	public void pack(Protocol.Message.Builder msg) {
		msg.addEventsBuilder().setType(Type.CONNECT)
							  .getConnectBuilder()
							  .setName(name);
	}
	
	// add unpacker
	static {
		EventParser.addParser(Type.CONNECT,
				new EventParser() {
			@Override
			public Event parse(Protocol.Event e) {
				if (!e.hasConnect()) throw new IllegalArgumentException("Does not have connect");
				return new Connect(e.getConnect().getName());
			}

		});
	}
}
