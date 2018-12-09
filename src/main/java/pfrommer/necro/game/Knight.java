package pfrommer.necro.game;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Entity.TypeCase;
import pfrommer.necro.util.Circle;
import pfrommer.necro.util.Renderer;
import pfrommer.necro.util.Sprite;
import pfrommer.necro.util.Color;
import pfrommer.necro.util.SpriteSheet;

public class Knight extends Unit {
	public static final float KNIGHT_COLLIDER_RADIUS = 0.8f;
	public static final float KNIGHT_TARGET_RADIUS = 3f; // Fight anyone within this radius of our collider
	
	public static final float KNIGHT_ATTACK_DAMAGE = 20f;
	public static final float KNIGHT_ATTACK_COOLDOWN = 2f;

	public static final float KNIGHT_MAX_HEALTH = 100f;
	public static final float KNIGHT_MAX_SPEED = 10f;
	
	// for animation timing
	
	public static final float KNIGHT_WALK_STEP_TIME = 1f;
	public static final float KNIGHT_ATTACK_STEP_TIME = 0.5f;

	// image related things
	
	public static final SpriteSheet KNIGHT_SPRITES =
			SpriteSheet.load("knight.png", 1, 4, false);

	public static final float KNIGHT_SPRITE_WIDTH = 3;
	public static final float KNIGHT_SPRITE_HEIGHT = 5;
	public static final float KNIGHT_SPRITE_YOFF = 1;
	
	public static final Sprite KNIGHT_DEAD =
			Sprite.load("knight_dead.png");

	public static final float KNIGHT_DEAD_WIDTH = 3;
	public static final float KNIGHT_DEAD_HEIGHT = 5;
	public static final float KNIGHT_DEAD_YOFF = 1;
	
	// Creates a new, fully healthy knight
	// that is targeting nobody
	public Knight(long id, long ownerID, long hordeID, float x, float y) {
		this(id, ownerID, hordeID, -1, x, y, 0, 0, KNIGHT_MAX_HEALTH);
	}
	
	public Knight(long id, long ownerID, long hordeID,
					long targetID,
					float x, float y,
					float theta, float speed,
					float health) {
		super(id, ownerID, hordeID, targetID, x, y,
				theta, speed, health,
				KNIGHT_MAX_SPEED, KNIGHT_MAX_HEALTH,
				KNIGHT_TARGET_RADIUS, KNIGHT_ATTACK_COOLDOWN);
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
	public void launchAttack(Unit enemy) {
		// Knight just does straight damage
		enemy.damage(getOwner(), KNIGHT_ATTACK_DAMAGE);
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
			s = KNIGHT_DEAD;
			faceRight = true;
			c = null;

			w = KNIGHT_DEAD_WIDTH;
			h = KNIGHT_DEAD_HEIGHT;
			yo = KNIGHT_DEAD_YOFF;
		} else {
			// Draw walking animation
			s = KNIGHT_SPRITES.get((int) (walkingAnimation / KNIGHT_WALK_STEP_TIME));
			w = KNIGHT_SPRITE_WIDTH;
			h = KNIGHT_SPRITE_HEIGHT;
			yo = KNIGHT_DEAD_YOFF;
		}
		r.drawImage(s, c, faceRight, getX() + xo, getY() + yo, w, h);
	}
	
	@Override
	public void pack(Protocol.Entity.Builder builder) {
		builder.setId(getID()).setX(getX()).setY(getY()).getKnightBuilder()
				.setOwner(getOwner()).setHordeID(getHorde())
				.setTargetID(getTarget())
			    .setTheta(getTheta()).setSpeed(getSpeed())
			    .setHealth(getHealth());
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
						k.getTheta(), k.getSpeed(), k.getHealth());
			}
		});
	}
}
