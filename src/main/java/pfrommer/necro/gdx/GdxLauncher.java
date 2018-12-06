package pfrommer.necro.gdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL30;

import pfrommer.necro.game.App;
import pfrommer.necro.net.Client;

public class GdxLauncher implements ApplicationListener {
	private App app;
	private GdxDisplay display;
	private GdxRenderer renderer;
	
	private Client client;
	
	public GdxLauncher(Client client) {
		this.client = client;
	}
	
	public void create() {
		Gdx.graphics.setTitle("Necromance");
		display = new GdxDisplay();
		renderer = new GdxRenderer();
		app = new App(display, client);
	}

	public void resize(int width, int height) {}

	public void render() {
		// Update the display input
		display.update();
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		renderer.begin();
		app.render(display, renderer, Gdx.graphics.getDeltaTime());
		renderer.finish();
	}

	public void pause() {}

	public void resume() {}

	public void dispose() {}
	
	public static void launch(Client client) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new GdxLauncher(client), config);
	}
}
