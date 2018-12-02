package pfrommer.necro.util;

public class Rectangle {
	private float x, y, width, height;
	
	// x and y are at the center
	public Rectangle(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
	}
	
	public float getX() { return x; }
	public float getY() { return y; }
	public float getWidth() { return width; }
	public float getHeight() { return height; }
	
	public boolean intersects(Rectangle r) {
		return !(x + width < r.x || x > r.x + r.width ||
				 y + height < r.y || y > r.y + r.height);
	}
}
