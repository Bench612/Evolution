import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Evolution extends EvolutionFrame implements WindowListener,
		ActionListener {
	public static Evolution staticThis;
	JMenuBar menuBar;
	JRadioButtonMenuItem drought;
	public static Image predatorImg;
	public static Image preyImg;

	public static void main(String[] args) {
		Toolkit t = Toolkit.getDefaultToolkit();
		predatorImg = t.getImage("Sylvester.gif");
		preyImg = t.getImage("Tweety.gif");
		evo = t.getImage("evo.gif");
		Autotroph.leaves = t.getImage("leaves.jpg");
		Autotroph.wood = t.getImage("wood.jpg");
		Viewport.dirt = t.getImage("dirt.jpg");
		new FlashyIntro();
	}

	public Evolution() {
		// JFrame stuff
		super("Evolution Game");
		setSize(900, 700);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);

		JMenuBar menuBar = new JMenuBar();
		JMenu edit = new JMenu("Edit");
		JMenuItem controls = new JMenuItem("Player Controls");
		JMenuItem restore = new JMenuItem("Restore Default Player Controls");
		restore.setActionCommand("restore");
		restore.addActionListener(this);
		controls.addActionListener(this);
		edit.add(controls);
		edit.add(restore);
		menuBar.add(edit);
		setJMenuBar(menuBar);

		// sets variables
		staticThis = this;
		add(new StartScreen(), BorderLayout.CENTER);
		setVisible(true);
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) { // ask for confirmation
		int response = JOptionPane
				.showConfirmDialog(
						this,
						"Are you sure you want to quit?\nAll open games will be closed.",
						"Close Game", JOptionPane.YES_NO_OPTION);
		if (JOptionPane.YES_OPTION == response) {
			System.exit(0);
		}
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public static Image createImage(double width, double height) {
		return staticThis.createImage((int) width, (int) height);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("restore"))
			GamePanel.controls = GamePanel.defaultControls;
		else
			new CustomControls();
	}
}

class CustomControls extends JFrame implements ActionListener, FocusListener,
		KeyListener {
	ButtonGroup buttons;
	JRadioButton one;
	JRadioButton two;
	JRadioButton three;
	JRadioButton four;
	JLabel text;
	int index = -1;
	int controlsIndex;

	public CustomControls() {
		super("Edit Controls");
		setSize(400, 100);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		buttons = new ButtonGroup();
		JPanel north = new JPanel();
		setLayout(new BorderLayout());
		add(north, BorderLayout.NORTH);

		one = new JRadioButton("Player one");
		buttons.add(one);
		one.addActionListener(this);
		north.add(one);

		two = new JRadioButton("Player two");
		buttons.add(two);
		two.addActionListener(this);
		north.add(two);

		three = new JRadioButton("Player three");
		buttons.add(three);
		three.addActionListener(this);
		north.add(three);

		four = new JRadioButton("Player four");
		buttons.add(four);
		four.addActionListener(this);
		north.add(four);

		text = new JLabel("", JLabel.CENTER);
		add(text, BorderLayout.CENTER);
		resetText();
		addFocusListener(this);
		addKeyListener(this);
		setVisible(true);
	}

	public void resetText() {
		text.setText("Select a Player");
		uncheckAll();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == one)
			index = 0;
		else if (e.getSource() == two)
			index = 1;
		else if (e.getSource() == three)
			index = 2;
		else if (e.getSource() == four)
			index = 3;
		requestFocus();
	}

	public void focusGained(FocusEvent e) {
		if (index != -1) {
			controlsIndex = 0;
			text.setText("Press the key for moving forward (UP)");
		}
	}

	private void uncheckAll() {
		buttons.clearSelection();
	}

	private void setText(String s) {
		text.setText(s);
	}

	public void focusLost(FocusEvent e) {
		resetText();
	}

	public void keyPressed(KeyEvent e) {
		if (index != -1) {
			if (GamePanel.controls[index].length > controlsIndex) {
				GamePanel.controls[index][controlsIndex] = e.getKeyCode();
				controlsIndex++;
				switch (controlsIndex) {
				case 1:
					setText("Press the key for turning left (LEFT)");
					break;
				case 2:
					setText("Press the key for moving backwards (DOWN)");
					break;
				case 3:
					setText("Press the key for turning right (RIGHT)");
					break;
				case 4:
					setText("Press the key for sprint (CTRL)");
					break;
				case 5:
					setText("Press the key for Extra # 1");
					break;
				case 6:
					setText("Press the key for Extra # 2");
					break;
				default: {
					index = -1;
					resetText();
				}
				}
			}
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
}