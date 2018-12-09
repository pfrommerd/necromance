package pfrommer.necro.game;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Entity.TypeCase;
import pfrommer.necro.util.Circle;
import pfrommer.necro.util.Renderer;
import pfrommer.necro.util.SpriteSheet;

public class Spell extends Entity {
	public static final float SPELL_SPEED = 25f;
	public static final float SPELL_DAMAGE = 50f;
	public static final float SPELL_DAMAGE_RADIUS = 0.5f;
	
	public static final float SPELL_WIDTH = 1f;
	public static final float SPELL_HEIGHT = 1f;
	
	public static final float SPELL_ANIMATION_STEP = 0.5f;
	
	public static final SpriteSheet SPELL_SPRITE =
			SpriteSheet.load("spell.png", 1, 7, false);
	
	private long castingPlayer;
	private float vx;
	private float vy;
	
	
	// This is a local thing for rendering the animation
	private float spellAnimation = 0f;
	
	public Spell(long id, float x, float y,
					long castingPlayer, float vx, float vy) {
		super(id, x, y);
		this.castingPlayer = castingPlayer;
		this.vx = vx;
		this.vy = vy;
	}
	
	// We'll handle collisions in the update
	@Override
	public Circle getCollider() { return null; }
	
	@Override
	public void update(float dt) {
		// Check if a unit
		// of an enemy is in range
		Unit target = null;
		
		Circle c = new Circle(getX(), getY(), SPELL_DAMAGE_RADIUS);
		for (Entity e : getArena().getEntities()) {
			if (e instanceof Unit) {
				Unit u = (Unit) e;
				if (u.getCollider() == null) continue;
				if (u.getOwner() == castingPlayer ||
						u.getOwner() < 0 && castingPlayer < 0 ||
						u.getHealth() < 0) continue;
				float distance = c.distanceTo(u.getCollider());
				if (distance < 0) {
					target = u;
					break;
				}
			}
		}
		
		if (target != null) {
			// Remove the spell and damage the target
			target.damage(castingPlayer, SPELL_DAMAGE);
			getArena().removeEntity(this);
			return;
		}
		
		// If out of bounds
		if (Math.abs(getX()) > getArena().getWidth() / 2 ||
			Math.abs(getY()) > getArena().getHeight() / 2) {
			getArena().removeEntity(this);
			return;
		}
		
		move(getX() + vx * dt , getY() + vy * dt);
	}

	@Override
	public void render(Renderer r, float dt) {
		r.drawImage(SPELL_SPRITE.get((int) (spellAnimation / SPELL_ANIMATION_STEP)),
							getX(), getY(), SPELL_WIDTH, SPELL_HEIGHT);
		spellAnimation += dt;
	}
	
	@Override
	public void pack(Protocol.Entity.Builder builder) {
		builder.setId(getID()).setX(getX()).setY(getY()).getSpellBuilder()
				.setVy(vy).setVx(vx).setCastingPlayer(castingPlayer);
	}

	// For unpacking
	public static void register() {
		Entity.Parser.add(TypeCase.SPELL, new Entity.Parser() {
			@Override
			public Entity unpack(Protocol.Entity e) {
				Protocol.Spell k = e.getSpell();
				return new Spell(e.getId(), e.getX(), e.getY(),
									k.getCastingPlayer(), k.getVx(), k.getVy());
			}
		});
	}
}
