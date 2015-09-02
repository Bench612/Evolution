import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

public class GamePanel extends JPanel implements Runnable, KeyListener,
		FocusListener, AdjustmentListener, ChangeListener, ActionListener {
	public int runSpeed = 25;

	public static double normalAutoEnergyGain = 0.27; // suns energy given to
	// plants
	// per pixel
	public double autoEnergyGain;
	public static int[][] defaultControls = new int[][] {
			{ KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D,
					KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_Q, KeyEvent.VK_E },
			{ KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN,
					KeyEvent.VK_RIGHT, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT,
					KeyEvent.VK_ENTER },
			{ KeyEvent.VK_I, KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L,
					KeyEvent.VK_SEMICOLON, KeyEvent.VK_U, KeyEvent.VK_O },
			{ KeyEvent.VK_HOME, KeyEvent.VK_DELETE, KeyEvent.VK_END,
					KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_INSERT,
					KeyEvent.VK_PAGE_UP, KeyEvent.VK_BACK_SLASH } };
	public static int[][] controls = defaultControls;
	public Image predatorImg;
	public Image preyImg;
	int preyCreated = 0;
	int predatorsCreated = 0;

	public float variationLevel;

	public double mapWidth;
	public double mapHeight;

	public Color terrain;
	public static Color viewColor = Color.yellow;

	public EvolutionFrame frame;
	public MiniMap miniMap;
	public boolean flagged;
	public boolean goThrough;
	public double mainFlagX;
	public double mainFlagY;

	public BasicStats stats;

	public boolean focused;
	public ArrayList<Organism> predators;
	public ArrayList<Organism> prey;
	public Autotroph[] plants;

	public boolean fastForward = false;

	// first is prey / pred second is time third is thingymabob
	// first is pred
	public double[][][] sizeData;
	public double[][][] speedData;
	public double[][][] sprintData;
	public double[][][] eyeData;
	public double[][] averageSizes;
	public double[][] averageSpeeds;
	public double[][] averageSprints;
	public double[][] averageEyes;
	public double[][] populations;
	public int maxIndex;
	public static int timeToRecord = 40;
	public double timeSinceLastRecord;
	public static double recordPer = 15; // in seconds
	public int timesShifted = 0;

	public double[][] trunkX;
	public double[][] trunkY;
	public double[][] trunkZ;
	public double[][] leafX;
	public double[][] leafZ;
	public double[][] leafY;

	static double startSize = 32;
	static double startEyes = Math.toRadians(160);
	static double startWalk = 0.09;
	static double startSprint = 0.2;
	int playType;
	public static final int NORMAL = 0;
	public static final int TRANQ = 1;
	public static final int ALONE = 2;
	Viewport[] ports;
	JPanel cardContainer; // not same as the class CardContainer to get class,
	// use frame
	CardLayout cardLayout;
	JPanel messageContainer;

	public Star[] stars;

	public ArrayList<Organism> players() {
		ArrayList<Organism> returnValue = new ArrayList<Organism>(ports.length);
		for (int i = 0; i < ports.length; i++)
			returnValue.add(ports[i].player);
		return returnValue;
	}

	public void setAllViewModes(int viewMode) {
		for (int i = 0; i < ports.length; i++)
			ports[i].viewMode = viewMode;
	}

	public void recordData() {
		maxIndex++;
		recordData(Organism.add(players(), predators), 0, maxIndex);
		if (prey != null)
			recordData(prey, 1, maxIndex);
		if (maxIndex >= timeToRecord) {
			for (int b = 0; b < averageSizes.length; b++) {
				for (int i = 0; i < timeToRecord - 1; i++) {
					averageSizes[b][i] = averageSizes[b][i + 1];
					averageSpeeds[b][i] = averageSpeeds[b][i + 1];
					averageSprints[b][i] = averageSprints[b][i + 1];
					averageEyes[b][i] = averageEyes[b][i + 1];
					populations[b][i] = populations[b][i + 1];
				}
			}
			timesShifted++;
			maxIndex = timeToRecord - 1;
		}
		for (int b = 0; b < averageSizes.length; b++) {
			averageSizes[b][maxIndex] = getAverage(sizeData[b][maxIndex]);
			averageSpeeds[b][maxIndex] = getAverage(speedData[b][maxIndex]);
			averageSprints[b][maxIndex] = getAverage(sprintData[b][maxIndex]);
			averageEyes[b][maxIndex] = getAverage(eyeData[b][maxIndex]);
		}
		populations[0][maxIndex] = predatorsAlive();
		populations[1][maxIndex] = preyAlive();
	}

	public int predatorsAlive() {
		if (predators != null && ports != null)
			return predators.size() + ports.length;
		else
			return 0;
	}

	public int preyAlive() {
		if (prey != null)
			return prey.size();
		return 0;
	}

	public static double getAverage(double[] d) {
		double total = 0;
		for (int i = 0; i < d.length; i++)
			total += d[i];
		return total / d.length;
	}

	private void recordData(ArrayList<Organism> o, int i1, int indexToRecord) {
		if (indexToRecord >= timeToRecord) {
			for (int i = 0; i < timeToRecord - 1; i++) {
				sizeData[i1][i] = sizeData[i1][i + 1];
				speedData[i1][i] = speedData[i1][i + 1];
				sprintData[i1][i] = sprintData[i1][i + 1];
				eyeData[i1][i] = eyeData[i1][i + 1];
			}
			indexToRecord = timeToRecord - 1;
		}
		sizeData[i1][indexToRecord] = new double[o.size()];
		speedData[i1][indexToRecord] = new double[o.size()];
		sprintData[i1][indexToRecord] = new double[o.size()];
		eyeData[i1][indexToRecord] = new double[o.size()];
		for (int i = 0; i < o.size(); i++) {
			sizeData[i1][indexToRecord][i] = o.get(i).getSize();
			speedData[i1][indexToRecord][i] = o.get(i).getSpeed();
			sprintData[i1][indexToRecord][i] = o.get(i).getSprint();
			eyeData[i1][indexToRecord][i] = o.get(i).getEyes();
		}
	}

	class StartThread extends Thread {
		GamePanel gp;
		double plantPercent;
		double plantSize;

		public StartThread(GamePanel g, double perc, double size) {
			int prey = (int) (g.mapWidth / 2000);
			con(g,
					perc,
					size,
					(int) ((Math.pow(g.mapWidth, 2) / Math.pow(size, 2)) * perc),
					prey * 20, prey);
		}

		public StartThread(GamePanel g, double prec, double size, int plants,
				int prey, int numPreds) {
			con(g, prec, size, plants, prey, numPreds);
		}

		private void con(GamePanel g, double perc, double size, int plants,
				int prey, int numPreds) {
			preds = numPreds;
			numPrey = prey;
			gp = g;
			plantPercent = perc;
			plantSize = size;
			StartScreen.step.setStep(1);
			StartScreen.step.setMax(preds);
			gp.plants = new Autotroph[plants];
		}

		int index = 0;
		int i = 0;
		int preds;
		int numPrey;

		private void setValue(int a) {
			StartScreen.step.setValue(a);
		}

		private void nextIndex() {
			index++;
			i = 0;
		}

		public void run() {
			while (CardContainer.loading && gp.frame == CardContainer.newest) {
				Color c = Color.WHITE;
				// predators
				if (index == 0) {
					if (i < preds) {
						// for each predator
						new Predator(gp, randomPosition(), randomPosition(),
								Organism.close(gp, startSize), Organism.close(
										gp, startWalk), Organism.close(gp,
										startSprint), Organism.close(gp,
										startEyes, Organism.minSightAngle,
										Organism.maxSightAngle), c);
						setValue(i);
						i++;
					} else {
						StartScreen.step.setStep(2);
						StartScreen.step.setMax(numPrey);
						nextIndex();
					}
				} else if (index == 1) {
					// prey
					if (i < numPrey) {
						new Prey(gp, randomPosition(), randomPosition(),
								Organism.close(gp, startSize), Organism.close(
										gp, startWalk), Organism.close(gp,
										startSprint), Organism.close(gp,
										startEyes, Organism.minSightAngle,
										Organism.maxSightAngle), c);
						setValue(i);
						i++;
					} else {
						StartScreen.step.setStep(3);
						StartScreen.step.setMax(gp.plants.length);
						nextIndex();
					}
				} else {
					// plants
					if (i < gp.plants.length) {
						gp.plants[i] = new Autotroph(gp, plantSize, plantSize);
						StartScreen.step.setValue(i);
						i++;
					} else {
						gp.recordData();
						gp.frame.setVisible(true);
						StartScreen.step.setStep(4);
						StartScreen.step.current().p.setStringPainted(true);
						StartScreen.step.current().p
								.setString("Never Finished!");
						CardContainer.loading = false;// basicly breaks
					}
				}
				try {
					Thread.sleep(0, 1);
				} catch (InterruptedException e) {
				}
			}
			gp.plants[0].load(gp);
		}
	}

	public void resetDefaultVariables(EvolutionFrame owner, double theMapWidth,
			double theMapHeight, float variation, Color background) {
		variationLevel = variation;
		playType = NORMAL;
		frame = owner;
		autoEnergyGain = normalAutoEnergyGain;
		timeSinceLastRecord = 0;
		maxIndex = -1;
		predators = null;
		prey = null;
		plants = null;
		focused = false;
		goThrough = false;
		flagged = false;

		mapWidth = theMapWidth;
		mapHeight = theMapHeight;

		terrain = background;

		stars = new Star[(int) (Math.random() * 250 + 250)];
		for (int i = 0; i < stars.length; i++)
			stars[i] = new Star();

		predatorImg = Evolution.predatorImg;
		preyImg = Evolution.preyImg;
		setBackground(terrain);
		resetMessage();
		addFocusListener(this);
		addKeyListener(this);
	}

	public Predator standardPredator(double xP, double yP) {
		return new Predator(this, xP, yP, startSize, startWalk, startSprint,
				startEyes, Color.RED);
	}

	public Prey standardPrey(double xP, double yP) {
		return new Prey(this, xP, yP, startSize, startWalk, startSprint,
				startEyes, Color.RED);
	}

	public GamePanel() {
		// should cann set To Custom if using this constructer
		stats = new BasicStats(this);
	}

	public void setToCustom(EvolutionFrame owner, float variation,
			Color background, double theMapWidth, double theMapHeight,
			double plantSize, ArrayList<Organism> preds,
			ArrayList<Organism> thePrey, Autotroph[] thePlants) {
		resetDefaultVariables(owner, theMapWidth, theMapHeight, variation,
				background);
		plants = thePlants;
		plants[0].load(this);
		predators = preds;
		prey = thePrey;
		ports = new Viewport[] { new Viewport(this, controls[0],
				(Predator) preds.get(0), false) };
		new Thread(this).start();
	}

	public GamePanel(EvolutionFrame owner, float variation, Color background,
			double theMapWidth, double theMapHeight, double plantSize,
			double plantPercent) {
		stats = new BasicStats(this);
		populations = new double[2][timeToRecord];
		averageSizes = new double[2][timeToRecord];
		averageEyes = new double[2][timeToRecord];
		averageSpeeds = new double[2][timeToRecord];
		averageSprints = new double[2][timeToRecord];
		eyeData = new double[2][timeToRecord][1];
		sizeData = new double[2][timeToRecord][1];
		sprintData = new double[2][timeToRecord][1];
		speedData = new double[2][timeToRecord][1];
		resetDefaultVariables(owner, theMapWidth, theMapHeight, variation,
				background);

		// ports && creates 1st player
		ports = new Viewport[] { new Viewport(this, controls[0],
				standardPredator(randomPosition(), randomPosition()), false) };
		reAddAllViewports();

		JPanel messageContainerWButton = new JPanel(new BorderLayout());
		JPanel messageContainer = new JPanel(new GridLayout(0, 1, 0, 20));
		// controls , how to play, educational info
		messageContainer
				.add(new TextLabel(
						"Controls",
						"General Controls\n-Backspace = Toggle First Person\n-F1 = Remove Player\n-F2 = Add Top Down Player\n-F3 = Add First Person Player"
								+ "\n\nDEFAULT CONTROLS(May have been changed)\nPLAYER ONE CONTROLS\n-W = Move forward\n-S = Move backward\n-A = Turn left\n-D = Turn right\n-Caps Lock = Sprint"
								+ "\n\nPLAYER TWO CONTROLS\n-Up = Move forward\n-Down = Move backward\n-Left = Turn left\n-Right = Turn right\n-Control = Sprint"
								+ "\n\nPLAYER THREE CONTROLS\n-I = Move forward\n-K = Move backward\n-J = Turn left\n-L = Turn right\n-; = Sprint"
								+ "\n\nPLAYER FOUR CONTROLS\n-Home = Move forward\n-End = Move backward\n-Del = Turn left\n-Pg Dn = Turn right\n-\\ = Sprint"));
		messageContainer
				.add(new TextLabel(
						"How to Play",
						"You are a predator.\nThe goal of the game is to eat as many prey as you can. To eat a prey, simply walk into it.\nThe green shapes are plants. You CANNOT EAT PLANTS. Prey eat plants.\nWhen a prey or predator's energy level reaches 100% they will create a baby. The baby will have similar traits to the parent.\n In \"Auto-Play\" mode, which can be selected in the top left corner, you can select predators using drag and click then right click to make them move."));
		messageContainer
				.add(new TextLabel(
						"Things to consider",
						"Why would a certain trait be more advantageous than another?\nWhy does evolution not happen immediately?\nWhat components of speciation are missing?"));
		messageContainerWButton.add(messageContainer, BorderLayout.CENTER);
		JButton play = new JButton("Start/Continue Game!");
		play.addActionListener(this);
		play.setActionCommand("play");
		JButton tutorial = new JButton("Play Tutorial");
		tutorial.addActionListener(this);
		tutorial.setActionCommand("tutorial");
		JPanel tempPlayPanel = new JPanel();
		tempPlayPanel.add(play);
		tempPlayPanel.add(tutorial);
		messageContainerWButton.add(tempPlayPanel, BorderLayout.SOUTH);

		cardLayout = new CardLayout();
		cardContainer = new JPanel(cardLayout);
		cardContainer.add(messageContainerWButton, "start");
		cardContainer.add(this, "game");

		miniMap = new MiniMap(this);
		new StartThread(this, plantPercent, plantSize).start();
	}

	class TextLabel extends JPanel {
		public TextLabel(String label, String areaMessage) {
			setLayout(new BorderLayout(0, 5));
			add(new JLabel(label), BorderLayout.NORTH);
			JTextArea area = new JTextArea(areaMessage);
			area.setEditable(false);
			JScrollPane jsp = new JScrollPane(area);
			add(jsp, BorderLayout.CENTER);
		}
	}

	public void resetMessage() {
		frame.setText("Use the left and right arrow keys to turn. Use the up arrow key to move forward, down to move back and ctrl to sprint.");
	}

	public double sunHeightAngle = Math.random() * Math.PI;
	public double sunAngle = Math.random() * 2 * Math.PI;
	public static double earthSpinSpeed = Math.PI / (720 * 10);

	public void run() {
		while (focused) {
			update();
			timeSinceLastRecord += 25 / 1000.0;
			if (timeSinceLastRecord > recordPer) {
				stats.updateTimeText(maxIndex + timesShifted);
				recordData();
				timeSinceLastRecord -= recordPer;
			}
			sunHeightAngle = (sunHeightAngle + earthSpinSpeed) % (Math.PI * 2);
			for (int i = 0; i < stars.length; i++)
				stars[i].update(this);
			Thread.yield();
			try {
				if (!fastForward)
					Thread.sleep(runSpeed);
				else
					Thread.sleep(runSpeed);
			} catch (InterruptedException e) {
			}
		}
		repaint();
		repaintAll();
	}

	public boolean quality = false;

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("play")) {
			cardLayout.next(cardContainer);
			reAddAllViewports();
		} else if (e.getActionCommand().equals("tutorial"))
			new Tutorial();
		else if (e.getSource() == ((CardContainer) frame).quality)
			quality = ((CardContainer) frame).quality.isSelected();
		else
			miniMap.map.setToPerfectFit();
		requestFocus();
	}

	public void updatePlayers(boolean invincible) {
		for (int i = 0; i < ports.length; i++)
			if (ports[i].updatePlayer(this, invincible))
				return;
	}

	public void updatePrey() {
		for (int i = 0; prey != null && i < prey.size(); i++) {
			Prey p = (Prey) prey.get(i);
			p.updateAi(this, predators, plants);
			for (int b = 0; b < ports.length; b++)
				if (ports[b].player.intersects(p))
					ports[b].player.eat(this, p);
			if (p.alive)
				p.tryCreateChild(this);
			else
				i--;
		}
	}

	public void updatePlants() {
		for (int i = 0; i < plants.length; i++)
			plants[i].update(this);
	}

	public void updatePredators() {
		for (int i = 0; predators != null && i < predators.size(); i++) {
			Predator p = (Predator) predators.get(i);
			p.updateAi(this, prey);
			if (p.alive) {
				p.tryCreateChild(this);
			} else
				i--;
		}
	}

	public void update() {
		updatePlayers(false);
		updatePredators();
		updatePrey();
		updatePlants();
		miniMap.redraw();
		repaintAll();
	}

	public void repaintAll() {
		for (int i = 0; i < ports.length; i++)
			ports[i].repaint();
	}

	boolean clear = true;

	public double randomPosition() {
		return Math.random() * mapWidth;
	}

	public void focusGained(FocusEvent e) {
		if (!focused) {
			focused = true;
			new Thread(this).start();
		} else if (cardLayout != null)
			cardLayout.last(cardContainer);
	}

	public void focusLost(FocusEvent e) {
		focused = false;
		cardLayout.first(cardContainer);
	}

	public void keyPressed(KeyEvent e) {
		for (int i = 0; i < ports.length; i++)
			ports[i].updateKeyPressed(e);
		if (e.getKeyCode() == KeyEvent.VK_F2)
			addNewViewport(false);
		else if (e.getKeyCode() == KeyEvent.VK_F3)
			addNewViewport(true);
		else if (e.getKeyCode() == KeyEvent.VK_F1)
			removeViewport();
	}

	public void removeViewport() {
		if (ports.length > 1)
			ports[ports.length - 1].remove();
		else
			JOptionPane.showMessageDialog(frame,
					"Cannot remove the last player!");
	}

	public void addNewViewport(boolean firstPerson) {
		if (ports.length < GamePanel.controls.length) { // only 4 sets of
			// 'arrows'
			if ((predators != null && predators.size() != 0) || playType == ALONE) {
				Viewport[] temp = new Viewport[ports.length + 1];
				for (int i = 0; i < ports.length; i++)
					temp[i] = ports[i];
				Predator newPlayer;
				if (predators.size() == 0)
					newPlayer = standardPredator(randomPosition(), randomPosition());
				else
					newPlayer = (Predator)predators.get(0);
				temp[temp.length - 1] = new Viewport(this,
						controls[temp.length - 1], newPlayer,
						firstPerson);
				ports = temp;
				reAddAllViewports();
			} else
				JOptionPane.showMessageDialog(frame,
						"Wait for another predator to become available");
		} else
			JOptionPane.showMessageDialog(frame,
					"Cannot have more than 4 players at a time");
	}

	String code = "mrs ma1";

	public void reAddAllViewports() {
		removeAll();
		setLayout(new GridLayout(0, 1));
		if (ports.length == 3) {
			JPanel top = new JPanel(new BorderLayout());
			top.add(ports[0].container);
			JPanel bottom = new JPanel(new GridLayout(1, 0));
			bottom.add(ports[1].container);
			bottom.add(ports[2].container);
			add(top);
			add(bottom);
		} else {
			if (ports.length > 3)
				setLayout(new GridLayout(2, 0));
			for (int i = 0; i < ports.length; i++)
				add(ports[i].container);
		}
		reAddAllPlayers();
		frame.setVisible(true);
	}

	public void reAddAllPlayers() {
		for (int i = 0; i < ports.length; i++)
			ports[i].reAddPlayerPanel();
	}

	public void keyReleased(KeyEvent e) {
		for (int i = 0; i < ports.length; i++)
			ports[i].updateKeyReleased(e);
	}

	public void keyTyped(KeyEvent e) {
		code = code.substring(1, code.length()) + e.getKeyChar();
		if (codeContains("krishna"))
			switchPlayType(TRANQ);
		else if (codeContains("mrs man") || codeContains("ms man")
				|| codeContains("mr girl")) {
			switchPlayType(ALONE);
		} else if (codeContains("ben"))
			for (int i = 0; i < ports.length; i++)
				ports[i].player.acidBuildup = 0;
	}

	public void switchPlayType(int play) {
		playType = play;
		switch (playType) {
		case ALONE:
			if (prey != null)
				for (int i = 0; i < prey.size(); i++)
					prey.get(i).alive = false;
			if (predators != null)
				for (int i = 0; i < predators.size(); i++)
					predators.get(i).alive = false;
			break;
		}
	}

	public boolean codeContains(String check) {
		return code.toLowerCase().indexOf(check.toLowerCase()) != -1;
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == miniMap.map.zoom)
			miniMap.map.updateZoom();
		else
			runSpeed = ((CardContainer) frame).fastForward.getValue();
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		miniMap.map.updateScrollBars();
	}

	class Star {
		public double angle;
		double startHeightAngle;
		public double heightAngle;
		public int points;
		public double width;
		public double startAngle;
		public float brightness;

		public Star() {
			angle = Math.random() * Math.PI;
			heightAngle = Math.random() * Math.PI * 2;
			startHeightAngle = heightAngle;
			points = (int) (Math.random() * 9 + 4);
			width = Math.random() * 0.005;
			startAngle = Math.random() * 2 * Math.PI;
			brightness = (float) Math.random();
		}

		public void update(GamePanel gp) {
			heightAngle = (startHeightAngle + gp.sunHeightAngle)
					% (Math.PI * 2);
		}
	}
}