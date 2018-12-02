package pfrommer.necro.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pfrommer.necro.net.Protocol;

// We make this an abstract class
// instead of an event since something that is
// not truly an event should not act doubly as
// an event
public abstract class Event {	
	// Applies an event to an arena
	public abstract void apply(Arena a);
	
	// Add to a message
	public abstract void pack(Protocol.Event.Builder msg);
	
	private static HashMap<Protocol.Event.TypeCase, Parser> parsers = new HashMap<>();

	@FunctionalInterface
	public interface Parser {
		public Event unpack(Protocol.Event e);
		
		public static void add(Protocol.Event.TypeCase t, Parser p) {
			parsers.put(t, p);
		}
		
		public static Parser get(Protocol.Event.TypeCase t) {
			return parsers.get(t);
		}
	}
	
	public static List<Event> unpack(Protocol.Message msg) {
		List<Event> l = new ArrayList<Event>();
		for (Protocol.Event e : msg.getEventsList()) {
			Parser p = Parser.get(e.getTypeCase());
			if (p == null) {
				throw new IllegalArgumentException("Could not find parser for type " 
													+ e.getTypeCase());
			}
			Event event = p.unpack(e);
			if (event == null)
				throw new IllegalArgumentException("Failed to parse event!");
			l.add(event);
		}
		return l;
	}
}