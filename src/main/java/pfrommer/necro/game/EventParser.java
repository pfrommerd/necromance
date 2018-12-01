package pfrommer.necro.game;

import java.util.HashMap;

import pfrommer.necro.net.Protocol;

@FunctionalInterface
public interface EventParser {
	public Event parse(Protocol.Event e);
	
	public static void addParser(Protocol.Event.Type t, EventParser p) {
		parsers.put(t, p);
	}
	
	public static EventParser getParser(Protocol.Event.Type t) {
		return parsers.get(t);
	}
	
	public static HashMap<Protocol.Event.Type, EventParser> parsers = new HashMap<>();
}
