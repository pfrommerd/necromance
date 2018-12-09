import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import pfrommer.necro.game.Arena;
import pfrommer.necro.game.Rock;
import pfrommer.necro.game.SpawnManager;
import pfrommer.necro.net.Client;
import pfrommer.necro.net.Server;
import pfrommer.necro.swing.SwingLauncher;

public class Game {
	public static final String INSTRUCTIONS_FILE = "instructions.txt";
	public static final String HIGHSCORE_FILE = ".necro_highscores";
	
	// Just hardcode these here for now
	public static final int ARENA_WIDTH = 200;
	public static final int ARENA_HEIGHT = 200;
	
	public Game() {}
	
	// Some utility IO things here
	// for reading high scores, files
	
	@SuppressWarnings("resource")
	public static String readInstructions() {
		// See https://stackoverflow.com/questions/309424/how-to-read-convert-an-inputstream-into-a-string-in-java
		Scanner s = null;
		try {
			InputStream is = openResource(INSTRUCTIONS_FILE);
			if (is == null) return "Could not find instructions file";
			s = new Scanner(is).useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} catch (IOException e) {
			return "Could not read instructions file";
		} finally {
			if (s != null) s.close();
		}
	}
	
	public static InputStream openResource(String name) throws IOException {
		File f = new File(new File("files"), name);
		if (f.exists()) return new FileInputStream(f);
		// Try and read it from the classpath
		return Game.class.getClassLoader().getResourceAsStream(name);
	}
	
	public static Arena createArena() {
		Arena arena = new Arena();
		arena.setWidth(ARENA_WIDTH);
		arena.setHeight(ARENA_HEIGHT);
		
		// Place 10 rocks in random places
		for (int i = 0; i < 10; i++) {
			arena.addEntity(new Rock(i, ARENA_WIDTH * (float) (Math.random() - 0.5f),
										ARENA_HEIGHT * (float) (Math.random() - 0.5f)));			
		}
		
		return arena;
	}
	
	public static SpawnManager createSpawnManager(Arena a) {
		SpawnManager m = new SpawnManager(a);
		for (int i = 0; i < 8; i++) m.addBot(); // Add 8 bots
		return m;
	}
	
	public static boolean launchClient(String host, int port) {
		Client client = new Client(host, port);
		try {
			client.open();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not connect to server!");
			return false;
		}
		
		// Launch the game
		SwingLauncher.launch(client);
		// success!
		return true;
	}
	
	public static boolean launchServer(int port) {
		Arena a = createArena();
		SpawnManager m = createSpawnManager(a);
		// Start the server in a separate thread
		Server s = new Server(a, m, "0.0.0.0", port);
		
		try {
			s.open();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not start server");
			return false;
		}
		
		Thread t = new Thread(s);
		t.start();
		// RUN MY SERVER, RUN!
		return true;
	}
	
	public static void showLauncher() {
		final JFrame frame = new JFrame("Launcher");
		
		JPanel root = new JPanel();
		frame.getContentPane().add(root, BorderLayout.CENTER);
		
		// Now to the root add a text pane containing all of the 
		// instructions in the center and a launching pane in the bottom
		root.setLayout(new BorderLayout());
		
		JTextArea text = new JTextArea(readInstructions());
		text.setRows(30);
		text.setColumns(30);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setEditable(false);
		
		root.add(new JScrollPane(text), BorderLayout.CENTER);
		
		// The toolbar for launching the server, client
		JPanel toolbar = new JPanel();
		root.add(toolbar, BorderLayout.SOUTH);
		
		// We will use another level of border layout nesting
		toolbar.setLayout(new BorderLayout());
		
		// The textfield for the host name
		final JTextField hostfield = new JTextField("localhost");
		hostfield.setColumns(20);
		
		// the text field for the port
		NumberFormat format = NumberFormat.getIntegerInstance();
		format.setGroupingUsed(false);
		final JFormattedTextField portField = 
				new JFormattedTextField(format);
		portField.setText("6000"); // Default port number
		portField.setColumns(10);
		
		JPanel portPanel = new JPanel();
		portPanel.setLayout(new BorderLayout());
		portPanel.add(new JLabel("Port: "), BorderLayout.WEST);
		portPanel.add(portField, BorderLayout.CENTER);
		
		JPanel hostPanel = new JPanel();
		hostPanel.setLayout(new BorderLayout());
		hostPanel.add(new JLabel("Host: "), BorderLayout.WEST);
		hostPanel.add(hostfield, BorderLayout.CENTER);
		
		toolbar.add(portPanel, BorderLayout.WEST);
		toolbar.add(hostPanel, BorderLayout.CENTER);
		
		// Add the join and host buttons, with appropriate
		// action listeners
		JButton joinButton = new JButton("Join");
		joinButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String host = hostfield.getText();
				int port = Integer.parseInt(portField.getText());
				if (!launchClient(host, port)) return;
				frame.dispose();
			}
		});
		
		JButton hostButton = new JButton("Host");
		hostButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int port = Integer.parseInt(portField.getText());
				if (!launchServer(port)) return;
				if (!launchClient("localhost", port)) return;
				frame.dispose();
			}
		});
		
		JPanel controlsPanel = new JPanel();
		controlsPanel.add(joinButton);
		controlsPanel.add(hostButton);
		
		toolbar.add(controlsPanel, BorderLayout.EAST);
		
		// Pack and make the frame visible
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}
	

	
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Game.showLauncher();
			}
		});
	}
}
