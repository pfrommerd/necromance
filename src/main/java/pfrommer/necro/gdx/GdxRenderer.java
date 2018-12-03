package pfrommer.necro.gdx;

import java.util.HashMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import pfrommer.necro.util.Color;
import pfrommer.necro.util.Renderer;

public class GdxRenderer extends Renderer {
	private enum RenderMode { NONE, BATCH, SHAPES }
	
	private OrthographicCamera orthoCam = new OrthographicCamera();
	private SpriteBatch batch = new SpriteBatch();
	private ShapeRenderer shapes = new ShapeRenderer();
	
	private RenderMode mode = RenderMode.NONE; // If in shape mode
	private AssetManager manager = new AssetManager();
	private HashMap<String, TextureRegion> textures = new HashMap<String, TextureRegion>();
	
	public GdxRenderer() {
		FileHandleResolver resolver = new LocalFileHandleResolver();
		manager.setLoader(Texture.class, new TextureLoader(resolver));
	}
	
	private void startMode(RenderMode mode) {
		switch(mode) {
		case NONE: break;
		case BATCH: batch.begin(); break;
		case SHAPES: shapes.begin(ShapeType.Filled); break;
		}
	}

	private void endMode(RenderMode mode) {
		switch(mode) {
		case NONE: break;
		case BATCH: batch.end(); break;
		case SHAPES: shapes.end(); break;
		}
	}
	
	public void begin() {
		mode = RenderMode.NONE; // Reset the mode
	}
	
	public void finish() {
		endMode(mode);
	}
	
	@Override
	public void orthoCamera(float x, float y, float w, float h) {
		orthoCam.setToOrtho(false, w, h);
		orthoCam.position.x = x;
		orthoCam.position.y = y;
		orthoCam.update();
		batch.setProjectionMatrix(orthoCam.combined);
		shapes.setProjectionMatrix(orthoCam.combined);
	}

	@Override
	public void drawRectangle(Color c, float x, float y, float w, float h, float rot) {
		if (mode != RenderMode.SHAPES) {
			endMode(mode);
			mode = RenderMode.SHAPES;
			startMode(mode);
		}
		
		shapes.setColor(new com.badlogic.gdx.graphics.Color(c.getRed(), c.getGreen(),
																c.getBlue(), c.getAlpha()));
		shapes.rect(x - w/2, y - h/2, w/2, h/2, w, h, 1, 1,
				(float) Math.toDegrees(rot));
	}

	@Override
	public void drawImage(String image, Color tint,
							float x, float y, float w, float h, float rot) {
		if (!textures.containsKey(image)) {
			// Load the image
			manager.load(image, Texture.class);
			manager.finishLoading();
			Texture t = manager.get(image, Texture.class);
			TextureRegion r = new TextureRegion(t);
			textures.put(image, r);
		}
		if (mode != RenderMode.BATCH) {
			endMode(mode);
			mode = RenderMode.BATCH;
			startMode(mode);
		}
		if (tint != null) {
			batch.setColor(new com.badlogic.gdx.graphics.Color(tint.getRed(), tint.getGreen(),
																tint.getBlue(), tint.getAlpha()));
		}
		TextureRegion region = textures.get(image);
		batch.draw(region,
				x - w/2, y - h/2, w/2, h/2,
				w, h, 1, 1,
				(float) Math.toDegrees(rot));
		
		if (tint != null)
			batch.setColor(com.badlogic.gdx.graphics.Color.WHITE); // Reset tint
	}

	@Override
	public void drawImage(String image,
							float x, float y, float w, float h, float rot) {
		drawImage(image, null, x, y, w, h, rot);
	}
}
