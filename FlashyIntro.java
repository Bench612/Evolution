import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class FlashyIntro extends JFrame implements ActionListener {
	public FlashyIntro() {
		super("Welcome to Evolution!");
		setIconImage(EvolutionFrame.evo);
		setSize(1000, 500);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		JPanel buttons = new JPanel();
		JButton start = new JButton("Play Evolution");
		start.setActionCommand("start");
		start.addActionListener(this);
		buttons.add(start);
		JPanel learnPanel = new JPanel(new BorderLayout());
		JButton learn = new JButton("Learn");
		learn.addActionListener(this);
		learnPanel.add(learn, BorderLayout.CENTER);
		learnPanel.add(new JLabel("Learn about evolution!"),
				BorderLayout.NORTH);
		buttons.add(start);
		buttons.add(learnPanel);
		add(buttons, BorderLayout.SOUTH);
		add(new FlashyIntroPanel(), BorderLayout.CENTER);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("start")) {
			dispose();
			new Evolution();
		} else
			new TeachingEvolution();
	}
}



class TeachingEvolution extends EvolutionFrame {
	public TeachingEvolution() {
		super("Learn about the Theory of Evolution!");
		setSize(1000, 600);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setText("Read below and exit when done.");
		JTextArea area = new JTextArea(
				"\tThe process of evolution is a very complicated one with many \n things that can change the outcome. Evolution is when a something changes overtime to\n benefit their species. Evolution is mainly caused by natural selection, there are three\n different kinds of natural selection: stabilizing selection, directional selection, and\n disruptive selection. A couple of the things that can result from evolution are\n divergent evolution and convergent evolution, but we wont talk about them for now.\n\tDisruptive selection is when genes that are on the high and low extremes are favored.\n For example, if there was an environment where there were large nuts for big birds and\n small seeds for small birds, the population count of medium sized birds would decrease.\nWhile you play Evolution take a look at the sizes of the birds a little while into\n the game.\n\tStabilizing selection is the complete opposite of disruptive selection. Instead of the\n genes of high and low extremes being favored it’s the average group of the population\n that are favored. You probably can’t see this when you play our game but an example\n would be when a creature with short legs can’t move fast enough and the same species\n creature with extremely long legs cant run fast for long enough.\n\tDirectional selection is when the genes of one of the extremes are more favorable for\n some reason. If you have a bird that has a large wing span they are able to get away from\n predators. Then that would be the favored gene.");
		area.setEditable(false);
		JScrollPane scroll = new JScrollPane(area);
		add(scroll, BorderLayout.CENTER);
		setVisible(true);
	}
}
