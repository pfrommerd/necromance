package pfrommer.necro.gdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL30;

import pfrommer.necro.client.App;

public class GdxLauncher {
	protected static class GdxWrapper implements ApplicationListener {
		private App app;
		private GdxDisplay display;
		private GdxRenderer renderer;
		
		private boolean host;
		private String hostname;
		private int port;
		
		public GdxWrapper(boolean host, String hostname, int port) {
			this.host = host;
			this.hostname = hostname;
			this.port = port;
		}
		
		public void create() {
			Gdx.graphics.setTitle("Necromance");
			display = new GdxDisplay();
			renderer = new GdxRenderer();
			app = new App(host, hostname, port);
			app.create(display);
		}

		public void resize(int width, int height) {
			app.resize(display, width, height);
		}

		public void render() {
			// Update the display input
			display.update();
			
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
			renderer.begin();
			app.render(display, renderer, Gdx.graphics.getDeltaTime());
			renderer.finish();
		}

		public void pause() {
			app.pause(display);
		}

		public void resume() {
			app.resume(display);
		}

		public void dispose() {
			app.dispose(display);
			app = null;
		}
	}
	
	public static void launch(boolean host, String hostname, int port) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new GdxWrapper(host, hostname, port), config);
	}
	
	public static void main(String[] args) {
		boolean host = true;
		String hostname = "0.0.0.0";
		int port = 6000;
		
		if (args.length > 1) {
			host = args[0].equals("host");
			hostname = args[1];
			try {
				port = Integer.parseInt(args[2]);
			} catch (Exception e) {} // If not a number
		}
		
		GdxLauncher.launch(host, hostname, port);
	}
}
