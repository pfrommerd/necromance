package pfrommer.necro.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import pfrommer.necro.util.Display;

public class GdxDisplay implements Display {
	private boolean leftPress;
	private boolean rightPress;
	
	private float x;
	private float y;
	
	public boolean justRightPress;
	public boolean justLeftPress;
	public boolean justRightLift;
	public boolean justLeftLift;
	
	public GdxDisplay() {}
	
	public int getWidth() { return Gdx.graphics.getWidth(); }
	public int getHeight() { return Gdx.graphics.getHeight(); }
	
	public float getDensity() {
		return Gdx.graphics.getDensity() * 160f;
	}
	
	public float getMouseX() { return x; }
	public float getMouseY() { return y; }
	
	public boolean isLeftButtonDown() { return leftPress; }
	public boolean isRightButtonDown() { return rightPress; }
	
	public boolean isLeftButtonPressed() { return justLeftPress; }
	public boolean isRightButtonPressed() { return justRightPress; }
	
	public boolean isLeftButtonLifted() { return justLeftLift; }
	public boolean isRightButtonLifted() { return justRightLift; }
	
	public void update() {
		boolean nl = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		boolean nr = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
		x = (Gdx.input.getX() - getWidth() / 2) / ((float) getWidth() / 2f);
		y = -(Gdx.input.getY() - getHeight() / 2) / ((float) getHeight() / 2f);
		
		justLeftPress = leftPress != nl ? nl : false;
		justRightPress = rightPress != nr ? nr : false;
		justLeftLift = leftPress != nl ? !nl : false;
		justRightLift = rightPress != nr ? !nr : false;
		
		leftPress = nl;
		rightPress = nr;
	}
}
