package pfrommer.necro.game;

import pfrommer.necro.net.Protocol.Message;

public abstract class Unit extends Entity {
	public enum State { PROTECTED, ENGAGED, READY, DEAD }
		
	private float x, y;
	private float health;
	private State state;
	
	private Player owner;
	
	public Unit(long id, float x, float y,
				float health, State state, Player owner) {
		super(id);
		this.x = x;
		this.y = y;
		this.health = health;
		this.owner = owner;
		this.state = state;
	}

	@Override
	public float getX() { return x; }
	@Override
	public float getY() { return y; }
	
	public Player getOwner() { return owner; }
	public State getState() { return this.state; }
	
	// These all fire events in addition to
	// mutating the state

	protected void move(float x, float y) {
		this.x = x;
		this.y = y;
		
		fireEvent(new UnitMoved(this, x, y));
	}
	
	protected void damage(float damage) {
		health -= damage;
		// If health is negative, notify both that the
		// health changed and the unit state changed
		if (health <= 0) {
			health = 0;
			state = State.DEAD;
		}
	}

	protected void changeOwner(Player o) {
		owner = o;
	}
	
	// All the unit-related events
	
	public static class UnitMoved extends Event {
		private Unit unit;
		private float x;
		private float y;
		
		public UnitMoved(Unit u, float x, float y) {
			this.unit = u;
			this.x = x;
			this.y = y;
		}
		
		@Override
		public void apply(Arena a) {
			unit.move(x, y);
		}

		@Override
		public void pack(Message.Builder msg) {
		}
	}
}
