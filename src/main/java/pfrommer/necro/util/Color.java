package pfrommer.necro.util;

public class Color {
	private float r, g, b, a;
	
	public Color(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public Color(float r, float g, float b) {
		this(r, g, b, 1f);
	}
	
	public float getRed() { return r; }
	public float getGreen() { return g; }
	public float getBlue() { return b; }
	public float getAlpha() { return a; }
	
	public int hashCode() {
		// Discretize to 255 and hash based on that
		int red = (int) (255 * r);
		int green = (int) (255 * g);
		int blue = (int) (255 * b);
		int alpha = (int) (255 * a);

		int hash = 17;
		hash = 31 * hash + red;
		hash = 31 * hash + green;
		hash = 31 * hash + blue;
		hash = 31 * hash + alpha;
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o.getClass() == Color.class)) return false;
		Color c = (Color) o;
		return (int) (255 * r) == (int) (255 * c.r) &&
				(int) (255 * g) == (int) (255 * c.g) &&
				(int) (255 * b) == (int) (255 * c.b);
	}
}
