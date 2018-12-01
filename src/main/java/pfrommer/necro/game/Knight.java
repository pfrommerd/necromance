package pfrommer.necro.game;

import pfrommer.necro.util.Renderer;

public class Knight extends Unit {
	public Knight(long id, float x, float y, float health,
					State state, Player p) {
		super(id, x, y, health, state, p);
	}

	// The z should be the center minus half the height
	@Override
	public float getZ() {
		return getY() - 2.5f;
	}
	
	@Override
	public void render(Renderer r) {
		r.drawImage("files/knight_right.png", getX(), getY(), 7, 5, 0);
	}

	@Override
	public void update(Arena a, float dt) {
		// Do nothing for now
	}

}
