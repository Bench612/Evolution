import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MiniMap extends JPanel {
	Map map;
	public JLabel label;

	public MiniMap(GamePanel gp) {
		setLayout(new BorderLayout());
		JPanel labelButton = new JPanel(new BorderLayout());
		label = new JLabel();
		labelButton.add(label, BorderLayout.NORTH);
		JButton b = new JButton("Fit to screen");
		b.addActionListener(gp);
		labelButton.add(b, BorderLayout.SOUTH);
		add(labelButton, BorderLayout.NORTH);
		JScrollBar east = new JScrollBar();
		JScrollBar south = new JScrollBar(JScrollBar.HORIZONTAL);
		JSlider west = new JSlider(JSlider.VERTICAL, 1, 1000, 1000);
		add(east, BorderLayout.EAST);
		add(south, BorderLayout.SOUTH);
		add(west, BorderLayout.WEST);
		map = new Map(south, east, west, gp);
		map
				.setToolTipText("Red dot = you. Green dots = allies. Orange dots = prey");
		add(map, BorderLayout.CENTER);
	}

	public void redraw() {
		map.repaint();
	}
}

class BasicStats extends JPanel implements ActionListener {
	JLabel pred;
	JLabel prey;
	JLabel time;
	GamePanel gp;

	public BasicStats(GamePanel g) {
		pred = new JLabel("", JLabel.CENTER);
		prey = new JLabel("", JLabel.CENTER);
		time = new JLabel("", JLabel.CENTER);
		updateTimeText(0);
		gp = g;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(time);
		add(pred);
		add(prey);
		JButton stats = new JButton("View Advanced Stats");
		stats.addActionListener(this);
		add(stats);
	}
	
	public void updateTimeText(int ctime){
		time.setText("Time Elapsed : " + ctime);
	}

	public void actionPerformed(ActionEvent e) {
		boolean isMax = gp.maxIndex == gp.timeToRecord;
		gp.recordData();
		((CardContainer)gp.frame).stats.load();
		((CardContainer)gp.frame).layout.next(((CardContainer)gp.frame).center);
		if (!isMax)
			gp.maxIndex--;
	}
}

