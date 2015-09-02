import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class StartScreen extends SinePanel implements ActionListener {
	static StepsPanel step;
	static final Color invisible = new Color(0, 0, 0, 0);
	float variation = 0.1f;
	int width = 5100;
	int height = 5100;
	double plantWidth = 60;
	double plantsPercentage = 0.03; // how much percentage of the screen the

	// plants

	// should take

	public StartScreen() {
		Evolution.staticThis
				.setText("Welcome to the Evolution Game! Select your settings and hit start!");

		String[] lowMedHigh = new String[] { "Low", "Medium", "High" }; // commonly
		String[] smallMedLarg = new String[] { "Small", "Medium", "Large",
				"Giant", "Humongous", "Lag fest" };
		setBackground(Color.WHITE);
		setLayout(new BorderLayout(10, 0));
		// used
		// items
		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center
				.add(new componentLabel(this, "Map Size", smallMedLarg, "map",
						1));
		center.add(new componentLabel(this, "Genetic variation between generations",
				lowMedHigh, "variation", 0));
		center.add(new componentLabel(this, "Plant population", new String[] {
				"Low", "Medium", "High", "Everywhere" }, "plants", 1));
		center.add(new componentLabel(this, "Plant size", new String[] {
				"Small", "Medium", "Large" }, "pSize", 0));
		JButton start = new JButton("Start");
		start.setActionCommand("start");
		start.addActionListener(this);
		center.add(start);
		center.setBackground(invisible);
		add(center, BorderLayout.CENTER);

		// add stuff
		JPanel east = new JPanel(new BorderLayout(0, 10));
		east.setBackground(invisible);
		step = new StepsPanel(this);
		east.add(step, BorderLayout.CENTER);
		JPanel northEast = new JPanel();
		northEast.setBackground(invisible);
		JButton howToPlay = new JButton("How to Play");
		howToPlay.setActionCommand("play");
		howToPlay.addActionListener(this);
		northEast.add(howToPlay);
		east.add(northEast, BorderLayout.NORTH);
		add(east, BorderLayout.EAST);
	}

	public static final Color background = new Color(128, 64, 0);

	public void start() {
		step.reset();
		step.setStep(1);
		new CardContainer(variation, background, width, height, plantWidth,
				plantsPercentage);
	}

	public void stopLoading() {
		CardContainer.newest.dispose();
		CardContainer.loading = false;
		CardContainer.newest = null;
	}

	public void actionPerformed(ActionEvent e) {
		requestFocus();
		if (e.getActionCommand().equals("start")) {
			if (CardContainer.loading) {
				if (JOptionPane.showConfirmDialog(Evolution.staticThis,
						"Stop loading the current game?", "Are you sure?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					if (CardContainer.loading)
						stopLoading();
					start();
				}
			} else {
				start();
			}

		} else if (e.getActionCommand().equals("play"))
			new Tutorial();
		else if (e.getActionCommand().equals("cancel")) {
			// canel loading
			if (CardContainer.loading) {
				stopLoading();
				step.reset();
			}
		} else {
			if (!CardContainer.loading)
				step.reset();
			if (e.getActionCommand().equals("variation")) { // set amount of
				// varaition
				switch (((JComboBox) e.getSource()).getSelectedIndex()) {
				case 0:
					variation = 0.1f;
					break;
				case 1:
					variation = 0.15f;
					break;
				case 2:
					variation = 0.2f;
					break;
				}
			} else if (e.getActionCommand().equals("plants")) { // set plant
				// percent
				switch (((JComboBox) e.getSource()).getSelectedIndex()) {
				case 0:
					plantsPercentage = 0.03;
					break;
				case 1:
					plantsPercentage = 0.055;
					break;
				case 3:
					plantsPercentage = 0.8;
					break;
				default:
					plantsPercentage = 1;
				}
			} else if (e.getActionCommand().equals("pSize")) { // set plant size
				switch (((JComboBox) e.getSource()).getSelectedIndex()) {
				case 0:
					plantWidth = 60;
					break;
				case 1:
					plantWidth = 100;
					break;
				case 3:
					plantWidth = 130;
					break;

				}
			} else if (e.getActionCommand().equals("map")) { // set size
				switch (((JComboBox) e.getSource()).getSelectedIndex()) {
				case 0: {
					width = 4000;
					break;
				}
				case 1: {
					width = 5000;
					break;
				}
				case 2: {
					width = 6000;
					break;
				}
				case 3: {
					width = 7000;
					break;
				}
				case 4: {
					width = 8000;
					break;
				}
				case 5: {
					width = 100000;
					break;
				}
				}
				height = width;
			}
		}
	}

	class componentLabel extends JPanel {
		public componentLabel(ActionListener toAdd, String label,
				String[] comboStrings, String actionCommand, int startIndex) {
			setBackground(invisible);
			// used to shorten repetitive code
			JComboBox jComp = new JComboBox(comboStrings);
			jComp.setSelectedIndex(startIndex);
			jComp.setActionCommand(actionCommand);
			jComp.addActionListener(toAdd);
			add(new JLabel(label));
			add(jComp);
		}
	}

	class Step extends JPanel {
		public JProgressBar p;
		JLabel label;

		public Step(int number, String instruction) {
			setBackground(invisible);
			label = new JLabel("Step " + number + " : " + instruction);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			p = new JProgressBar(0, 10);
			add(label);
			add(p);
		}

		public void reset() {
			p.setStringPainted(false);
			label.setVisible(false);
			p.setVisible(false);
			p.setIndeterminate(true);
		}

		public void finish() {
			show1();
			setMax(1);
			p.setValue(1);
		}

		public void show1() {
			p.setVisible(true);
			label.setVisible(true);
		}

		public void setMax(int max) {
			p.setIndeterminate(false);
			p.setMaximum(max);
			p.setStringPainted(true);
		}

		public void setValue(int value) {
			p.setValue(value);
		}
	}

	class StepsPanel extends JPanel {
		Step[] steps;
		int stepIndex = 0;
		JButton cancel;

		public StepsPanel(StartScreen ss) {
			setBackground(invisible);
			cancel = new JButton("Cancel Loading");
			cancel.setActionCommand("cancel");
			cancel.addActionListener(ss);
			steps = new Step[] {
					new Step(1, "Pick your settings and hit start!"),
					new Step(2, "Loading Game Predators..."),
					new Step(3, "Loading Game Prey..."),
					new Step(4, "Loading Game Plants..."),
					new Step(5, "Have Fun Learning and Playing!") };
			setLayout(new GridLayout(0, 1));
			add(steps[0]);
			JPanel temp = new JPanel();
			temp.setBackground(invisible);
			temp.add(cancel);
			add(temp);
			for (int i = 1; i < steps.length; i++)
				add(steps[i]);
			reset();
		}

		public Step current() {
			return steps[stepIndex];
		}

		public void reset() {
			for (int i = 0; i < steps.length; i++)
				steps[i].reset();
			setStep(0);
			repaint();
		}

		public void setStep(int index) {
			for (int i = 0; i < index; i++)
				steps[i].finish();
			stepIndex = index;
			cancel.setVisible(index > 0);
			cancel.setEnabled(index < 4);
			current().show1();
		}

		public void setMax(int i) {
			current().setMax(i);
		}

		public void setValue(int i) {
			current().setValue(i);
		}
	}
}