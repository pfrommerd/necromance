package pfrommer.necro.game;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Event.Builder;
import pfrommer.necro.net.Protocol.Event.TypeCase;

public abstract class Unit extends Entity {		
	private float x, y; // x, y of the unit base
	
	private float theta; // The direction the unit is facing in radians, with 0 being to the right
	private float speed; // Speed the unit is moving
	
	private float maxSpeed; // Maximum speed the unit can go
	
	private float health;
		
	private long ownerID;
	
	public Unit(long id,
				long ownerID,
				float x, float y, // coordinates
				float theta, float speed, float maxSpeed, // movement
				float health) {
		super(id);

		this.ownerID = ownerID;
		this.x = x;
		this.y = y;
		this.theta = theta;
		this.speed = speed;
		this.maxSpeed = maxSpeed;
		this.health = health;
	}

	@Override
	public float getX() { return x; }
	@Override
	public float getY() { return y; }
	
	public float getTheta() { return theta; }
	public float getSpeed() { return speed; }
	public float getMaxSpeed() { return maxSpeed; }
	
	public float getHealth() { return health; }
	
	public long getOwner() { return ownerID; }
	
	// These all fire events in addition to
	// mutating the state
	protected void move(float x, float y) {		
		float ox = this.x;
		float oy = this.y;
		
		this.x = x;
		this.y = y;
		
		// Check collision!
		/*if (getArena() != null) {
			for (Entity e : getArena().getEntities()) {
				if (e != this && e.getCollider().intersects(getCollider())) {
					// We need to go back because of an intersection!
					this.x = ox;
					this.y = oy;
					break;
				}
			}
		}*/
		
		fireEvent(new Moved(getID(), this.x, this.y));
	}
	
	protected void run(float theta, float speed) {
		if (speed != 0) this.theta = theta;
		this.speed = Math.min(speed, maxSpeed);

		fireEvent(new Run(getID(), theta, this.speed));
	}
	
	protected void damage(Unit attacker, float damage) {
		health -= damage;
		
		// If health is negative, notify both that the
		// health changed and the unit state changed
		if (health <= 0) {
			health = 0;
		}
	}
	
	
	// Creates events for these units
	// without mutating the state (for the controllers)
	public Event createRunCommand(float theta, float speed) {
		return new Run(getID(), theta, speed);
	}
	
	// All the unit-related events

	// for position change
	public static class Moved extends Event {
		private long unitID;
		private float x;
		private float y;
		
		public Moved(long id, float x, float y) {
			this.unitID = id;
			this.x = x;
			this.y = y;
		}
		
		@Override
		public void apply(Arena a) {
			Entity e = a.getEntity(unitID);
			if (e == null) return;
			Unit u = (Unit) e;
			u.move(x, y);
		}

		@Override
		public void pack(Protocol.Event.Builder msg) {
			msg.getMovedBuilder()
				.setId(unitID)
				.setX(x)
				.setY(y);
		}
		
		static {
			Event.Parser.add(TypeCase.MOVED, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					Protocol.Moved m = e.getMoved();
					return new Moved(m.getId(), m.getX(), m.getY());
				}
			});
		}
	}
	
	// for run command
	public static class Run extends Event {
		private long id;
		private float theta, speed;
		public Run(long id, float theta, float speed) {
			this.id = id;
			this.theta = theta;
			this.speed = speed;
		}
		
		@Override
		public void apply(Arena a) {
			Unit u = (Unit) a.getEntity(id);
			u.run(theta, speed);
		}

		@Override
		public void pack(Builder msg) {
			msg.getRunBuilder().setId(id).setTheta(theta).setSpeed(speed);
		}
		
		static {
			Event.Parser.add(TypeCase.RUN, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					Protocol.Run r = e.getRun();
					return new Run(r.getId(), r.getTheta(), r.getSpeed());
				}
			});
		}
	}
}
