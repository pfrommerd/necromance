package pfrommer.necro.game;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Entity.TypeCase;
import pfrommer.necro.util.Rectangle;
import pfrommer.necro.util.Renderer;

public class Knight extends Unit {
	public Knight(long id, long ownerID,
					float x, float y,
					float theta, float speed, float maxSpeed,
					float health) {
		super(id, ownerID, x, y, theta, speed, maxSpeed, health);
	}

	// The z should be the center minus half the height
	@Override
	public float getZ() {
		return getY() - 2.5f;
	}
	
	@Override
	public Rectangle getCollider() {
		return new Rectangle(getX(), getY(), 7, 5);
	}
	
	@Override
	public void render(Renderer r) {
		r.drawImage("files/knight_right.png", getX(), getY(), 7, 5, 0);
	}

	@Override
	public void update(Arena a, float dt) {
		if (getHealth() < 0) return;
		if (getSpeed() > 0) {
			// Try and move in the specified direction
			float dx = dt * getSpeed() * (float) Math.cos(getTheta());
			float dy = dt * getSpeed() * (float) Math.sin(getTheta());
			move(getX() + dx, getY() + dy);
		}
	}

	@Override
	public void pack(Protocol.Entity.Builder builder) {
		builder.setId(getID()).getKnightBuilder()
				.setOwner(getOwner())
			   .setX(getX()).setY(getY())
			   .setTheta(getTheta()).setSpeed(getSpeed())
			   .setMaxSpeed(getMaxSpeed()).setHealth(getHealth());
	}

	// For unpacking
	static {
		Entity.Parser.add(TypeCase.KNIGHT, new Entity.Parser() {
			@Override
			public Entity unpack(Protocol.Entity e) {
				Protocol.Unit k = e.getKnight();
				return new Knight(e.getId(), k.getOwner(), k.getX(), k.getY(),
						k.getTheta(), k.getSpeed(), k.getMaxSpeed(), k.getHealth());
			}
		});
	}
}
