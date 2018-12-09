package pfrommer.necro.game;

import pfrommer.necro.net.Protocol;
import pfrommer.necro.net.Protocol.Entity.TypeCase;
import pfrommer.necro.util.Circle;
import pfrommer.necro.util.Renderer;
import pfrommer.necro.util.Sprite;

public class Rock extends Entity {
	public static final float ROCK_COLLIDER_RADIUS = 2.0f;
	public static final float ROCK_IMAGE_WIDTH = 5.0f;
	public static final float ROCK_IMAGE_HEIGHT = 5.0f;
	
	public static final Sprite ROCK_SPRITE = Sprite.load("rock.png");
	
	public Rock(long id, float x, float y) {
		super(id, x, y);
	}

	@Override
	public Circle getCollider() {
		return new Circle(getX(), getY(), ROCK_COLLIDER_RADIUS);
	}

	@Override
	public void render(Renderer r, float dt) {
		r.drawImage(ROCK_SPRITE, getX(), getY(), ROCK_IMAGE_WIDTH, ROCK_IMAGE_HEIGHT);
	}

	@Override
	public void update(float dt) {}

	@Override
	public void pack(Protocol.Entity.Builder builder) {
		builder.setId(getID()).setX(getX()).setY(getY()).getRockBuilder(); // Just add a rock type, we don't need any extra fields
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
