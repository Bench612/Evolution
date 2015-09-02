import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.*;

public class StatsPanel extends JPanel implements ActionListener {
	public CenterPanel center;
	GamePanel gp;
	CardContainer cp;

	public StatsPanel(CardContainer c) {
		gp = c.gameHUD.gp;
		cp = c;
		setLayout(new BorderLayout(0, 15));
		JButton back = new JButton("Back to Game");
		back.setActionCommand("back");
		back.addActionListener(this);
		add(back, BorderLayout.NORTH);
		center = new CenterPanel();
		add(center, BorderLayout.CENTER);
		JButton timeGraph = new JButton("View Line Graphs");
		timeGraph.addActionListener(this);
		add(timeGraph, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("back")) {
			center.removeAll();
			gp.resetMessage();
			cp.layout.first(cp.center);
		} else {
			cp.line.load(gp);
			cp.layout.next(cp.center);
		}
	}

	public void load() {
		setMessage(cp);
		center.load(cp);
	}

	public static void setMessage(CardContainer c) {
		c.setText("These are the current animals alive. Press 'view bell curve' or 'view line graphs' for more information");
	}
}

class CenterPanel extends JPanel {
	public CenterPanel() {
	}

	public void load(CardContainer cc) {
		removeAll();
		setLayout(new GridLayout(0, 2, 10, 10));
		add(new IndivStats(cc, Organism.add((ArrayList<Organism>)cc.gameHUD.gp.players(),cc.gameHUD.gp.predators
				), cc.gameHUD.gp.predatorImg, 0,
				"Predators"));
		add(new IndivStats(cc, cc.gameHUD.gp.prey, cc.gameHUD.gp.preyImg, 1,
				"Prey"));
	}
}

class IndivStats extends JPanel {
	public IndivStats(CardContainer cc, ArrayList<Organism> info, Image img, int index,
			String labelMessage) {
		setLayout(new BorderLayout());
		JScrollBar vertScroll = new JScrollBar();
		add(new StatsCenter(info, img, vertScroll), BorderLayout.CENTER);
		add(vertScroll, BorderLayout.EAST);
		add(new StatsSouth(cc, index), BorderLayout.SOUTH);
		add(new JLabel(labelMessage, JLabel.CENTER), BorderLayout.NORTH);
	}
}

class StatsSouth extends JPanel implements ActionListener {
	int index;
	CardContainer cc;

	public StatsSouth(CardContainer c, int i) {
		cc = c;
		String avg = "Average Size : "
				+ String.format("%.2f",c.gameHUD.gp.averageSizes[i][c.gameHUD.gp.maxIndex])
				+ "\nAverage Speed : "
				+ String.format("%.2f",c.gameHUD.gp.averageSpeeds[i][c.gameHUD.gp.maxIndex])
				+ "\nAverage Sprint Speed : "
				+ String.format("%.2f",c.gameHUD.gp.averageSprints[i][c.gameHUD.gp.maxIndex])
				+ "\nAverage Eye Angle : "
				+ String.format("%.2f",c.gameHUD.gp.averageEyes[i][c.gameHUD.gp.maxIndex]);
		setLayout(new BorderLayout(0, 10));
		JTextArea text = new JTextArea(avg);
		text.setEditable(false);
		add(text, BorderLayout.CENTER);
		JButton curve = new JButton("View Bell Curves");
		curve.addActionListener(this);
		add(curve, BorderLayout.SOUTH);
		index = i;
	}

	public void actionPerformed(ActionEvent e) {
		cc.bar.load(cc.gameHUD.gp, index);
		cc.layout.last(cc.center);
	}
}

class StatsCenter extends JPanel implements AdjustmentListener {
	JPanel[] panels;
	int totalHeight;
	int yOff = 0;
	int ySpacing = 20;
	JScrollBar vertScroll;

	StatsCenter(ArrayList<Organism> info, Image img, JScrollBar j) {
		vertScroll = j;
		vertScroll.addAdjustmentListener(this);
		setLayout(null);
		totalHeight = 0;
		panels = new JPanel[info.size()];
		for (int i = 0; i < info.size(); i++) {
			panels[i] = info.get(i).getJPanel(img);
			add(panels[i]);
			totalHeight += panels[i].getPreferredSize().height;
		}
		totalHeight += ySpacing * (info.size() + 1);
		setPositions();
		vertScroll.setMinimum(0);
		vertScroll.setMaximum(totalHeight
				- panels[panels.length - 1].getPreferredSize().height);
		repaint();
	}

	private void setPositions() {
		int x = getWidth() / 2;
		int y = yOff;
		for (int i = 0; i < panels.length; i++) {
			Dimension size = panels[i].getPreferredSize();
			panels[i].setBounds(x - size.width / 2, y + ySpacing, size.width,
					size.height);
			y += size.height + ySpacing;
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		setPositions();
		for (int i = 0; i < panels.length; i++) {
			panels[i].repaint();
		}
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		yOff = -vertScroll.getValue();
		repaint();
	}
}