package pfrommer.necro.game;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Event.Builder;
import pfrommer.necro.net.Protocol.Event.TypeCase;

public abstract class Unit extends Entity {	
	private float maxSpeed; // Maximum speed the unit can go
	private float maxHealth;
	
	// Mutating state
	private long ownerID;
	// The original horde owner ID (must be dead to necromance)
	private long hordeID; // For checking when we should resurrect

	private long targetID; // Entity ID of the current target (negative if none)
	
	private float theta; // The direction the unit is facing in radians, with 0 being to the right
	private float speed; // Speed the unit is moving
	
	private float health;
		
	public Unit(long id,
				long ownerID, long hordeID, long targetID,
				float x, float y,  // coordinates
				float theta, float speed, float maxSpeed, // movement
				float health, float maxHealth) {
		super(id, x, y);

		this.ownerID = ownerID;
		this.hordeID = hordeID;
		this.targetID = targetID;
		this.theta = theta;
		this.speed = speed;
		this.maxSpeed = maxSpeed;
		this.health = health;
		this.maxHealth = maxHealth;
	}

	@Override
	public void setArena(Arena a) {
		if (getArena() != null) {
			getArena().deregisterOwner(this, ownerID);
			getArena().deregisterHorde(this, hordeID);
		}
		super.setArena(a);
		if (a != null) {
			a.registerOwner(this, ownerID);
			a.registerHorde(this, hordeID);
		}
	}
	
	public long getOwner() { return ownerID; }
	public long getHorde() { return hordeID; }
	
	public long getTarget() { return targetID; }
	
	public float getTheta() { return theta; }
	public float getSpeed() { return speed; }
	
	public float getHealth() { return health; }

	public float getMaxSpeed() { return maxSpeed; }
	public float getMaxHealth() { return maxHealth; }
	
	// Should be overridden by subtypes
	// if they want to do special graphics stuff on certain events
	// getting triggered
	protected void onDamage(float damage) {}

	protected Set<Unit> getTargetsWithin(float radius) {
		if (getCollider() == null) return Collections.emptySet();
		
		Set<Unit> s = new HashSet<>();
		for (Entity e : getArena().getEntities()) {
			if (e instanceof Unit) {
				Unit u = (Unit) e;
				if (u.getCollider() == null) continue;
				if (u.getOwner() == ownerID ||
						u.getOwner() < 0 && ownerID < 0 || // If two bot players (bots have - player IDs)
						u.getHealth() <= 0)
						continue;
				float distance = getCollider().distanceTo(u.getCollider());
				if (distance < radius) {
					s.add((Unit)e);
				}
			}
		}
		return s;
	}
	
	// These all fire events in addition to
	// mutating the state
	protected void changeOwner(long newOwner) {
		long oldOwner = ownerID;
		if (getArena() != null) {
			getArena().deregisterOwner(this, oldOwner);
			getArena().registerOwner(this, newOwner);
		}
		this.ownerID = newOwner;
		// Fire an event
		fireEvent(new OwnerChange(getID(), newOwner));
	}

	protected void run(float theta, float speed) {
		if (speed != 0) this.theta = theta;
		this.speed = Math.min(speed, maxSpeed);

		fireEvent(new Run(getID(), theta, this.speed));
	}
	
	protected void damage(Unit attacker, float damage) {
		health -= damage;
		if (health < 0) {
			health = 0;
		}
		
		onDamage(damage); // Notify subtypes for rendering
		fireEvent(new Damage(getID(), attacker.getID(), damage));
		
		if (health == 0) {
			changeOwner(attacker.getOwner());
			// Horde owner stays the same (until necromance is called!)
		}
	}
	
	protected void necromance() {
		if (health <= 0 && !getArena().isHordeAlive(hordeID)) {
			// All of them are dead, now:
			// NECROMANCE!
			
			// Get any living units for this user
			// and necromance so that they have the same ID
			long newHordeID =  getArena().getPrimaryHorde(ownerID);
			if (newHordeID < 0) return;
			getArena().deregisterHorde(this, hordeID);
			hordeID = newHordeID;
			getArena().registerHorde(this, newHordeID);
			
			// reset the health
			health = maxHealth;
			targetID = -1;
			
			move(this.x, this.y);
			
			fireEvent(new Necromance(getID()));
		}
	}
	
