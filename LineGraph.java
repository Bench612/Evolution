import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class LineGraph extends JPanel implements ActionListener {
	public Line graph;

	public LineGraph() {
	}

	public void load(GamePanel gp) {
		removeAll();
		graph = new Line(gp);
		graph.setToolTipText("Green is prey. Red is predators.");
		setLayout(new BorderLayout()); // set layout
		add(graph.back, BorderLayout.NORTH);
		JPanel east = new JPanel();
		east.add(graph.trait);
		JPanel legend = new JPanel(new GridLayout(3, 0)); // amke a key
		legend.add(new JLabel("KEY")); // add labels (for key)
		legend.add(new JLabel("Predators", getRect(Color.RED), JLabel.LEFT));
		legend.add(new JLabel("Prey", getRect(Color.GREEN), JLabel.LEFT));
		east.add(legend);
		add(east, BorderLayout.EAST);
		graph.trait.addActionListener(this); // add listeners
		add(graph, BorderLayout.CENTER);
	}

	public ImageIcon getRect(Color c) {
		// make an image icon that is a rectangle with a certain color
		Image rect = Evolution.createImage(10.0, 10.0);
		Graphics g = rect.getGraphics();
		g.setColor(c);
		g.fillRect(0, 0, 10, 10);
		return new ImageIcon(rect);
	}

	public void actionPerformed(ActionEvent e) {
		graph.updateData();
	}
}

class Line extends Graph {
	double[][] data;

	public Line(GamePanel g) {
		setBasics(g, true);
		gp = g;
		xLabel.setText("Time");
		updateData();
	}

	public void updateData() {
		// set data based on JCombo
		yLabel.setText("Avg " + trait.getSelectedItem());
		switch (trait.getSelectedIndex()) {
		case 1:
			data = gp.averageSizes;
			break;
		case 2:
			data = gp.averageSpeeds;
			break;
		case 3:
			data = gp.averageSprints;
			break;
		case 4:
			data = gp.averageEyes;
			break;
		default:
			data = gp.populations;
			yLabel.setText(trait.getSelectedItem() + "");
			break;
		}
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g); // draws line graph
		double minY = Math.min(getMin(data[0], gp.maxIndex + 1), getMin(data[1],
						gp.maxIndex + 1));
		double maxY = Math.max(getMax(data[0], gp.maxIndex + 1), getMax(data[1],
						gp.maxIndex + 1));
		drawBorders(g, gp.timesShifted, gp.timesShifted + data[0].length, minY * 0.9, maxY * 1.1,
				data[0].length);
		g.setColor(Color.RED);
		drawPoints(g, 0);
		g.setColor(Color.GREEN);
		drawPoints(g, 1);
	}

	public void drawPoints(Graphics g, int index) {
		// plot points with ovals and draw lines connecting them
		double previousX = 0;
		double previousY = 0;
		for (int i = 0; i <= gp.maxIndex; i++) {
			double x = screenX(i + gp.timesShifted);
			double y = screenY(data[index][i]);
			drawPoint(g, x, y);
			if (i != 0)
				g.drawLine((int) x, (int) y, (int) previousX, (int) previousY);
			previousX = x;
			previousY = y;
		}
	}
}