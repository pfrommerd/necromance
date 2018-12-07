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
	// Colored versions of the images
	private Map<String, Map<Color, BufferedImage>> coloredMap = new HashMap<>();
	
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
	public void drawImage(String image, float x, float y,
								float w, float h, float rot) {
		// No tint-coloring!
		drawImage(image, null, x, y, w, h, rot);
	}

	@Override
	public void drawImage(String image, Color tint, float x, float y, float w, float h, float rot) {
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
		
		if (tint != null) {
			// Get a tinted version of the image
			if (!coloredMap.containsKey(image)) coloredMap.put(image, new HashMap<>());
			Map<Color, BufferedImage> imgs = coloredMap.get(image);
			if (!imgs.containsKey(tint)) {
				// Colorize the image and put it in the map
				imgs.put(tint, SwingRenderer.tintImage(img, tint));
			}
			img = imgs.get(tint);
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
		graphics.setColor(new java.awt.Color((int) (255 *c.getRed()),
				 (int) (255 *c.getGreen()),
				 (int) (255 *c.getBlue()),
				 (int) (255 *c.getAlpha()) ));

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
	
	private static BufferedImage tintImage(BufferedImage src, Color c) {
		// From https://stackoverflow.com/questions/4248104/applying-a-tint-to-an-image-in-java
	    BufferedImage newImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TRANSLUCENT);
	    Graphics2D graphics = newImage.createGraphics();
	    graphics.drawImage(src, 0, 0, null);
	    graphics.dispose();
	    float r = c.getRed();
	    float g = c.getGreen();
	    float b = c.getBlue();
	    // Color image
	    for (int i = 0; i < newImage.getWidth(); i++) {
	        for (int j = 0; j < newImage.getHeight(); j++) {
	            int ax = newImage.getColorModel().getAlpha(newImage.getRaster().getDataElements(i, j, null));
	            int rx = newImage.getColorModel().getRed(newImage.getRaster().getDataElements(i, j, null));
	            int gx = newImage.getColorModel().getGreen(newImage.getRaster().getDataElements(i, j, null));
	            int bx = newImage.getColorModel().getBlue(newImage.getRaster().getDataElements(i, j, null));
	            rx *= r;
	            gx *= g;
	            bx *= b;
	            newImage.setRGB(i, j, (ax << 24) | (rx << 16) | (gx << 8) | (bx << 0));
	        }
	    }
	    return newImage;
	}

}