	protected void target(Unit target) {
		if (target != null) this.targetID = target.getID();
		else this.targetID = -1;

		fireEvent(new Target(getID(), targetID));
	}
	
	
	// Creates events for these units
	// without mutating the state (for the controllers)
	
	public Event createRunCommand(float theta, float speed) {
		return new Run(getID(), theta, speed);
	}
	
	public Event createNecromanceCommand() {
		return new Necromance(getID());
	}
	
	// All the unit-related events
	// event for owner change
	public static class OwnerChange extends Event {
		private long unitID;
		private long newOwner;
		
		public OwnerChange(long id, long no) {
			this.unitID = id;
			this.newOwner = no;
		}
		
		@Override
		public void apply(Arena a) {
			Entity e = a.getEntity(unitID);
			((Unit) e).changeOwner(newOwner);
		}
		
		@Override
		public void pack(Protocol.Event.Builder msg) {
			msg.getOwnerChangeBuilder().setId(unitID)
									   .setNewOwner(newOwner);
		}
		public static void register() {
			Event.Parser.add(TypeCase.OWNERCHANGE, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					Protocol.OwnerChange o = e.getOwnerChange();
					return new OwnerChange(o.getId(), o.getNewOwner());
				}
			});
		}
	}
	
	public static class Damage extends Event {
		private long id;
		private long attackerID;
		private float damage;
		
		public Damage(long id, long attackerID, float damage) {
			this.id = id;
			this.attackerID = attackerID;
			this.damage = damage;
		}
		
		@Override
		public void apply(Arena a) {
			Unit u = (Unit) a.getEntity(id);
			Unit b = (Unit) a.getEntity(attackerID);
			u.damage(b, damage);
		}

		@Override
		public void pack(Builder msg) {
			msg.getDamageBuilder().setId(id)
					.setAttacker(attackerID)
					.setDamage(damage);
		}
		
		public static void register() {
			Event.Parser.add(TypeCase.DAMAGE, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					Protocol.Damage r = e.getDamage();
					return new Damage(r.getId(), r.getAttacker(), r.getDamage());
				}
			});
		}
	}
	
	public static class Target extends Event {
		private long id;
		private long targetID;
		
		public Target(long id, long targetID) {
			this.id = id;
			this.targetID = targetID;
		}
		
		@Override
		public void apply(Arena a) {
			Unit u = (Unit) a.getEntity(id);
			Unit t = (Unit) a.getEntity(targetID);
			u.target(t);
		}

		@Override
		public void pack(Builder msg) {
			msg.getTargetBuilder().setId(id).setTargetID(targetID);
		}
		
		public static void register() {
			Event.Parser.add(TypeCase.TARGET, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					Protocol.Target t = e.getTarget();
					return new Target(t.getId(), t.getTargetID());
				}
			});
		}
	}
	
	public static class Necromance extends Event {
		private long id;
		
		public Necromance(long id) {
			this.id = id;
		}
		
		@Override
		public void apply(Arena a) {
			Entity e = a.getEntity(id);
			if (e == null || !(e instanceof Unit)) return;
			Unit u = (Unit) e;
			u.necromance();
		}

		@Override
		public void pack(Builder msg) {
			msg.getNecromanceBuilder().setId(id);
		}
		
		public static void register() {
			Event.Parser.add(TypeCase.NECROMANCE, new Event.Parser() {
				@Override
				public Event unpack(Protocol.Event e) {
					Protocol.Necromance r = e.getNecromance();
					return new Necromance(r.getId());
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
			Entity e = a.getEntity(id);
			if (e == null || !(e instanceof Unit)) return;
			((Unit) e).run(theta, speed);
		}

		@Override
		public void pack(Builder msg) {
			msg.getRunBuilder().setId(id).setTheta(theta).setSpeed(speed);
		}
		
		public static void register() {
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
