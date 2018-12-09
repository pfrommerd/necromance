package pfrommer.necro.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import pfrommer.necro.game.App;
import pfrommer.necro.net.Client;

public class SwingLauncher implements Runnable {
	private App app;
	private SwingDisplay display;
	private SwingRenderer renderer;
	
	private CustomPanel panel;
	

	public SwingLauncher(Client client) {
		
		// Create the application frame
		JFrame frame = new JFrame("Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new CustomPanel();
		panel.setPreferredSize(new Dimension(600, 600));
		panel.setFocusable(true);


		display = new SwingDisplay(panel);
		renderer = new SwingRenderer(panel);
		app = new App(display, client);

		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {}// About 60 fps
			panel.repaint(); // Request a repaint
		}
	}
	
	public class CustomPanel extends JPanel {
		private static final long serialVersionUID = -3869367575393104873L;
		private long lastPaintTime;
		
		@Override
		public void paintComponent(Graphics g) {			
			Graphics2D g2d = (Graphics2D) g;
			renderer.useGraphics(g2d);
			
			float dt;
			if (lastPaintTime == 0) {
				dt = 0;
				lastPaintTime = System.currentTimeMillis();
			} else {
				dt = (System.currentTimeMillis() - lastPaintTime) / 1000f;
				lastPaintTime = System.currentTimeMillis();
			}
			
			// Draw the app
			if (app != null) app.render(display, renderer, dt);
			if (display != null) display.update();
		}
	}
	
	public static void launch(Client client) {
		SwingLauncher l = new SwingLauncher(client);
		Thread ct = new Thread(l);
		ct.start();
	}
}
