package pfrommer.necro.swing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import pfrommer.necro.util.Color;
import pfrommer.necro.util.Renderer;

public class SwingRenderer implements Renderer {
	private Graphics2D graphics = null;
	private JPanel panel = null;
	private float cx, cy, cw, ch;
	
	private Map<String, BufferedImage> imgMap = new HashMap<>();
	
	public SwingRenderer(JPanel panel) {
		this.panel = panel;
	}
	
	public void useGraphics(Graphics2D g) {
		this.graphics = g;
		cx = 0; cy = 0; cw = 1; ch = 1;
	}
	
	@Override
	public void orthoCamera(float x, float y, float w, float h) {
		cx = x; cy = y;
		cw = w; ch = h;
	}

	@Override
	public void drawRectangle(Color c, float x, float y, float w, float h, float rot) {
		graphics.setColor(new java.awt.Color((int) (255 *c.getRed()),
											 (int) (255 *c.getGreen()),
											 (int) (255 *c.getBlue()),
											 (int) (255 *c.getAlpha()) ));
		graphics.drawRect(panel.getWidth()/2 + (int) (panel.getWidth() * (x - w/2 - cx)/cw),
  				panel.getHeight()/2 - (int) (panel.getHeight() * (y + h/2 - cy)/ch),
  				(int) (panel.getWidth() * w / cw),
  				(int) (panel.getHeight() * h / ch));
		graphics.setColor(java.awt.Color.BLACK);
	}

	@Override
	public void drawImage(String image, Color tint, float x, float y,
								float w, float h, float rot) {
		// No tint-coloring!
		drawImage(image, x, y, w, h, rot);
	}

	@Override
	public void drawImage(String image, float x, float y, float w, float h, float rot) {
		BufferedImage img = imgMap.get(image);
		if (img == null) {
			// Load this image
			try {
				File f = new File(new File("files"), image);
				if (f.exists()) {
					img = ImageIO.read(f);
				} else {
					InputStream s = SwingRenderer.class.getResourceAsStream("/" + image);
					if (s == null) throw new IOException("Could not find: " + image);
					img = ImageIO.read(s);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			imgMap.put(image, img);
		}
		
		graphics.drawImage(img,
				panel.getWidth()/2 + (int) (panel.getWidth() * (x - w/2 - cx)/cw),
  				panel.getHeight()/2 - (int) (panel.getHeight() * (y + h/2 - cy)/ch),
  				(int) (panel.getWidth() * w / cw),
  				(int) (panel.getHeight() * h / ch),
			  					null);
	}

}
