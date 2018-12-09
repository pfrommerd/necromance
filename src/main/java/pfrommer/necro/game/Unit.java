package pfrommer.necro.game;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Event.Builder;
import pfrommer.necro.net.Protocol.Event.TypeCase;
import pfrommer.necro.util.Circle;
import pfrommer.necro.util.Renderer;

public abstract class Unit extends Entity {	
	public static final float UNIT_DAMAGE_ANIMATION_TIME = 0.5f;
	public static final float UNIT_BASE_SPEED_ANIMATION = 1f;
	
	// These are passed down from the subtype
	private float attackRadius;
	private float attackCooldown;
	private float maxHealth;
	private float maxSpeed;
	
	// Mutating state
	private long ownerID;
	// The original horde owner ID (must be dead to necromance)
	private long hordeID; // For checking when we should resurrect

	private long targetID; // Entity ID of the current target (negative if none)
	
	private float theta; // The direction the unit is facing in radians, with 0 being to the right
	private float speed; // Speed the unit is moving
	
	private float health;
	
	// Time remaining on the damage animation
	// This variable is not synchronized
	// across the different computers
	private float damageAnimation = 0f; 
	// Time elapsed on the walk animation
	private float walkAnimation = 0f; 
	// Time elapsed on the attack animation
	private float attackAnimation = 0f;
	
	private float attackRemaining = 0f; // Seconds until attack
	
	public Unit(long id,
				long ownerID, long hordeID, long targetID,
				float x, float y,  // coordinates
				float theta,
				float speed, float health,
				float maxSpeed, float maxHealth,
				float attackRadius, float attackCooldown) {
		super(id, x, y);

		this.ownerID = ownerID;
		this.hordeID = hordeID;
		this.targetID = targetID;
		this.theta = theta;
		this.speed = speed;
		this.maxSpeed = maxSpeed;
		this.health = health;
		this.maxHealth = maxHealth;
		
		this.attackRadius = attackRadius;
		this.attackCooldown = attackCooldown;
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
	
	protected Set<Unit> getTargetsWithin(float radius) {
		if (getCollider() == null) return Collections.emptySet();
		
		Circle c = getCollider();
		Set<Unit> s = new HashSet<>();
		for (Entity e : getArena().getEntities()) {
			if (e instanceof Unit) {
				Unit u = (Unit) e;
				if (u.getCollider() == null) continue;
				if (u.getOwner() == ownerID ||
						u.getOwner() < 0 && ownerID < 0 || // If two bot players (bots have - player IDs), ignore each other
						u.getHealth() <= 0)
						continue;
				float distance = c.distanceTo(u.getCollider());
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
	
	protected void damage(long attackingPlayer, float damage) {
		health -= damage;
		if (health < 0) {
			health = 0;
		}
		fireEvent(new Damage(getID(), attackingPlayer, damage));
		
		if (health == 0) {
			changeOwner(attackingPlayer);
			// Horde owner stays the same (until necromance is called!)
		}
		
		// Reset the damage animation
		damageAnimation = UNIT_DAMAGE_ANIMATION_TIME;
	}

	protected void necromance() {
		if (health <= 0 && !getArena().isHordeAlive(hordeID)) {
			// All of them are dead, now:
			// NECROMANCE!
			// reset the movement
			run(theta, 0);
			move(x, y); // in case we spawned inside someone
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
			fireEvent(new Necromance(getID()));
		}
	}
	
	protected void target(Unit target) {
		if (target != null) this.targetID = target.getID();
		else this.targetID = -1;
		fireEvent(new Target(getID(), targetID));
	}

	// Handles the rendering animations for the unit
	@Override
	public void render(Renderer r, float dt) {
		if (getHealth() <= 0) {
			// Reset out any animations;
			damageAnimation = 0;
			walkAnimation = 0;
			attackAnimation = 0;
		} else {
			// Run the animations
			if (damageAnimation > 0) {
				damageAnimation -= dt;
			}
			if (speed > 0) {
				walkAnimation += dt * (speed + UNIT_BASE_SPEED_ANIMATION);
			} else {
				walkAnimation = 0;
			}
			if (targetID >= 0) {
				attackAnimation += dt;
			} else {
				attackAnimation = 0;
			}
		}
		// Draw the unit
		// Theta is not going to wrap around
		// so this is safe
		boolean rightDir = theta >= -(float) Math.PI / 2f &&
						   theta <= (float) Math.PI / 2f;		
		if (getTarget() > 0) {
			Entity e = getArena().getEntity(getTarget());
			if (e != null) {
				rightDir = e.getX() - getX() > 0;
			}
		}
		
		renderUnit(r,
			1 - damageAnimation / UNIT_DAMAGE_ANIMATION_TIME,
			walkAnimation, attackAnimation,
				rightDir, dt);
	}
	
	@Override
	public void update(float dt) {
		if (health <= 0) return;
		
		Set<Unit> targets = getTargetsWithin(attackRadius);
		
		if (targetID >= 0 &&
				!targets.contains(getArena().getEntity(targetID))) {
			target(null); // Reset targeting and cooldown
		}
		
		if (targetID < 0 && targets.size() > 0) {
			target(targets.iterator().next());
		}
		
		if (targetID >= 0 && attackRemaining <= 0) {
			attackRemaining = attackCooldown;
			Unit enemy = (Unit) getArena().getEntity(targetID);
			launchAttack(enemy);
		} else if (targetID >= 0 && attackRemaining > 0) {
			attackRemaining -= dt;
		} else if (targetID <= 0 && attackRemaining < attackCooldown){
			attackRemaining += dt;
			if (attackRemaining > attackCooldown)
				attackRemaining = attackCooldown;
		}
		
		if (speed > 0) {
			float dx = dt * speed * (float) Math.cos(theta);
			float dy = dt * speed * (float) Math.sin(theta);
			move(getX() + dx, getY() + dy);
		}
	}
	
	// For the subtype when we actually attack
	public abstract void launchAttack(Unit enemy);
	
	public abstract void renderUnit(Renderer r,
							float damageAnimation,
							float walkingAnimation,
							float attackAnimation,
							boolean faceRight, float dt);
	
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
			u.damage(attackerID, damage);
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
