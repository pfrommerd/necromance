package pfrommer.necro.util;

public class Rectangle {
	private float x, y, width, height, rot;
	
	public Rectangle(float x, float y, float w, float h, float rot) {
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		this.rot = rot;
	}
	
	public Rectangle(float x, float y, float w, float h) {
		this(x, y, w, h, 0);
	}
	
	public float getX() { return x; }
	public float getY() { return y; }
	public float getWidth() { return width; }
	public float getHeight() { return height; }
	public float getRotation() { return rot; }
}
