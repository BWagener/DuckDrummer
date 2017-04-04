/**
 * AutoDuck Drummer by Ronan Hanley
 * Feb. 2016
 */

package duck_drummer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

public class Drummer implements HotkeyListener, Runnable {
	protected static final String version = "v1.2.1";
	private static final int activateKey = (int) 'K';
	private static final int deactivateKey = (int) 'L';
	private JFrame frame;
	private BeatPanel beatPanel;
	private ControlPanel controlPanel;
	private static final int defaultNumBeats = 16;
	private int numBeats = defaultNumBeats;
	private int currentBeat = 0;
	private static final float defaultBpm = 120f;
	private float bpm = defaultBpm;
	private int sleepTime = bpmToSleepTime(bpm);
	private boolean beatRunning = false;
	private static final String homeFolderPath = System.getProperty("user.home") + "\\AutoDuck files\\";
	private static final String beatsPath = homeFolderPath + "\\beats\\";
	private static final String messageLockPath = homeFolderPath + "\\warning_lock.txt";
	private static final String keyBindsPath = homeFolderPath + "\\keybinds.txt";
	private KeyListener keyListener;
	
	public static void main(String[] args) {
		new Drummer().go();
	}
	
	public Drummer() {
		beatPanel = new BeatPanel(defaultNumBeats, this);
		controlPanel = new ControlPanel(this);
	}
	
