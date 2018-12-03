package pfrommer.necro.util;

public class Point {
	private float x, y;
	
	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float getX() { return x; }
	public float getY() { return y; }
	
	public float distanceTo(Point other) {
		if (other == null) return 0;
		float dx = x - other.x;
		float dy = y - other.y;
		return (float) Math.sqrt(dx*dx + dy*dy);
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
