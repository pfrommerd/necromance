package pfrommer.necro.util;

public abstract class Renderer {	
	// Sets up a camera transformation
	public abstract void orthoCamera(float x, float y, float w, float h);
	
	public abstract void drawRectangle(Color c,
			float x, float y, float w, float h, float rot);
	
	// Draws given the path to the image, the rendering
	// backend determines for itself how best to load the image
	public abstract void drawImage(String image, Color tint,
			float x, float y, float w, float h, float rot);
	
	public abstract void drawImage(String image,
			float x, float y, float w, float h, float rot);
}