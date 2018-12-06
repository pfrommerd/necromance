package pfrommer.necro.game;

import java.util.Set;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Entity.TypeCase;
import pfrommer.necro.util.Circle;
import pfrommer.necro.util.Color;
import pfrommer.necro.util.Renderer;

public class Knight extends Unit {
	public static final float KNIGHT_COLLIDER_RADIUS = 0.8f;
	public static final float KNIGHT_TARGET_RADIUS = 3f; // Fight anyone within this radius of our collider
	
	// When we get speed hampering effects
	public static final float KNIGHT_ATTACK_SPEED_RADIUS = 1f; 
	// The speed cap when engaged too close (to prevent running away units)
	public static final float KNIGHT_ATTACK_SPEED_CAP = 0.8f;
	
	public static final float KNIGHT_ATTACK_DAMAGE = 20f;
	public static final float KNIGHT_ATTACK_COOLDOWN = 2f;

	// animation related things
	// Show red damage for 0.5 seconds
	
	public static final float KNIGHT_DAMAGE_ANIMATION_TIME = 0.5f;
	
	public static final float KNIGHT_WALK_ANIMATION_TIME_FACTOR = 1f; // Mixes in some time-based animation
	public static final float KNIGHT_WALK_ANIMATION_STEP_TIME = 1f;
	
	public static final float KNIGHT_ATTACK_ANIMATION_STEP_TIME = 0.5f;
	
	public static final int	  KNIGHT_WALK_ANIMATION_FRAMES = 8;
	public static final int   KNIGHT_ATTACK_ANIMATION_FRAMES = 4;
	
	// image related things
	
	public static final float KNIGHT_DRAW_WIDTH = 3;
	public static final float KNIGHT_DRAW_HEIGHT = 5;
	public static final float KNIGHT_DRAW_YOFF = 1;
	
	// Local variable that does not get forwarded anywhere else
	// since we only need this where update() is called
	private float attackCooldown = 0f;
	
	// Local variable that is rendering related
	// and not forwarded anywhere
	private float damageAnimation = 0f; // Time remaining on the damage animation
	private float attackAnimation = 0f; // Time elapsed on the attack animation
	private float walkAnimation = 0f; // Time elapsed on the attack animation
	
	// Creates a new, fully healthy knight
	// that is targeting nobody
	public Knight(long id, long ownerID, long hordeID, float x, float y, 
				   float maxSpeed, float maxHealth) {
		this(id, ownerID, hordeID, -1, x, y, 0, 0,
				maxSpeed, maxHealth, maxHealth);
	}
	
	public Knight(long id, long ownerID, long hordeID,
					long targetID,
					float x, float y,
					float theta, float speed, float maxSpeed,
					float health, float maxHealth) {
		super(id, ownerID, hordeID, targetID, x, y,
				theta, speed, maxSpeed, health, maxHealth);
	}
	
	@Override
	public Circle getCollider() {
		return getHealth() <= 0 ? null :
			new Circle(getX(), getY(), KNIGHT_COLLIDER_RADIUS);
	}
	
	// For changing the render state,
	// called by unit whenever this unit was damaged
	// by another
	@Override
	protected void onDamage(float damage) {
		// Reset death/damage animation times in case these animations
		// need to be drawn
		damageAnimation = KNIGHT_DAMAGE_ANIMATION_TIME; 
	}
	
