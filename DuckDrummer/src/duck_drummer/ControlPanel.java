/**
 * AutoDuck Drummer by Ronan Hanley
 * Feb. 2016
 */

package duck_drummer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ControlPanel extends JPanel {
	private static final long serialVersionUID = -7535525670176986072L;
	private JTextField bpmField;
	private JTextField numBeatsField;
	private JTextField filenameField;
	private Drummer drummer;
	private JLabel keyPrompt;
	private JLabel bpmLabel;
	private JComboBox<String> fileList;

	private JButton playPauseButton;
	private JButton saveButton;
	private JButton loadButton;
	private JButton clearButton;

	public ControlPanel(Drummer drummer) {
		this.drummer = drummer;
		populate();
		fileList.setSelectedItem("New File");
	}

	/**
	 * Add all the needed elements to the panel
	 */
	public void populate() {
		FlowLayout flowLayout = new FlowLayout();
		setLayout(flowLayout);

		keyPrompt = new JLabel("Press a key...");
		keyPrompt.setBackground(Color.WHITE);
		keyPrompt.setForeground(Color.BLUE);
		add(keyPrompt);
		keyPrompt.setVisible(false);
		add(Box.createRigidArea(new Dimension(30, 0)));

		playPauseButton = new JButton(">");
		playPauseButton.setToolTipText("ALT + " + (char) Drummer.getActivatekey());
		playPauseButton.setMargin(new Insets(0, 2, 0, 2));
		playPauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				drummer.onHotKey(1);
			}
		});
		add(playPauseButton);

		bpmLabel = new JLabel("BPM :");
		add(bpmLabel);
		bpmField = new JTextField(3);
		bpmField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				update();
			}

			public void update() {
				String text = bpmField.getText();
				boolean isFloat;
				float textFloat = 0f;
				try {
					textFloat = Float.parseFloat(text);
					isFloat = true;
				} catch (Exception e) {
					isFloat = false;
				}

				boolean success = false;
				if (isFloat) {
					success = drummer.setBPM(textFloat);
				}

				if (!success) {
					Toolkit.getDefaultToolkit().beep();
					drummer.setBPM(drummer.getDefaultBpm());
					bpmField.setText(Float.toString(drummer.getDefaultBpm()));
				}
			}

		});
		bpmField.setText(Float.toString(drummer.getBPM()));
		add(bpmField);

		JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
		Dimension size = new Dimension(1, bpmField.getPreferredSize().height);
		sep.setPreferredSize(size);
		add(sep);

		JLabel numBeatsLabel = new JLabel("No. Beats :");
		add(numBeatsLabel);
		numBeatsField = new JTextField(3);
		numBeatsField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				update();
			}

			public void update() {
				String text = numBeatsField.getText();
				boolean isInt;
				int textNum = 0;
				try {
					textNum = Integer.parseInt(text);
					isInt = true;
				} catch (Exception e) {
					isInt = false;
				}

				boolean success = false;
				if (isInt) {
					success = drummer.setNumBeats(textNum);
				}

				if (!success) {
					Toolkit.getDefaultToolkit().beep();
					numBeatsField.setText(Integer.toString(drummer.getNumBeats()));
				}
			}

		});
		numBeatsField.setText(Integer.toString(drummer.getNumBeats()));
		add(numBeatsField);

		JSeparator sep2 = new JSeparator(SwingConstants.VERTICAL);
		sep2.setPreferredSize(size);
		add(sep2);

		JLabel fileNameLabel = new JLabel("File Name :");
		add(fileNameLabel);

		fileList = new JComboBox<String>(new String[] { "New File" });
		fileList.setEditable(true);
		add(fileList);

		saveButton = new JButton("S");
		saveButton.setToolTipText("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				drummer.storeBeats(filenameField.getText());
				updateFileList();
			}
		});
		add(saveButton);

		loadButton = new JButton("L");
		loadButton.setToolTipText("Load");
		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				drummer.retrieveBeats(fileList.getSelectedItem().toString());
				updateFileList();
			}
		});
		add(loadButton);

		clearButton = new JButton("C");
		clearButton.setToolTipText("Clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				drummer.clearBoxes();
			}
		});
		add(clearButton);
	}

	public void updateBpm(int bpm) {
		bpmField.setText(String.valueOf(bpm));
	}

	public void updateNumBeats(int numBeats) {
		numBeatsField.setText(String.valueOf(numBeats));
	}

	public void updatePlayPause(boolean running) {
		if (running) {
			playPauseButton.setText("||");
		} else {
			playPauseButton.setText(">");
		}
	}

	public void setPromptVisibile(boolean visible) {
		keyPrompt.setVisible(visible);
	}

	protected void releaseFocus() {
		if (numBeatsField.isFocusOwner() || bpmField.isFocusOwner()) {
			bpmLabel.requestFocusInWindow();
		}
	}

	public JComboBox<String> getFileList() {
		return fileList;
	}

	public void setFileList(JComboBox<String> fileList) {
		this.fileList = fileList;
	}

	public void updateFileList() {
		fileList.removeAllItems();
		for (String file : drummer.getFileList()) {
			fileList.addItem(file);
		}
	}
}
