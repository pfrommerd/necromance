package pfrommer.necro.util;

public class Circle {
	public float x, y, radius;
	
	public Circle(float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}
	
	public float getX() { return x; }
	public float getY() { return y; }

	public float getRadius() { return radius; }
	
	public float distanceTo(Circle c) {
		float dx = x - c.x;
		float dy = y - c.y;
		return (float) Math.sqrt(dx*dx + dy*dy) - radius - c.radius;
	}
	
	public boolean intersects(Circle c) {
		if (c == null) return false;
		float dx = x - c.x;
		float dy = y - c.y;
		float tr = radius + c.radius;
		return (dx*dx + dy * dy) <  tr*tr;
	}
}
