package pfrommer.necro.util;

import java.awt.image.BufferedImage;

public class SpriteSheet {
	
	// Takes a sprite containing
	// all of the sprites and cuts it up
	private Sprite[] sprites;
	
	public SpriteSheet(Sprite s, int width, int height,
								boolean rowMajor) {
		if (width <= 0 || height <= 0) 
			throw new IllegalArgumentException();
		
		sprites = new Sprite[width * height];
		
		// Cut everything up
		BufferedImage i = s.getImage();
		int sw = i.getWidth() / width;
		int sh = i.getHeight() / height;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {				
				BufferedImage sub = s.getImage().getSubimage(sw*x, sh*y, sw, sh);
				if (rowMajor) sprites[y*width + x] = new Sprite(sub);
				else sprites[x*height + y] = new Sprite(sub);
			}
		}
	}
	
	// Gets in a circular manner so that
	// it loops if you go past the maximum number of sprites
	public Sprite get(int i) {
		return sprites[i % sprites.length];
	}
	
	public static SpriteSheet load(String filename, int width, int height, boolean rowMajor) {
		Sprite s = Sprite.load(filename);
		if (s == null) return null;
		return new SpriteSheet(s, width, height, rowMajor);
	}
}