	public void go() {
		// Create the folder to hold all files needed for this program
		File homeFolder = new File(homeFolderPath);
		homeFolder.mkdirs();
		
		File beatsFolder = new File(beatsPath);
		beatsFolder.mkdirs();
		
		final int bSize = 5;
		beatPanel.setBorder(BorderFactory.createEmptyBorder(bSize, bSize, 0, bSize));
		controlPanel.setBorder(BorderFactory.createEmptyBorder(0, bSize, bSize, bSize));
		controlPanel.updateFileList();
		controlPanel.getFileList().setSelectedItem("New File");
		frame = new JFrame("AutoDuck: Drummer - by Ronan Hanley" + " " + version);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				JIntellitype.getInstance().cleanUp();
				e.getWindow().dispose();
			}
		});
		BoxLayout boxLayout = new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS);
		frame.setLayout(boxLayout);
		frame.add(beatPanel);
		frame.add(controlPanel);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);;
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		List<Component> cList = getAllComponents(frame);
		Component[] comps = cList.toArray(new Component[cList.size()]);
		for(Component c : comps) {
			c.addKeyListener(keyListener);
		}
		
		frame.setVisible(true);
		
		/* The way I remember if the user said not to show the message again is by
		 * the existance of a certain file.
		 * The file is created when the user chooses not to show the message on startup
		 */
		if(shouldShowMessage()) {
			// Display the warning popup
			String[] options = new String[] {"Ok", "Don't show this again", "Exit"};
			int choice = JOptionPane.showOptionDialog(frame, readMessage(), "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
			
			switch(choice) {
			case 1:
				disableMessage();
				break;
			case 2:
				System.exit(0);
				break;
			}
		}
		
		JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_ALT, activateKey);
		JIntellitype.getInstance().registerHotKey(2, JIntellitype.MOD_ALT, deactivateKey);
		JIntellitype.getInstance().addHotKeyListener(this);
	}
	
	public void run() {
		LinkedList<Long> sleepTimes = new LinkedList<Long>();
		long lastCycle;
		long now;
		while(beatRunning) {
			long avgTime = 0;
			while(sleepTimes.size() > 10) {
				sleepTimes.remove(0);
			}
			for(int i=0; i<sleepTimes.size(); i++) {
				avgTime += sleepTimes.get(i);
			}
			if(sleepTimes.size() > 0) {
				avgTime /= sleepTimes.size();
			}else{
				avgTime = 25;
			}
			lastCycle = System.nanoTime();
			int restTime = (int) ((double) (avgTime) * 0.75d);
			beatPanel.playColumn(currentBeat, restTime);
			now = System.nanoTime();
			long diff = now - lastCycle;
			long sleepNs = sleepTime * 1000000 - diff;
			sleepTimes.add((sleepNs + restTime) /1000000);
			try {
				Thread.sleep(sleepNs /1000000, (int) (sleepNs %1000000L));
			}catch(Exception e) {
				e.printStackTrace();
			}
			currentBeat++;
			if(currentBeat >= numBeats) {
				currentBeat = 0;
			}
		}
	}
	
	public void onHotKey(int id) {
		switch(id) {
		case 1:
			if(!beatRunning) {
				beatRunning = true;
				currentBeat = 0;
				new Thread(this).start();
			} else {
				beatRunning = !beatRunning;
			}
			break;
		case 2:
			if(beatRunning) {
				beatRunning = !beatRunning;
			}
			break;
		}
		controlPanel.updatePlayPause(beatRunning);
	}
	
	public boolean isRunning() {
		return beatRunning;
	}
	
	public float getBPM() {
		return bpm;
	}
	
	public int getNumBeats() {
		return numBeats;
	}
	
	/**
	 * 
	 * @param newBpm
	 * @return Success
	 */
	public boolean setBPM(float newBpm) {
		if(newBpm > 40f && newBpm <= 320f) {
			bpm = newBpm;
			sleepTime = bpmToSleepTime(bpm);
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param newNumBeats
	 * @return Success
	 */
	public boolean setNumBeats(int newNumBeats) {
		if(newNumBeats >= 1 && newNumBeats <= 48) {
			numBeats = newNumBeats;
			beatPanel.updateNumBeats(numBeats);
			frame.pack();
			
			int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
			int offScreenBy = (frame.getX() + frame.getWidth()) - screenWidth;
			int padding = 10;
			if(offScreenBy > -padding) {
				frame.setLocation(frame.getX() - offScreenBy - padding, frame.getY());
			}
			return true;
		}
		return false;
	}
	
	public int getDefaultNumBeats() {
		return defaultNumBeats;
	}
	
	public float getDefaultBpm() {
		return defaultBpm;
	}
	
	/**
	 * Converts a bpm value to how many milliseconds of sleeping
	 * is needed to reach that bpm, if that sleep is used between
	 * beats.
	 * @param bpm
	 * @return
	 */
	private static final int bpmToSleepTime(float bpm) {
		return (int) (1f/ (bpm *4f / 60f) *1000f);
	}
	
	/**
	 * Load the warning message from file.
	 * @return
	 */
	private static final String readMessage() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Drummer.class.getResourceAsStream("/warning.txt")));
			String warningMsg = new String();
			while(reader.ready()) {
				warningMsg += reader.readLine();
			}
			reader.close();
			return warningMsg;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "Failed to retrieve warning";
	}
	
	private static final boolean shouldShowMessage() {
		return !(new File(messageLockPath).exists());
	}
	
	private static final void disableMessage() {
		File messageLock = new File(messageLockPath);
		if(messageLock.exists()) {
			return;
		}
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(messageLock)));
			writer.println("AutoDuck checks if this file exists to disable the warning popup.");
			writer.flush();
			writer.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Store the key binds the user has set in a file
	 * @param drums
	 */
	protected static void storeKeyBinds(Drum[] drums) {
		try {
			File keyBindsFile = new File(keyBindsPath);
			if(keyBindsFile.exists()) {
				keyBindsFile.delete();
			}
			
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(keyBindsPath)));
			writer.println("These are the keybinds for AutoDuck: Drummer. "
						+ "You can change them, but using the application would be easier. "
						+ "(This line is ignored by the application)");
			writer.println("INDEX:NAME:KEYID");
			for(int i=0; i<drums.length; i++) {
				Drum d = drums[i];
				writer.println(String.format("%d:%s:%d", d.getIndex(), d.getName(), d.getKeyID()));
			}
			writer.flush();
			writer.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieve the keybinds the user has set previously, from a file
	 * @param drums
	 */
	protected static void retrieveKeyBinds(Drum[] drums) {
		try {
			boolean corruptFile = false;
			File keyBindsFile = new File(keyBindsPath);
			if(!keyBindsFile.exists()) {
				return;
			}
			BufferedReader reader = new BufferedReader(new FileReader(keyBindsPath));
			final int lineSkips = 2;
			for(int i=0; i<lineSkips; i++) {
				reader.readLine();
			}
			int[] keyIDs = new int[drums.length];
			for(int i=0; i<drums.length; i++) {
				String line = reader.readLine();
				String[] pieces = line.split(":");
				if(pieces.length != 3) {
					corruptFile = true;
					break;
				}
				try {	
					int index = Integer.parseInt(pieces[0]);
					if(index != i) {
						throw new Exception();
					}
					int key = Integer.parseInt(pieces[2]);
					keyIDs[i] = key;
				}catch(Exception e) {
					corruptFile = true;
					break;
				}
			}
			reader.close();
			if(corruptFile) {
				keyBindsFile.delete();
			}else{
				for(int i=0; i<keyIDs.length; i++) {
					drums[i].setKeyId(keyIDs[i]);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void storeBeats(String name) {
		String beatsString = beatPanel.getBeats();
		try {
			File beatsFile = new File(beatsPath + "\\" + name + ".txt");
			if (beatsFile.exists()) {
				beatsFile.delete();
			}
			File beatsFolder = new File(beatsPath);
			beatsFolder.mkdirs();
			beatsFile.createNewFile();
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(beatsPath + "\\" + name + ".txt")));
			writer.println("These are the keybinds for AutoDuck: Drummer. "
					+ "You can change them, but using the application would be easier. "
					+ "(This line is ignored by the application)");
			writer.println((int) getBPM() + "," + getNumBeats());
			for (String line : beatsString.split(";")) {
				writer.println(line);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void retrieveBeats(String name) {
		StringBuilder beatString = new StringBuilder();
		try {
			boolean corruptFile = false;
			File beatsFile = new File(beatsPath + "\\" + name + ".txt");
			if (!beatsFile.exists()) {
				return;
			}
			Scanner scanner = new Scanner(new FileReader(beatsPath + "\\" + name + ".txt"));
			final int lineSkips = 1;
			for (int i = 0; i < lineSkips; i++) {
				if (!scanner.hasNextLine()) {
					corruptFile = true;
					break;
				}
				scanner.nextLine();
			}
			
			String[] meta = scanner.nextLine().split(",");
			setBPM(Float.valueOf(meta[0]));
			controlPanel.updateBpm(Integer.valueOf(meta[0]));
			setNumBeats(Integer.parseInt(meta[1]));
			controlPanel.updateNumBeats(Integer.parseInt(meta[1]));
			while (scanner.hasNext() && !corruptFile) {
				beatString.append(scanner.nextLine() + ";");
			}
			scanner.close();
			if (corruptFile) {
//				beatsFile.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		beatPanel.setBeats(beatString.toString().split(";"));
	}
	
	protected void clearBoxes() {
		beatPanel.clearBoxes();
	}
	
	protected ControlPanel getControlPanel() {
		return controlPanel;
	}
	
	public static int getActivatekey() {
		return activateKey;
	}
	
	public ArrayList<String> getFileList() {
		ArrayList<String> returnList = new ArrayList<>();
		File folder = new File(beatsPath);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        returnList.add(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().indexOf(".txt")));
		      }
		    }
		
		return returnList;
	}

	protected JFrame getFrame() {
		return frame;
	}
	
	protected void setKeyListener(KeyListener keyListener) {
		this.keyListener = keyListener;
	}
	
	private static final List<Component> getAllComponents(final Container c) {
	    Component[] comps = c.getComponents();
	    List<Component> compList = new ArrayList<Component>();
	    for (Component comp : comps) {
	        compList.add(comp);
	        if (comp instanceof Container)
	            compList.addAll(getAllComponents((Container) comp));
	    }
	    return compList;
	}
}
