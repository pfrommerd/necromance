package pfrommer.necro.game;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Entity.TypeCase;
import pfrommer.necro.util.Circle;
import pfrommer.necro.util.Renderer;

public class Rock extends Entity {
	public static final float ROCK_COLLIDER_RADIUS = 1.0f;
	public static final float ROCK_IMAGE_WIDTH = 1.0f;
	public static final float ROCK_IMAGE_HEIGHT = 1.0f;
	
	public Rock(long id, float x, float y) {
		super(id, x, y);
	}

	@Override
	public Circle getCollider() {
		return new Circle(getX(), getY(), ROCK_COLLIDER_RADIUS);
	}

	@Override
	public void render(Renderer r, float dt) {
		r.drawImage("rock.png", dt, dt, ROCK_IMAGE_WIDTH, ROCK_IMAGE_HEIGHT, 0);
	}

	@Override
	public void update(Arena a, float dt) {}

	@Override
	public void pack(Protocol.Entity.Builder builder) {
		builder.getRockBuilder(); // Just add a rock type
	}
	
	public static void register() {
		Entity.Parser.add(TypeCase.ROCK, new Entity.Parser() {
			@Override
			public Entity unpack(Protocol.Entity e) {
				return new Rock(e.getId(), e.getX(), e.getY());
			}
		});
	}
}
