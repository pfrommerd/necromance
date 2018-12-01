package pfrommer.necro.game;

import pfrommer.necro.net.Protocol;

// We make this an abstract class
// instead of an event since something that is
// not truly an event should not act doubly as
// an event
public abstract class Event {	
	// Applies an event to an arena
	public abstract void apply(Arena a);
	
	// Add to a message
	public abstract void pack(Protocol.Message.Builder msg);
}