/**
 * AutoDuck Drummer by Ronan Hanley
 * Feb. 2016
 */

package duck_drummer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class BeatPanel extends JPanel {
	private static final long serialVersionUID = 8356013385702893589L;
	private int numBeats = 0;
	private final Drum[] drums = new Drum[] { new Drum("Low tom", KeyEvent.VK_A, 0),
			new Drum("Mid tom", KeyEvent.VK_S, 1), new Drum("High tom", KeyEvent.VK_D, 2),
			new Drum("Hi hat", KeyEvent.VK_W, 3), new Drum("Symbol", KeyEvent.VK_3, 4),
			new Drum("Base drum", KeyEvent.VK_Q, 5), new Drum("Snare", KeyEvent.VK_V, 6),
			new Drum("Quack", KeyEvent.VK_E, 7) };
	private final ArrayList<ArrayList<JCheckBox>> boxes;
	private Robot robot = null;
	private JButton[] keyChangers;
	private boolean waitingForKeyPress = false;
	private int keyToChange = 0;

	public BeatPanel(int initialNumBeats, Drummer drummer) {
		Drummer.retrieveKeyBinds(drums);
		setLayout(new GridBagLayout());
		GridBagConstraints c;
		JLabel keyTip = new JLabel(
				"<html><center>></center>Press<br>button<br>to change<br>key<br><center>></center></html>");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 0, 0, 7);
		add(keyTip, c);

		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				for (int i = 0; i < keyChangers.length; i++) {
					if (keyChangers[i] == ((JButton) ae.getSource())) {
						keyToChange = i;
						break;
					}
				}
				waitingForKeyPress = true;
				drummer.getControlPanel().setPromptVisibile(true);
				drummer.getControlPanel().releaseFocus();
			}
		};

		keyChangers = new JButton[drums.length];
		for (int i = 0; i < keyChangers.length; i++) {
			keyChangers[i] = new JButton(drums[i].getAssignedKey());
			keyChangers[i].addActionListener(buttonListener);
			keyChangers[i].setFocusable(false);
		}
		boxes = new ArrayList<ArrayList<JCheckBox>>();
		for (int j = 0; j < drums.length; j++) {
			boxes.add(new ArrayList<JCheckBox>());
		}
		for (int j = 0; j < keyChangers.length; j++) {
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = j;
			add(keyChangers[j], c);
		}
		for (int j = 0; j < drums.length; j++) {
			c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = j;
			add(new JLabel(drums[j].getName()), c);
		}
		updateNumBeats(initialNumBeats);

		JSeparator separator = new JSeparator();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = drums.length;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(separator, c);

		KeyListener keyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent ke) {
				if (waitingForKeyPress) {
					drums[keyToChange].setKeyId(ke.getKeyCode());
					keyChangers[keyToChange].setText(drums[keyToChange].getAssignedKey());
					waitingForKeyPress = false;
					drummer.getControlPanel().setPromptVisibile(false);
					Drummer.storeKeyBinds(drums);
				}
			}

			@Override
			public void keyReleased(KeyEvent ke) {
			}

			@Override
			public void keyTyped(KeyEvent ke) {
			}
		};

		drummer.setKeyListener(keyListener);

		try {
			robot = new Robot();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simulates the key presses for a column of beats, effectivly playing 1
	 * beat.
	 * 
	 * @param id
	 * @param restMs
	 */
	public void playColumn(int id, int restMs) {
		for (int y = 0; y < boxes.size(); y++) {
			if (boxes.get(y).get(id).isSelected()) {
				robot.keyPress(drums[y].getKeyID());
			}
		}
		try {
			Thread.sleep(restMs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int y = 0; y < boxes.size(); y++) {
			if (boxes.get(y).get(id).isSelected()) {
				robot.keyRelease(drums[y].getKeyID());
			}
		}
	}

	public void updateNumBeats(int newNumBeats) {
		int oldNumBeats = numBeats;
		numBeats = newNumBeats;
		if (newNumBeats == oldNumBeats) {
			return;
		}
		if (newNumBeats < oldNumBeats) {
			for (int j = 0; j < boxes.size(); j++) {
				for (int i = oldNumBeats - 1; i >= newNumBeats; i--) {
					remove(boxes.get(j).get(i));
					boxes.get(j).remove(i);
					revalidate();
				}
			}
		} else {
			Color[] colorGroups = new Color[2];
			colorGroups[1] = Color.LIGHT_GRAY;
			colorGroups[0] = new Color(colorGroups[1].getRGB() + 0x131313);
			GridBagConstraints c;
			for (int j = 0; j < drums.length; j++) {
				for (int i = oldNumBeats; i < newNumBeats; i++) {
					byte beatGroup = (byte) ((i / 4) % 2);
					Color groupColor = colorGroups[beatGroup];
					c = new GridBagConstraints();
					c.gridx = 3 + i;
					c.gridy = j;
					c.anchor = GridBagConstraints.CENTER;
					c.fill = GridBagConstraints.BOTH;
					JCheckBox cBox = new JCheckBox();
					boxes.get(j).add(cBox);
					cBox.setFocusable(false);
					boxes.get(j).get(i).setBackground(groupColor);
					add(boxes.get(j).get(i), c);
				}
			}
		}
	}

	protected void clearBoxes() {
		for (ArrayList<JCheckBox> beats : boxes) {
			for (JCheckBox box : beats) {
				box.setSelected(false);
			}
		
		}
	}
	
	protected String getBeats() {
			StringBuilder beatsString = new StringBuilder();
			for (ArrayList<JCheckBox> beats : boxes) {
				for (JCheckBox cBox : beats) {
					if (cBox.isSelected()) {
						beatsString.append("1,");
					} else {
						beatsString.append("0,");
					}
				}
				beatsString.append(";");
			}
			return beatsString.toString();
	}

	protected void setBeats(String[] beatStrings) {
		for (int i=0; i<beatStrings.length; i++) {
			String[] beatForDrum = beatStrings[i].split(",");
			int counter = 0;
			ArrayList<JCheckBox> checkboxesForDrum = boxes.get(i);
			for (JCheckBox box : checkboxesForDrum) {
				if ( beatForDrum[counter].equals("1")) {
					box.setSelected(true);
				} else {
					box.setSelected(false);
				}
				counter++;
			}
		}
	}
}
