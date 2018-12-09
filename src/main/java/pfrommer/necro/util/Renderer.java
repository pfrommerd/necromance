package pfrommer.necro.util;

public interface Renderer {	
	// Sets up a camera transformation
	public void resetCamera(); // Resets to a 0, 0, 1, 1 coordinate system
	public void orthoCamera(float x, float y, float w, float h);
	
	public void clear(Color c);
	
	// Draws given the path to the image, the rendering
	// backend determines for itself how best to load the image
	public void drawImage(Sprite sprite, Color tint, boolean flip,
							float x, float y, float w, float h);
	public void drawImage(Sprite sprite, boolean flip,
			float x, float y, float w, float h);
	public void drawImage(Sprite sprite, float x, float y, float w, float h);
	
	public enum TextMode { BOTTOM_LEFT, BOTTOM_RIGHT, CENTER, TOP_LEFT, TOP_RIGHT };
	public void drawText(float x, float y, float size, Color c,
							TextMode mode, String text);
}