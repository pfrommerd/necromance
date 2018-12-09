package pfrommer.necro.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class Sprite {
	private BufferedImage image;
	private BufferedImage flipped;
	private Map<Color, BufferedImage> tinted = new HashMap<>();
	private Map<Color, BufferedImage> tintedFlipped = new HashMap<>();
	
	public Sprite(BufferedImage img) {
		image = img;
		flipped = flip(image);
	}
	
	public BufferedImage getImage() { return image; }
	public BufferedImage getFlipped() { return flipped; }
	
	public BufferedImage getTinted(Color c) {
		if (tinted.containsKey(c)) return tinted.get(c);
		tinted.put(c, tintImage(image, c));
		return tinted.get(c);
	}
	
	public BufferedImage getTintedFlipped(Color c) {
		if (tintedFlipped.containsKey(c)) return tintedFlipped.get(c);
		tintedFlipped.put(c, tintImage(flipped, c));
		return tintedFlipped.get(c);
	}
	
	public static Sprite load(String filename) {
		try {
			File f = new File(new File("files"), filename);
			InputStream s = null;
			if (f.exists()) {
				s = new FileInputStream(f); 
			} else {
				s = Sprite.class.getResourceAsStream("/" + filename);
			}
			return new Sprite(ImageIO.read(s));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
		
	private static BufferedImage tintImage(BufferedImage src, Color c) {
		// Modified version of https://stackoverflow.com/questions/4248104/applying-a-tint-to-an-image-in-java
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
	
	private static BufferedImage flip(BufferedImage src) {
	    BufferedImage newImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TRANSLUCENT);
	    Graphics2D graphics = newImage.createGraphics();
	    graphics.drawImage(src, 0, 0, null);
	    graphics.dispose();
	    
	    for (int i = 0; i < newImage.getWidth()/2; i++) {
	    	for (int j = 0; j < newImage.getHeight(); j++) {
	    		int tmp = src.getRGB(i, j);
	    		int tmp2 = src.getRGB(newImage.getWidth() - i - 1, j);
	    		
	    		src.setRGB(i, j, tmp2);
	    		src.setRGB(newImage.getWidth() - i - 1, j, tmp);
	    	}
	    }
	    return newImage;
	}
}
