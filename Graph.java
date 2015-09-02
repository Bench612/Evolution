import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.*;

public class Graph extends JPanel implements ActionListener {
	public JComboBox trait;
	public JButton back;

	double xScale;
	double yScale;
	double graphHeight;
	double graphWidth;
	int x0;// a position
	int y0;// position
	double xMin;
	double yMin;
	double xMax;
	double yMax;

	public double xTickInc;
	public double yTickInc;

	int xTicks;
	int yTicks;

	public JLabel xLabel;
	public JLabel yLabel;
	public GamePanel gp;

	public void setBasics(GamePanel g, boolean population) {
		gp = g;
		if (population)
			trait = new JComboBox(new String[] { "Population" , "Size", "Speed",
					"Sprint Speed", "Eye Angle"});
		else
			trait = new JComboBox(new String[] { "Size", "Speed",
					"Sprint Speed", "Eye Angle" });
		back = new JButton("Return to Advanced Stats"); // return button
		xLabel = new JLabel("", JLabel.CENTER);
		yLabel = new JLabel("", JLabel.CENTER);
		setLayout(new BorderLayout()); // add labels
		add(yLabel, BorderLayout.WEST);
		add(xLabel, BorderLayout.SOUTH);
		back.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		StatsPanel.setMessage((CardContainer) gp.frame);
		((CardContainer) gp.frame).layout.show(
				((CardContainer) gp.frame).center, "stats");
	}

	public double screenX(double graphX) {
		return (graphX - xMin) * xScale + x0;
	}

	public double screenY(double graphY) {
		return y0 - ((graphY - yMin) * yScale);
	}

	public void resetScales(Graphics g, double minX, double maxX, double minY,
			double maxY, int tickX, int tickY) {
		xTicks = tickX;
		yTicks = tickY; // set number of ticks
		y0 = getHeight() - 100; // set 0s
		x0 = 50 + yLabel.getWidth();
		xMin = minX; // set min and max
		yMin = minY;
		graphWidth = getWidth() - x0; // set width and height
		graphHeight = y0;
		xTickInc = graphWidth / tickX;
		yTickInc = graphHeight / tickY;
		xScale = graphWidth / (maxX - minX); // set scales
		yScale = graphHeight / (maxY - minY);
	}

	public void drawPoint(Graphics g, double x, double y) {
		g.fillOval((int) (x - 2), (int) (y - 2), 4, 4);
	}

	public void drawVerticalString(Graphics2D g, String s, double centerX,
			double topY) {
		FontMetrics fm = g.getFontMetrics(g.getFont());
		int width = fm.stringWidth(s);
		int height = fm.getHeight();

		// rotate string
		AffineTransform original = g.getTransform();
		AffineTransform rotated = ((AffineTransform) original.clone());
		rotated.rotate(Math.toRadians(90), centerX, topY);
		g.setTransform(rotated);
		g.setColor(Color.BLACK);
		g.drawString(s, (int) centerX - height, (int) topY + width / 2);
		g.setTransform(original);
	}

	public double getMin(double[] x, int maxIndex) {
		double min = x[0];
		for (int i = 1; i < maxIndex; i++)
			min = Math.min(x[i], min);
		return min;
	}

	public double getMax(double[] x, int maxIndex) {
		double max = x[0];
		for (int i = 1; i < maxIndex; i++)
			max = Math.max(x[i], max);
		return max;
	}

	public void drawHorizontalString(Graphics g, String s, double centerX, // draw
																			// it
																			// centered
			double y) {
		drawHorizontalString(g, s, centerX, y, g.getFontMetrics(g.getFont()));
	}

	public void drawHorizontalString(Graphics g, String s, double centerX,
			double y, FontMetrics fm) { // draw it centered
		g.drawString(s, (int) (centerX - fm.stringWidth(s) / 2.0), (int) y);

	}

	public double graphX(int x) {
		return (x - x0) / xScale + xMin;
	}

	public double graphY(int y) {
		return (y0 - y) / yScale + yMin;
	}

	public void drawBorders(Graphics g, double minX, double maxX, double minY,
			double maxY, int tick) {
		resetScales(g, minX, maxX, minY, maxY, tick, 10);
		g.setColor(Color.BLACK);

		g.drawLine(x0, 0, x0, y0); // draw line
		g.drawLine(x0, y0, (int) (x0 + graphWidth), y0); // draw line
		int x = x0;
		int y = y0;
		int width = getWidth();
		while (x <= width) { // draws x axis stuff
			x += xTickInc;
			drawPoint(g, x, y0);
			drawVerticalString((Graphics2D) g, Math.round(graphX(x)) + "", x,
					y0 + 30);
		}
		while (y >= 0) { // draw y axis stuff
			y -= yTickInc;
			drawPoint(g, x0, y);
			drawHorizontalString(g, (Math.round(graphY(y) * 10) / 10.0) + "",
					x0 - 30, y);
		}
	}
}