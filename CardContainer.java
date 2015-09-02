import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

import javax.swing.*;

public class CardContainer extends EvolutionFrame implements ActionListener {
	public static CardContainer newest;
	public GameHUD gameHUD;
	public CardLayout layout;
	public StatsPanel stats;
	public LineGraph line;
	public BarGraph bar;
	public JPanel center;
	public JSlider fastForward;
	public JCheckBox quality;
	JRadioButtonMenuItem normal;
	JRadioButtonMenuItem drought;
	public static boolean loading = false;

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("auto"))
			gameHUD.gp.fastForward = ((JCheckBoxMenuItem) e.getSource())
					.isSelected(); // set fastforward
		else if (e.getActionCommand().equals("top"))
			gameHUD.gp.setAllViewModes(Viewport.TOP_DOWN); // switch to topdown
		else if (e.getActionCommand().equals("first"))
			gameHUD.gp.setAllViewModes(Viewport.FIRST_PERSON); // switch to
		// firstPerson
		else if (e.getActionCommand().equals("top down player"))
			gameHUD.gp.addNewViewport(false);
		else if (e.getActionCommand().equals("first person player"))
			gameHUD.gp.addNewViewport(true);
		else if (e.getActionCommand().equals("remove player"))
			gameHUD.gp.removeViewport();
		else if (e.getActionCommand().equals("norm"))
			gameHUD.gp.switchPlayType(GamePanel.NORMAL);
		else if (e.getActionCommand().equals("tranq"))
			gameHUD.gp.switchPlayType(GamePanel.TRANQ);
		else if (e.getActionCommand().equals("alone"))
			gameHUD.gp.switchPlayType(GamePanel.ALONE);
		else {
			if (drought.isSelected())
				gameHUD.gp.autoEnergyGain = GamePanel.normalAutoEnergyGain / 2; // half
			// sun's
			// energy
			else
				gameHUD.gp.autoEnergyGain = GamePanel.normalAutoEnergyGain; // restor
			// to
			// normal
			// sun
			// energy
		}
	}

	public CardContainer(float variation, Color background, double mapWidth,
			double mapHeight, double plantSize, double plantPercent) {
		super("The Hunt");
		// set basic JFrame stuff
		newest = this;
		loading = true;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(1100, 750);

		// create menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenuBar left = new JMenuBar();
		left.setLayout(new FlowLayout(0, 0, FlowLayout.LEFT));
		menuBar.setLayout(new BorderLayout());
		JMenu n = new JMenu("Player");
		JMenuItem nTP = new JMenuItem("New Top Down Player");
		JMenuItem nFP = new JMenuItem("New First Person Player");
		JMenuItem rP = new JMenuItem("Remove Player");
		nTP.addActionListener(this);
		nFP.addActionListener(this);
		rP.addActionListener(this);
		n.add(nTP);
		n.add(nFP);
		n.add(rP);
		nTP.setActionCommand("top down player");
		nFP.setActionCommand("first person player");
		rP.setActionCommand("remove player");
		left.add(n);
		JMenu view = new JMenu("View"); // View Menu
		JMenuItem top = new JMenuItem("Top Down");
		top.addActionListener(this);
		top.setActionCommand("top");
		view.add(top);
		JMenuItem first = new JMenuItem("First Person");
		first.addActionListener(this);
		first.setActionCommand("first");
		view.add(first);
		left.add(view);
		JMenu gameMode = new JMenu("Play Mode");
		JMenuItem normMode = new JMenuItem("Normal");
		JMenuItem tranqMode = new JMenuItem("Stun");
		JMenuItem battleMode = new JMenuItem("Battle");
		normMode.addActionListener(this);
		tranqMode.addActionListener(this);
		battleMode.addActionListener(this);
		gameMode.add(normMode);
		gameMode.add(tranqMode);
		gameMode.add(battleMode);
		normMode.setActionCommand("norm");
		tranqMode.setActionCommand("tranq");
		battleMode.setActionCommand("alone");
		left.add(gameMode);
		

		JCheckBoxMenuItem playingStraight = new JCheckBoxMenuItem(
				"Auto-Play", false); // a check box for fast forward
		playingStraight.addActionListener(this);
		playingStraight.setActionCommand("auto");
		playingStraight.setToolTipText("Allows the computer to take over.");
		left.add(playingStraight);

		ButtonGroup bg = new ButtonGroup(); // creat button group for special
		// statuses
		drought = new JRadioButtonMenuItem("Drought"); // drought
		drought.addActionListener(this);
		normal = new JRadioButtonMenuItem("Normal Conditions", true); // normally
		normal.addActionListener(this);
		bg.add(normal);
		bg.add(drought);
		JMenuBar radios = new JMenuBar();
		radios.setLayout(new FlowLayout(0, 0, FlowLayout.RIGHT));
		radios.add(normal);
		radios.add(drought);
		// add sub menu bars
		menuBar.add(left, BorderLayout.WEST);
		menuBar.add(radios, BorderLayout.EAST);

		setJMenuBar(menuBar);

		// set variables
		center = new JPanel();
		layout = new CardLayout();
		center.setLayout(layout);
		gameHUD = new GameHUD(this, variation, background, mapWidth, mapHeight,
				plantSize, plantPercent);
		stats = new StatsPanel(this);
		line = new LineGraph();
		bar = new BarGraph();
		// add panels to card layout
		center.add(gameHUD, "HUD");
		center.add(stats, "stats");
		center.add(line, "line");
		center.add(bar, "bar");
		add(center, BorderLayout.CENTER);

		JPanel sliders = new JPanel();
		// JSlider for fastForward speed
		fastForward = new JSlider(JSlider.HORIZONTAL, 0, 50, 50 - 25);
		fastForward.addChangeListener(gameHUD.gp);
		fastForward.setToolTipText("Controls the speed of the game. Slide left for a faster game.");
		fastForward.setInverted(true);

		fastForward.setMajorTickSpacing(10);
		fastForward.setPaintTicks(true);
		fastForward.setMinimum(1);
		Hashtable labelTable = new Hashtable();
		labelTable.put( new Integer( 50 ), new JLabel("Slow") );
		labelTable.put( new Integer( 25 ), new JLabel("Normal") );
		labelTable.put( new Integer( 10 ), new JLabel("Fast") );
		labelTable.put( new Integer( 1), new JLabel("Crazy"));
		fastForward.setLabelTable( labelTable );

		fastForward.setPaintLabels(true);
		

		quality = new JCheckBox("High Quality (First Person Only)");
		quality.addActionListener(gameHUD.gp);
		view.add(quality);
		

		directions.add(fastForward, BorderLayout.WEST);
		setVisible(false);
	}
}