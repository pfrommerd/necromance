package pfrommer.necro.swing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import pfrommer.necro.util.Color;
import pfrommer.necro.util.Renderer;
import pfrommer.necro.util.Sprite;

public class SwingRenderer implements Renderer {
	private Graphics2D graphics = null;
	private JPanel panel = null;
	
	// I use these instead of g2d transforms
	// for simplicity/more fine-grained control
	private float cx, cy, cw, ch;
	
	public SwingRenderer(JPanel panel) {
		this.panel = panel;
	}
	
	public void useGraphics(Graphics2D g) {
		this.graphics = g;
		cx = 0; cy = 0; cw = 2; ch = 2;
	}
	
	@Override
	public void resetCamera() {
		cx = 0; cy = 0; cw = 2; ch = 2;
	}
	
	@Override
	public void orthoCamera(float x, float y, float w, float h) {
		cx = x; cy = y;
		cw = w; ch = h;
	}
	
	@Override
	public void clear(Color c) {
		graphics.setColor(toAwt(c));
		graphics.fillRect(0, 0, panel.getWidth(), panel.getHeight());
	}

	@Override
	public void drawImage(Sprite sprite, float x, float y,
										float w, float h) {
		// No tint-coloring!
		drawImage(sprite, null, false, x, y, w, h);
	}

	@Override
	public void drawImage(Sprite sprite, boolean flipped,
										float x, float y,
										float w, float h) {
		// No tint-coloring!
		drawImage(sprite, null, flipped, x, y, w, h);
	}

	@Override
	public void drawImage(Sprite sprite, Color tint, boolean flipped,
							float x, float y, float w, float h) {
		if (sprite == null) return;
		BufferedImage img = null;
		if (tint == null) {
			img = flipped ? sprite.getFlipped() : sprite.getImage();
		} else {
			img = flipped ? sprite.getTintedFlipped(tint) : sprite.getTinted(tint);
		}
		
		graphics.drawImage(img,
				panel.getWidth()/2 + (int) (panel.getWidth() * (x - w/2 - cx)/cw),
  				panel.getHeight()/2 - (int) (panel.getHeight() * (y + h/2 - cy)/ch),
  				(int) (panel.getWidth() * w / cw),
  				(int) (panel.getHeight() * h / ch),
			  					null);
	}
	
	@Override
	public void drawText(float x, float y, float size, Color c,
							TextMode mode, String text) {
		float ns = panel.getHeight() * size / ch;
		graphics.setFont(graphics.getFont().deriveFont(ns));
		graphics.setColor(toAwt(c));

		float nx = panel.getWidth()/2 + (panel.getWidth() * (x - cx)/cw);
		float ny = panel.getHeight()/2 - (panel.getHeight() * (y - cy)/ch);
		float width = graphics.getFontMetrics().stringWidth(text);
		float height = graphics.getFontMetrics().getHeight();
		// Adjust according to the text mode
		switch (mode) {
		case BOTTOM_LEFT: break;
		case BOTTOM_RIGHT: nx -= width; break;
		case TOP_LEFT: ny += height; break;
		case TOP_RIGHT: nx -= width; ny += height; break;
		case CENTER: nx -= width/2; ny += height/2; break;
		}
		graphics.drawString(text, nx, ny);
		
		graphics.setColor(java.awt.Color.BLACK);
	}
	
	private static java.awt.Color toAwt(Color c) {
		return new java.awt.Color((int) (255 *c.getRed()),
							(int) (255 *c.getGreen()),
							(int) (255 *c.getBlue()),
							(int) (255 *c.getAlpha()));
	}

}
