import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

public class Tutorial extends EvolutionFrame implements ActionListener,
		Runnable {
	CardLayout layout;
	JPanel cardContainer;
	public static final int tutorials = 5;
	int tutorialNum = 0;
	JProgressBar progress;
	JFrame progressFrame;

	public Tutorial() {
		// JFrame stuff
		super("Tutorial");
		setSize(1100, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		progress = new JProgressBar(0, tutorials);
		progress.setStringPainted(true);
		progressFrame = new JFrame("Tutorial");
		progressFrame.setSize(200, 70);
		progressFrame.setLayout(new BorderLayout());
		progressFrame.add(new JLabel("Please wait... Loading Tutorial"),
				BorderLayout.NORTH);
		progressFrame.add(progress);
		progressFrame.setVisible(true);

		new Thread(this).start();
	}

	public void run() {
		layout = new CardLayout();
		cardContainer = new JPanel(layout);
		TutorialPanel current = new MovementTutorial(this);
		TutorialPanel next = null;
		int i = 0;
		do {
			cardContainer.add(current.container, "nonsense");
			next = current.next();
			current = next;
			i++;
			progress.setValue(i);
			try {
				Thread.sleep(0, 1);
			} catch (InterruptedException e) {
			}
		} while (current != null);
		add(cardContainer, BorderLayout.CENTER);

		JButton nextB = new JButton("Next Lesson >>");
		nextB.addActionListener(this);
		add(nextB, BorderLayout.SOUTH);
		progressFrame.dispose();
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (tutorialNum < tutorials) {
			layout.next(cardContainer);
			tutorialNum++;
		} else {
			JOptionPane
					.showMessageDialog(this,
							"Congradulations on finishing the tutorial! Now play the real game!");
			dispose();
		}
	}

	class TutorialPanel extends GamePanel implements MouseListener {
		String message;
		JPanel container;
		boolean started;

		public TutorialPanel(EvolutionFrame owner, String initialMessage) {
			frame = owner;
			message = initialMessage;
			repaint();
			addMouseListener(this);
			container = this;
			started = false;
		}

		public void update() {
			if (started) {
				updatePlayers(true);
				updatePredators();
				updatePrey();
				updatePlants();
				miniMap.redraw();
				repaintAll();
			}
		}

		public void focusLost(FocusEvent e) {
			focused = false;
		}

		public void resetMessage() {
			frame
					.setText("Click on the screen to start after reading the message! Then press next when you are ready to move on.");
		}

		public void paintComponent(Graphics g) {
			if (!started || !focused) {
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.RED);
				String text = "CLICK HERE TO BEGIN (AFTER READING THE MESSAGE BELOW)";
				g.drawString(text, (getWidth() - g.getFontMetrics(g.getFont())
						.stringWidth(text)) / 2, getHeight() / 2 - 20);
				g.setColor(Color.BLACK);
				g
						.drawString(message, (getWidth() - g.getFontMetrics(
								g.getFont()).stringWidth(message)) / 2,
								getHeight() / 2);
			} else {
				repaintAll();
			}
		}

		public void loadBasicMap(EvolutionFrame owner) {
			setToCustom(owner, 0, StartScreen.background, 1000, 1000, 30,
					new ArrayList<Organism>(Arrays.asList( standardPredator(50, 50))), new ArrayList<Organism>(Arrays.asList(
							standardPrey(900, 100), standardPrey(400, 400),
							standardPrey(900, 600), standardPrey(500, 300) )),
					new Autotroph[] { new Autotroph(this, 50, 50) });
			plants = new Autotroph[] { new Autotroph(this, 50, 50),
					new Autotroph(this, 50, 50), new Autotroph(this, 50, 50),
					new Autotroph(this, 50, 50) };
		}

		public void loadBasicMapWithMiniMap(EvolutionFrame owner) {
			miniMap = new MiniMap(this);
			loadBasicMap(owner);
			container = new JPanel();
			container.setLayout(new BorderLayout());
			container.add(miniMap, BorderLayout.WEST);
			container.add(this, BorderLayout.CENTER);
		}

		public TutorialPanel next() {
			return null;
		}

		public void recordData() {

		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			requestFocus();
			reAddAllViewports();
			started = true;
		}

		public void mouseReleased(MouseEvent e) {
		}
	}

	class MovementTutorial extends TutorialPanel {
		public MovementTutorial(EvolutionFrame owner) {
			super(
					owner,
					"Use the Left and Right arrow keys to turn. Use up and down to move back and forth. WASD is also available.");
			setToCustom(owner, 0, StartScreen.background, 1000, 1000, 30,
					new ArrayList<Organism>(Arrays.asList(standardPredator(50, 50) )), null,
					new Autotroph[] { new Autotroph(this, 0, 0) });
			ports[0].showBackground = false;
		}

		public void update() {
			updatePlayers(true);
			repaint();
		}

		public TutorialPanel next() {
			return new VisionTutorial(frame);
		}
	}

	class VisionTutorial extends TutorialPanel {
		public VisionTutorial(EvolutionFrame owner) {
			super(
					owner,
					"You can only see things with in your yellow field of view with the exception of trees. Try looking for small yellow birds and green objects (plants).");
			loadBasicMap(owner);
		}

		public void update() {
			updatePlayers(true);
			for (int i = 0; i < prey.size(); i++)
				((Prey) prey.get(i)).flapWings();
			repaint();
		}

		public TutorialPanel next() {
			return new SearchTutorial(frame);
		}
	}

	class SearchTutorial extends TutorialPanel {
		public SearchTutorial(EvolutionFrame owner) {
			super(
					owner,
					"This time the birds will fly around. Try to find them... and run into them! If you eat enough you can have a baby!");
			loadBasicMap(owner);
		}

		public void update() {
			updatePlayers(true);
			updatePrey();
			repaint();
		}

		public TutorialPanel next() {
			return new MiniMapTutorial(frame);
		}
	}

	class MiniMapTutorial extends TutorialPanel {
		public MiniMapTutorial(EvolutionFrame owner) {
			super(
					owner,
					"There is a mini map on the left. Click anywhere on it to move there. You can use the slider or scroll wheel to zoom in on the map.");
			loadBasicMapWithMiniMap(owner);
		}

		public TutorialPanel next() {
			return new FirstPersonTutorial(frame);
		}
	}

	class FirstPersonTutorial extends TutorialPanel {
		public FirstPersonTutorial(EvolutionFrame owner) {
			super(
					owner,
					"Press Backspace to toggle back and forth between first person and top down views.");
			loadBasicMapWithMiniMap(owner);
		}

		public TutorialPanel next() {
			return new MultiPlayerTutorial(frame);
		}
	}

	class MultiPlayerTutorial extends TutorialPanel {
		public MultiPlayerTutorial(EvolutionFrame owner) {
			super(
					owner,
					"To play with your friends, press F2 or F3 when there is another predator alive. Press F1 to remove them.");
			loadBasicMapWithMiniMap(owner);
			predators = new ArrayList<Organism>(Arrays.asList( standardPredator(randomPosition(),
					randomPosition()) ));
		}
	}
}