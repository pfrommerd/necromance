package pfrommer.necro.game;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Entity.TypeCase;
import pfrommer.necro.util.Circle;
import pfrommer.necro.util.Renderer;
import pfrommer.necro.util.Sprite;
import pfrommer.necro.util.Color;
import pfrommer.necro.util.SpriteSheet;

public class Mage extends Unit {
	public static final float MAGE_COLLIDER_RADIUS = 0.8f;
	public static final float MAGE_TARGET_RADIUS = 10f; // Fight anyone within this radius of our collider
	
	public static final float MAGE_ATTACK_COOLDOWN = 6f;

	public static final float MAGE_MAX_HEALTH = 100f;
	public static final float MAGE_MAX_SPEED = 10f;
	
	// for animation timing
	
	public static final float MAGE_WALK_STEP_TIME = 1f;
	public static final float MAGE_ATTACK_STEP_TIME = 0.5f;

	// image related things
	
	public static final SpriteSheet MAGE_SPRITES =
			SpriteSheet.load("mage.png", 1, 4, false);

	public static final float MAGE_SPRITE_WIDTH = 3;
	public static final float MAGE_SPRITE_HEIGHT = 5;
	public static final float MAGE_SPRITE_YOFF = 1;
	
	public static final Sprite MAGE_DEAD =
			Sprite.load("mage_dead.png");

	public static final float MAGE_DEAD_WIDTH = 3;
	public static final float MAGE_DEAD_HEIGHT = 5;
	public static final float MAGE_DEAD_YOFF = 1;
	
	// Creates a new, fully healthy knight
	// that is targeting nobody
	public Mage(long id, long ownerID, long hordeID, float x, float y) {
		this(id, ownerID, hordeID, -1, x, y, 0, 0, MAGE_MAX_HEALTH);
	}
	
	public Mage(long id, long ownerID, long hordeID,
					long targetID,
					float x, float y,
					float theta, float speed,
					float health) {
		super(id, ownerID, hordeID, targetID, x, y,
				theta, speed, health,
				MAGE_MAX_SPEED, MAGE_MAX_HEALTH,
				MAGE_TARGET_RADIUS, MAGE_ATTACK_COOLDOWN);
	}
	
	@Override
	public Circle getCollider() {
		return getHealth() <= 0 ? null :
			new Circle(getX(), getY(), MAGE_COLLIDER_RADIUS);
	}
	
	// For changing the render state,
	// called by unit whenever this unit was damaged
	// by another

	@Override
	public void launchAttack(Unit enemy) {
		// Fire a big ball of doom at the enemy
		float dx = enemy.getX() - getX();
		float dy = enemy.getY() - getY();
		float dist = (float) Math.sqrt(dx*dx + dy*dy);
		if (dist != 0) {
			dx /= dist;
			dy /= dist;
		}
		
		// Fire along dist
		getArena().addEntity(new Spell(getArena().getLargestID() + 1,
										getX(), getY(), getOwner(), 
										Spell.SPELL_SPEED *dx,
										Spell.SPELL_SPEED *dy));
	}

	@Override
	public void renderUnit(Renderer r, float damageAnimation, 
					float walkingAnimation, float attackAnimation,
						boolean faceRight, float dt) {
		Sprite s = null;
		Color c = new Color(1, damageAnimation, damageAnimation);
		float xo = 0, yo = 0; // x and y off
		float w = 0, h = 0; // x and y off
		if (getHealth() <= 0) {
			// Draw dead person
			s = MAGE_DEAD;
			faceRight = true;
			c = null;

			w = MAGE_DEAD_WIDTH;
			h = MAGE_DEAD_HEIGHT;
			yo = MAGE_DEAD_YOFF;
		} else {
			// Draw walking animation
			s = MAGE_SPRITES.get((int) (walkingAnimation / MAGE_WALK_STEP_TIME));
			w = MAGE_SPRITE_WIDTH;
			h = MAGE_SPRITE_HEIGHT;
			yo = MAGE_DEAD_YOFF;
		}
		r.drawImage(s, c, faceRight, getX() + xo, getY() + yo, w, h);
	}
	
	@Override
	public void pack(Protocol.Entity.Builder builder) {
		builder.setId(getID()).setX(getX()).setY(getY()).getMageBuilder()
				.setOwner(getOwner()).setHordeID(getHorde())
				.setTargetID(getTarget())
			    .setTheta(getTheta()).setSpeed(getSpeed())
			    .setHealth(getHealth());
	}

	// For unpacking
	public static void register() {
		Entity.Parser.add(TypeCase.MAGE, new Entity.Parser() {
			@Override
			public Entity unpack(Protocol.Entity e) {
				Protocol.Unit k = e.getMage();
				return new Mage(e.getId(), k.getOwner(),
						k.getHordeID(), k.getTargetID(),
						e.getX(), e.getY(),
						k.getTheta(), k.getSpeed(), k.getHealth());
			}
		});
	}
}