	@Override
	public void render(Renderer r, float dt) {
		if (getHealth() <= 0) {
			// Reset out any animations;
			damageAnimation = 0;
			walkAnimation = 0;
			attackAnimation = 0;
			r.drawImage("knight_dead.png",
					getX(), getY() + KNIGHT_DRAW_YOFF,
						KNIGHT_DRAW_WIDTH, KNIGHT_DRAW_HEIGHT, 0);
			return;
		}
		
		// If we are targeting someone, always face them
		boolean rightDir = getTheta() >= -(float) Math.PI / 2f &&
				   getTheta() <= (float) Math.PI / 2f;		
		if (getTarget() > 0) {
			Entity e = getArena().getEntity(getTarget());
			if (e != null) {
				rightDir = e.getX() - getX() > 0;
			}
		}

		Color tint = null;
		if (damageAnimation > 0) {
			damageAnimation -= dt;
			float progress = 1 - damageAnimation / KNIGHT_DAMAGE_ANIMATION_TIME;
			tint = new Color(1f, progress, progress);
		}
		
		String filename = rightDir ? "knight_right" : "knight_left";
		if (getTarget() >= 0) {
			// Get the animation index
			attackAnimation += dt;
			filename += "_attack_" +
					(int) (1 + ((attackAnimation / KNIGHT_ATTACK_ANIMATION_STEP_TIME) 
						% KNIGHT_ATTACK_ANIMATION_FRAMES)) + ".png";
		} else {
			if (getSpeed() > 0) {
				walkAnimation += dt * getSpeed() +
								 dt * KNIGHT_WALK_ANIMATION_TIME_FACTOR;
				// Walk faster the faster we go, but with some base animation speed
			} else {
				walkAnimation = 0;
			}
			filename += "_walk_" + 
					(int) ( 1 + ((walkAnimation / KNIGHT_WALK_ANIMATION_STEP_TIME) 
					% KNIGHT_WALK_ANIMATION_FRAMES)) + ".png";
		}
		r.drawImage(filename, tint,
				getX(), getY() + KNIGHT_DRAW_YOFF,
					KNIGHT_DRAW_WIDTH, KNIGHT_DRAW_HEIGHT, 0);
	}

	@Override
	public void update(Arena a, float dt) {
		if (getHealth() <= 0) return; // If we are dead, do nothing
		
		// People we can target
		Set<Unit> targets = getTargetsWithin(KNIGHT_TARGET_RADIUS);

		// If we are fighting, check if we need to fight
		// someone else (due to death or them leaving)
		if (getTarget() >= 0 && !targets.contains(getArena().getEntity(getTarget()))) {
			target(null);
		}
	
		// If we have no one to target, check the targets
		// and fight them
		if (getTarget() < 0 && targets.size() > 0) {
			target(targets.iterator().next());
		}
						
		// If we are targeting someone and can attack, attack
		if (getTarget() >= 0 && attackCooldown <= 0) {
			attackCooldown = KNIGHT_ATTACK_COOLDOWN; // Reset the cooldown
			// ATTACK!
			Unit enemy = (Unit) getArena().getEntity(getTarget());
			enemy.damage(this, KNIGHT_ATTACK_DAMAGE);
		} else if (attackCooldown > 0) {
			attackCooldown -= dt; // Decrease the cooldown amount
		}
		
		// If we are not fighting and there are enemies in the vicinity
		// move towards them unless we have been commanded to move
		// with above a certain threshold of speed
		
		// If we are not fighting and not dead, move
		// if so commanded
		if (getSpeed() > 0) {
			float speed = getSpeed();
			if (getTarget() >= 0) {
				// Calculate the distance to the target
				Entity t = getArena().getEntity(getTarget());
				if (t != null && t.getCollider() != null && getCollider() != null) {
					float distance = getCollider().distanceTo(t.getCollider());
					if (distance < KNIGHT_ATTACK_SPEED_RADIUS)
						speed *= KNIGHT_ATTACK_SPEED_CAP;
				}
			}
			// Try and move in the specified direction
			float dx = dt * speed * (float) Math.cos(getTheta());
			float dy = dt * speed * (float) Math.sin(getTheta());
			move(getX() + dx, getY() + dy);
		}
	}

	@Override
	public void pack(Protocol.Entity.Builder builder) {
		builder.setId(getID()).setX(getX()).setY(getY()).getKnightBuilder()
				.setOwner(getOwner()).setHordeID(getHorde())
				.setTargetID(getTarget())
			    .setTheta(getTheta()).setSpeed(getSpeed())
			    .setMaxSpeed(getMaxSpeed()).setHealth(getHealth())
			    .setMaxHealth(getMaxHealth());
	}

	// For unpacking
	public static void register() {
		Entity.Parser.add(TypeCase.KNIGHT, new Entity.Parser() {
			@Override
			public Entity unpack(Protocol.Entity e) {
				Protocol.Unit k = e.getKnight();
				return new Knight(e.getId(), k.getOwner(),
						k.getHordeID(), k.getTargetID(),
						e.getX(), e.getY(),
						k.getTheta(), k.getSpeed(), k.getMaxSpeed(),
						k.getHealth(), k.getMaxHealth());
			}
		});
	}
}
