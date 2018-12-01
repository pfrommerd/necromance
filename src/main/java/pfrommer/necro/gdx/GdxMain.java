package pfrommer.necro.gdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL30;

import pfrommer.necro.client.App;

public class GdxMain {
	protected static class GdxWrapper implements ApplicationListener {
		private App app;
		private GdxDisplay display;
		private GdxRenderer renderer;
		public void create() {
			Gdx.graphics.setTitle("Tank Combat");
			display = new GdxDisplay();
			renderer = new GdxRenderer();
			app = new App(display);
			app.create();
		}

		public void resize(int width, int height) {
			app.resize(width, height);
		}

		public void render() {
			Gdx.gl.glClearColor(1, 1, 1, 0);
			Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
			renderer.begin();
			app.render(renderer, Gdx.graphics.getDeltaTime());
			renderer.finish();
		}

		public void pause() {
			app.pause();
		}

		public void resume() {
			app.resume();
		}

		public void dispose() {
			app.dispose();
			app = null;
		}
	}
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new GdxWrapper(), config);
	}
}
