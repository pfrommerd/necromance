package pfrommer.necro.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import pfrommer.necro.util.Display;

public class SwingDisplay implements Display {
	private JPanel panel;
	
	private float x;
	private float y;
	
	private boolean leftDown = false;
	private boolean rightDown = false;
	
	public boolean justRightPress;
	public boolean justLeftPress;
	public boolean justRightLift;
	public boolean justLeftLift;
	
	public SwingDisplay(JPanel panel) {
		this.panel = panel;
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					leftDown = true;
					justLeftPress = true;
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					rightDown = true;
					justRightPress = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					leftDown = false;
					justLeftLift = true;
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					rightDown = false;
					justRightLift = true;
				}
			}
		});
		panel.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				x = (e.getX() - getWidth() / 2) / ((float) getWidth() / 2f);
				y = -(e.getY() - getHeight() / 2) / ((float) getHeight() / 2f);
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				x = (e.getX() - getWidth() / 2) / ((float) getWidth() / 2f);
				y = -(e.getY() - getHeight() / 2) / ((float) getHeight() / 2f);
			}
		});
	}
	

	public int getWidth() { return panel.getWidth(); }
	public int getHeight() { return panel.getHeight(); }
	
	public float getDensity() {
		return 220f; // HELP, just guess
	}
	
	public float getMouseX() { return x; }
	public float getMouseY() { return y; }
	
	public boolean isLeftButtonDown() { return leftDown; }
	public boolean isRightButtonDown() { return rightDown; }
	
	public boolean isLeftButtonPressed() { return justLeftPress; }
	public boolean isRightButtonPressed() { return justRightPress; }
	
	public boolean isLeftButtonLifted() { return justLeftLift; }
	public boolean isRightButtonLifted() { return justRightLift; }

	public void update() {
		// Reset all of these, will be set to true again
		// by events
		justLeftPress = false;
		justRightPress = false;
		justLeftLift = false;
		justRightLift = false;
	}
}
