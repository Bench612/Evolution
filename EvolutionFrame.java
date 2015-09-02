import java.awt.*;

import javax.swing.*;

public class EvolutionFrame extends JFrame {
	public DirectionsPanel directions;
	public static Image evo = null;

	public EvolutionFrame(String s) {
		super(s);
		setIconImage(evo);
		directions = new DirectionsPanel(""); // add a directions
		setLayout(new BorderLayout());
		JPanel north = new JPanel(new BorderLayout());
		north.add(directions, BorderLayout.NORTH);
		north.add(new EvolutionPanel(), BorderLayout.CENTER);
		add(north, BorderLayout.NORTH);
	}

	public void setText(String s) {
		directions.label.setText(s); // method for displaying a message
	}

	class DirectionsPanel extends JPanel { // a label at top
		JLabel label;
		JButton mainMenu;

		private DirectionsPanel(String startMessage) {
			setLayout(new BorderLayout());

			label = new JLabel(startMessage);
			JPanel center = new JPanel();
			center.add(label);
			add(center, BorderLayout.CENTER);
		}
	}

	class EvolutionPanel extends JPanel {
		public EvolutionPanel() {
			setPreferredSize(new Dimension(0, 130));
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(new Color(0.5f, 0.5f, 0.5f));
			FlashyIntroPanel.drawEvolutionText(this, 400, g, true, 150,
					new Color(0.7f, 0.7f, 0.7f), 15, true);
			g.drawImage(Evolution.predatorImg, 0, 0, getHeight(), getHeight(),
					this);
			g.drawImage(Evolution.preyImg, getWidth() - getHeight(), 0,
					getHeight(), getHeight(), this);
		}
	}

}