class Map extends JPanel implements MouseListener, MouseMotionListener,
		MouseWheelListener {
	public Map(JScrollBar x, JScrollBar y, JSlider z, GamePanel g) {
		xScroll = x;
		yScroll = y;
		zoom = z;
		x.setEnabled(false);
		y.setEnabled(false);
		z.setPaintTicks(true);
		z.setMajorTickSpacing(100);
		z.setMinorTickSpacing(10);
		addMouseWheelListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		setBackground(Color.BLACK);
		repaint();
		gp = g;
		x.addAdjustmentListener(gp);
		y.addAdjustmentListener(gp);
		z.addChangeListener(gp);
		z.setToolTipText("Sets the zoom on the mini map");
	}

	public JSlider zoom;
	public JScrollBar yScroll;
	public JScrollBar xScroll;
	double xOff;
	double yOff;
	public double xScale;
	public double yScale;
	boolean dragging = false;
	int x1; // of rect
	int x2;
	int y1;
	int y2;
	double flagSize = 0;
	int maxFlagSize = 10;
	Color xColor;
	boolean perfectFit = true;
	static double zoomIncrement = 0.05;
	boolean updateScrolls = true;// if its changing because of code
	GamePanel gp;

	public void setToPerfectFit() {
		updateScrolls = false;
		basedOffPerfectFit = true;
		perfectFit = true;
		xOff = 0;
		yOff = 0;
		yScroll.setEnabled(false);
		xScroll.setEnabled(false);
		nullCenter();
		basedOffPerfectFit = false;
		repaint();
		updateScrolls = true;
	}

	public void setScaleText(JLabel label) {
		// sets label
		label.setText("Mini Map  X scale = " + Math.round(xScale * 100)
				+ "% & Y scale = " + Math.round(yScale * 100) + "%");
	}

	boolean basedOffPerfectFit = false; // when setting zooom

	private void setZoom(double xS, double yS) {
		xScale = Math.max(Math.min(xS, 1), 0);
		yScale = Math.max(Math.min(yS, 1), 0);
		zoom.setValue((int) (xScale * zoom.getMaximum()));
	}

	public void paintComponent(Graphics g) {
		if (perfectFit) {
			// scales
			basedOffPerfectFit = true;
			setZoom(getWidth() / gp.mapWidth, getHeight() / gp.mapHeight);
			setScaleText(gp.miniMap.label);
			basedOffPerfectFit = false;
		} else
			getMinMaxOff();

		super.paintComponent(g);

		g.setColor(gp.terrain);
		g.fillRect((int) xOff, (int) yOff, (int) (gp.mapWidth * xScale) + 1,
				(int) (gp.mapHeight * yScale) + 1);
		// draws selectedArea
		if (dragging) {
			g.setColor(Color.WHITE);
			int topX = Math.min(x1, x2);
			int topY = Math.min(y1, y2);
			g.fillRect(topX, topY, Math.abs(x2 - x1), Math.abs(y2 - y1));
		}

		// draws allies and player
		Color darker = GamePanel.viewColor.darker();
		if (gp.predators != null)
			for (int i = 0; i < gp.predators.size(); i++)
				drawOrganismBack(g, gp.predators.get(i), darker);
		for (int i = 0; i < gp.ports.length; i++)
			drawOrganismBack(g, gp.ports[i].player, GamePanel.viewColor);
		for (int i = 0; i < gp.plants.length; i++) {
			if (gp.plants[i].visible || gp.fastForward) {
				g.setColor(gp.plants[i].color);
				g.fillRect((int) (gp.plants[i].x * xScale + xOff),
						(int) (gp.plants[i].y * yScale + yOff),
						(int) (gp.plants[i].width * xScale),
						(int) (gp.plants[i].height * yScale));
			}
		}

		if (gp.predators != null)
			for (int i = 0; i < gp.predators.size(); i++)
				drawOrganismPoint(g, gp.predators.get(i), Color.GREEN);
		for (int i = 0; i < gp.ports.length; i++)
			drawOrganismPoint(g, gp.ports[i].player, Color.RED);

		// draws visible prey
		if (gp.prey != null) {
			for (int i = 0; i < gp.prey.size(); i++)
				if (gp.prey.get(i).seen || gp.fastForward) {
					Color preyC = Color.ORANGE;
					if (gp.prey.get(i).selected)
						preyC = Color.GRAY;
					drawOrganismPoint(g, gp.prey.get(i), preyC);
				}
		}

		// draws screen
		g.setColor(Color.BLACK);
		for (int i = 0; i < gp.ports.length; i++)
			g.drawRect((int) (-gp.ports[i].xOffset * xScale + xOff),
					(int) (-(gp.ports[i].yOffset) * yScale + yOff),
					(int) (gp.ports[i].width * xScale),
					(int) (gp.ports[i].height * yScale));

		if (gp.flagged) {
			if (flagSize < maxFlagSize)
				flagSize += 0.5;
			plotX(g, gp.mainFlagX * xScale + xOff,
					gp.mainFlagY * yScale + yOff, flagSize, xColor);
		}
	}

	public void drawOrganismPoint(Graphics g, Organism org, Color c) {
		plotPoint(g, org.x * xScale + xOff, org.y * yScale + yOff, c);
	}

	public void drawOrganismBack(Graphics g, Organism org, Color c) {
		if (!org.selected)
			g.setColor(c);
		else
			g.setColor(Color.BLACK);
		g.fillOval((int) ((org.x - org.hearing) * xScale + xOff),
				(int) ((org.y - org.hearing) * yScale + yOff),
				(int) ((org.hearing * 2) * xScale),
				(int) ((org.hearing * 2) * yScale));
		g.fillArc((int) ((org.x - org.sightDistance) * xScale + xOff),
				(int) ((org.y - org.sightDistance) * yScale + yOff),
				(int) ((org.sightDistance * 2) * xScale),
				(int) ((org.sightDistance * 2) * yScale), (int) Math
						.toDegrees(-org.sightRange / 2 - org.angle), (int) Math
						.toDegrees(org.sightRange));
	}

	public void plotPoint(Graphics g, double x, double y, Color c) {
		g.setColor(c);
		g.fillOval((int) x - 2, (int) y - 2, 4, 4);
	}

	public void plotX(Graphics g, double x, double y, double halfLength, Color c) {
		g.setColor(c);
		// half length isn't really half the length
		g.drawLine((int) (x - halfLength) + 1, (int) (y - halfLength) + 1,
				(int) (x + halfLength), (int) (y + halfLength));
		g.drawLine((int) (x + halfLength) + 1, (int) (y - halfLength) + 1,
				(int) (x - halfLength), (int) (y + halfLength));
	}

	public void mouseClicked(MouseEvent e) {
		double mX = (e.getX() - xOff) / xScale;
		double mY = (e.getY() - yOff) / yScale;
		if (e.isAltDown()) {
			if (!perfectFit) {
				zoomX(mX);
				zoomY(mY);
			} else {
				centerX = mX;
				centerY = mY;
			}
		} else
			setFlagPosition(mX, mY, e.isMetaDown());
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		x1 = e.getX();
		y1 = e.getY();
		dragging = false;
	}

	public void mouseMoved(MouseEvent e) {

	}

	public void mouseDragged(MouseEvent e) {
		dragging = gp.fastForward;
		x2 = e.getX();
		y2 = e.getY();
		repaint();
	}

	public void setFlagPosition(double x, double y, boolean rightClick) {
		if (gp.focused) {
			gp.flagged = true;
			flagSize = 0;
			gp.mainFlagX = x;
			gp.mainFlagY = y;
			gp.goThrough = rightClick;
			if (rightClick)
				xColor = Color.RED;
			else
				xColor = Color.YELLOW;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (dragging) {
			y2 = e.getY();
			x2 = e.getX();
			double topX = Math.min(x1, x2) - xOff;
			double topY = Math.min(y1, y2) - yOff;
			double bX = Math.max(x2, x1) - xOff;
			double bY = Math.max(y2, y1) - yOff;
			if (e.isShiftDown()) {
				if (gp.prey != null)
					for (int i = 0; i < gp.prey.size(); i++)
						if (gp.prey.get(i).x * xScale > topX
								&& gp.prey.get(i).x * xScale < bX
								&& gp.prey.get(i).y * yScale > topY
								&& gp.prey.get(i).y * yScale < bY)
							gp.prey.get(i).selected = !e.isMetaDown();
			} else if (gp.predators != null) {
				for (int i = 0; i < gp.predators.size(); i++)
					if (gp.predators.get(i).x * xScale > topX
							&& gp.predators.get(i).x * xScale < bX
							&& gp.predators.get(i).y * yScale > topY
							&& gp.predators.get(i).y * yScale < bY)
						gp.predators.get(i).selected = !e.isMetaDown();
			}
		}
		dragging = false;
		repaint();
	}

	double xOffMin;
	double yOffMin;
	double xOffMax;
	double yOffMax;

	public void getMinMaxOff() {

		xOffMax = -(gp.mapWidth * xScale - getWidth());
		yOffMax = -(gp.mapWidth * yScale - getHeight()); // actually

		updateScrolls = false;
		if (xOffMax < 0) {
			xScroll.setEnabled(true);
			xScroll.setMinimum((int) -xOffMin);
			xScroll.setMaximum((int) -xOffMax);
			xOff = Math.min(Math.max(xOff, xOffMax), 0);
		} else {
			xScroll.setEnabled(false);
			xOffMin = xOffMax / 2;
			xOffMax = xOffMin;
			xOff = xOffMin;
		}

		if (yOffMax < 0) {
			yScroll.setEnabled(true);
			yScroll.setMinimum((int) -yOffMin);
			yScroll.setMaximum((int) -yOffMax);
			yOff = Math.min(Math.max(yOff, yOffMax), 0);
		} else {
			yScroll.setEnabled(false);
			yOffMin = yOffMax / 2;
			yOffMax = yOffMin;
			yOff = yOffMin;
		}
		updateScrolls = true;

	}

	public void updateZoom() {
		if (!basedOffPerfectFit) {
			perfectFit = false;
			double startCenterX = getCenterX();
			double startCenterY = getCenterY();
			xScale = zoom.getValue() / (double) zoom.getMaximum();
			yScale = xScale;
			getMinMaxOff();
			if (xOffMax < 0)
				zoomX(startCenterX);
			if (yOffMax < 0)
				zoomY(startCenterY);
			setScaleText(gp.miniMap.label);
			repaint();
		}
	}

	double centerX;
	double centerY;

	public void nullCenter() {
		centerX = -1;
		getCenterX();
		centerY = -1;
		getCenterY();
	}

	public void zoomX(double x) { // game coordinates
		double tXO = (getWidth() / 2.0) - (x * xScale);
		updateScrolls = false;
		xScroll.setValue(-(int) tXO);
		updateScrolls = true;
		xOff = tXO;
		centerX = x;
	}

	public void zoomY(double y) {
		double tYO = (getHeight() / 2.0) - (y * yScale);
		updateScrolls = false;
		yScroll.setValue(-(int) tYO);
		updateScrolls = true;
		yOff = tYO;
		centerY = y;
	}

	public double getCenterX() {
		if (centerX == -1)
			centerX = ((getWidth()) / 2.0 - xOff) / xScale;
		return centerX;
	}

	public double getCenterY() {
		if (centerY == -1)
			centerY = (getHeight() / 2.0 - yOff) / yScale;
		return centerY;
	}

	public void updateScrollBars() {
		if (updateScrolls) {
			if (xScroll.isEnabled())
				xOff = -(xScroll.getValue() - xOffMin);
			if (yScroll.isEnabled())
				yOff = -(yScroll.getValue() - xOffMin);
			nullCenter();
			repaint();
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		double z = xScale + (zoomIncrement * -e.getWheelRotation());
		setZoom(z, z);
	}
}