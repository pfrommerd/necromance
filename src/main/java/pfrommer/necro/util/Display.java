package pfrommer.necro.util;

public interface Display {
	public int getWidth();
	public int getHeight();
	
	public float getDensity();
	
	public float getMouseX();
	public float getMouseY();
	
	public boolean isLeftButtonDown();
	public boolean isRightButtonDown();
	
	public boolean isLeftButtonPressed();
	public boolean isRightButtonPressed();
	
	public boolean isLeftButtonLifted();
	public boolean isRightButtonLifted();
}
