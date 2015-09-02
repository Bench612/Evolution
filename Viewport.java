import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

public class Viewport extends JPanel implements MouseListener,
		MouseMotionListener {
	// does not include mini map
	public int[] controls;// up,left,down,right,sprint,shoot,nade
	public double width;
	public double height;
	public static final int FIRST_PERSON = 0;
	public static final int TOP_DOWN = 1;
	public static Color skyColor = new Color(172, 214, 247);
	public int viewMode;

	static int sprintKey = KeyEvent.VK_CONTROL;
	public static Color stunColor = Color.WHITE;
	public double xOffset;
	public double yOffset;
	public int nadeHoldTime = 0;

	Predator player;
	Footprint[] feet;
	static int feetToRecord = 10;
	double distanceWalked;
	int maxFeetIndex;
	int feetIndex;

	PolygonSet mainPolygonSet;
	PolygonSet currentPolygonSet;

	GamePanel gp;
	JPanel container;

	boolean[][] pixels;

	public static Image dirt;
	
	public void startPolygonSet() {
		currentPolygonSet = currentPolygonSet.addNewPolygonSet();
	}

	public void endPolygonSet() {
		currentPolygonSet.close();
		currentPolygonSet = currentPolygonSet.container;
	}

	public Viewport(GamePanel g, int[] control, Predator play,
			boolean firstPerson) {
		mainPolygonSet = new PolygonSet(null);
		controls = control;
		gp = g;
		if (firstPerson)
			viewMode = FIRST_PERSON;
		else
			viewMode = TOP_DOWN;
		resetFeet();
		leftFoot = true;
		xOffset = 0;
		yOffset = 0;
		container = new JPanel(new BorderLayout());
		addMouseListener(this);
		addMouseMotionListener(this);
		player = play;
		playerPanel = player.getJPanel(gp.predatorImg);
		player.gainPlayerControl(this);
		container.add(playerPanel, BorderLayout.NORTH);
		container.add(this, BorderLayout.CENTER);
		setOffset();
	}

	public void remove() {
		player.losePlayerControl(this);
		gp.remove(container);
		gp.frame.setVisible(true);
	}

	public static final int footprintFadeTime = 40;

	class Footprint {
		double x;
		double y;
		double angle;
		int toeWidth;
		int heelWidth;
		double heelHeight;
		int time;

		public Footprint(Predator player, boolean leftFoot) {
			double distanceOff = player.width / 2;
			if (leftFoot)
				distanceOff = -distanceOff;
			angle = player.angle;
			x = player.x + Math.cos(angle + Math.PI / 2) * distanceOff;
			y = player.y + Math.sin(angle + Math.PI / 2) * distanceOff;
			toeWidth = (int) (player.width * 0.21);
			heelWidth = (int) (player.width * 0.8 - toeWidth);
			heelHeight = player.width / 2;
			time = 0;
		}

		public void draw(Graphics2D g, double xOff, double yOff, Color baseColor) {
			time++;
			int fadePart = Math.max(
					(int) (255 * (1 - ((double) time / footprintFadeTime))), 0);
			g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(),
					baseColor.getBlue(), fadePart));// fades until 0
			AffineTransform original = Organism.rotate(g, angle, x + xOff, y
					+ yOff);
			g.fillOval((int) (x + xOff - heelWidth / 2) - toeWidth, (int) (y
					+ yOff - heelHeight / 2), heelWidth, (int) (heelHeight));
			double angleInc = Math.toRadians(40);
			double distance = (heelWidth + toeWidth) / 2;
			for (double angle1 = angleInc; angle1 >= -angleInc; angle1 -= angleInc) {
				g.fillOval((int) (x + xOff - toeWidth / 2 + Math.cos(angle1)
						* distance), (int) (y + yOff - toeWidth / 2 + Math
						.sin(angle1)
						* distance), toeWidth, toeWidth);
			}
			g.setTransform(original);
		}
	}

	public void mouseDragged(MouseEvent e) {
		updateMouse(e);
	}

	boolean targetOnMouse = false;
	boolean mouseMoved = false;
	int mouseX;
	int mouseY;

	boolean sprinting = false;
	boolean pressLeft = false;
	boolean pressRight = false;
	boolean pressUp = false;
	boolean pressDown = false;

	public void mouseReleased(MouseEvent e) {
		holdingGrenade = false;
		if (e.isMetaDown())
			fireBullet(Organism.NADE);
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		targetOnMouse = false;
	}

	public void mouseClicked(MouseEvent e) {
	}

	boolean holdingGrenade = false;

	public void setOffset() {
		width = getWidth();
		height = getHeight();
		xOffset = Math.max(Math.min(width / 2 - player.x, 0),
				-(gp.mapWidth - width));
		yOffset = Math.max(Math.min(height / 2 - player.y, 0),
				-(gp.mapHeight - height));
	}

	public void mousePressed(MouseEvent e) {
		if (gp.focused) {
			if (!e.isAltDown()) {
				if (e.isMetaDown())
					startHoldNade();
				else
					fireBullet(Organism.TRANQ);
			} else if (viewMode == FIRST_PERSON)
				player.angle += ((e.getX() - (getWidth() / 2.0)) / getWidth())
						* player.sightRange;
			gp.flagged = false;
		}
	}

	public void fireBullet(int weaponType) {
		double angle;
		double vertAngle = 0;
		if (weaponType == Organism.NADE)
			holdingGrenade = false;
		if (targetOnMouse && viewMode == FIRST_PERSON) {
			angle = ((mouseX - getWidth() / 2.0) / getWidth())
					* player.sightRange + player.angle;
			vertAngle = ((getHeight() / 2.0 - mouseY) / getHeight()) * Math.PI;
		} else
			angle = player.angle;
		player.fireWeapon(gp, nadeHoldTime, angle, vertAngle, weaponType);
	}

	public void startHoldNade() {
		if (!holdingGrenade) {
			holdingGrenade = gp.playType != GamePanel.NORMAL;
			nadeHoldTime = 0;
		}
	}

	public void updateKeyPressed(KeyEvent e) {
		if (e.getKeyCode() != sprintKey && e.getKeyCode() != KeyEvent.VK_SPACE)
			gp.flagged = false;
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			if (viewMode == TOP_DOWN)
				viewMode = FIRST_PERSON;
			else
				viewMode = TOP_DOWN;
		} else if (e.getKeyCode() == controls[5])
			fireBullet(Organism.TRANQ);
		else if (e.getKeyCode() == controls[6])
			startHoldNade();
		setMovements(true, e);
	}

	private void setMovements(boolean tf, KeyEvent e) {
		int c = e.getKeyCode();
		int[] original = controls;
		int i = 0;
		do {
			if (c == controls[0])
				pressUp = tf;
			else if (c == controls[2])
				pressDown = tf;
			if (c == controls[1]) {
				pressLeft = tf;
				mouseMoved = false;
				targetOnMouse = false;
			} else if (c == controls[3]) {
				pressRight = tf;
				mouseMoved = false;
				targetOnMouse = false;
			} else if (c == controls[4])
				sprinting = tf;
			controls = gp.controls[i];
			i++;
		} while (gp.ports.length == 1 && i < gp.controls.length);
		controls = original;
	}

	public void updateKeyReleased(KeyEvent e) {
		setMovements(false, e);
		if (e.getKeyCode() == controls[6])
			fireBullet(Organism.NADE);
	}

	public void mouseMoved(MouseEvent e) {
		updateMouse(e);
	}

	boolean leftFoot;
	boolean showBackground = true;

	public void drawFootprint(Graphics2D g, Footprint f, Color baseColor) {
		f.draw(g, xOffset, yOffset, baseColor);
	}

	public void drawTopDown(Graphics g) {
		Graphics2D castedG = (Graphics2D) g;
		int width = dirt.getWidth(this);
		int height = dirt.getHeight(this);
		if (width > 0 && height > 0)
			for (int x = 0; x < gp.mapWidth; x += width)
				for (int y = 0; y < gp.mapHeight; y += height)
					g.drawImage(dirt, (int) (x + xOffset), (int) (y + yOffset),
							this);
		// draws feet;
		Color darker = new Color(0, 0, 0, 0.4f);
		for (int i = 0; i < maxFeetIndex; i++)
			drawFootprint(castedG, feet[i], darker);
		if (gp.playType != GamePanel.NORMAL) {
			g.setColor(Color.RED);
			g.drawLine((int) (player.x + xOffset), (int) (player.y + yOffset),
					(int) (player.x + xOffset + Math.cos(player.angle)
							* player.sightDistance),
					(int) (player.y + yOffset + Math.sin(player.angle)
							* player.sightDistance));
		}
		draw(castedG, gp.predators, gp.predatorImg);
		draw(castedG, gp.prey, gp.preyImg);
		// draws plants
		for (int i = 0; i < gp.plants.length; i++)
			gp.plants[i].draw(this, g);
		if (showBackground)
			player.drawBackground(this, g);
		draw(castedG, gp.players(), gp.predatorImg);
	}

	public void draw(Graphics2D g, ArrayList<Organism> o, Image img) {
		if (o != null)
			for (int i = 0; i < o.size(); i++) {
				if (player.canSee(o.get(i))) {
					o.get(i).seen = true;
					o.get(i).drawImage(this, g, img);
					o.get(i).drawStun(this, g);
				}
				o.get(i).drawBullets(this, g);
			}
	}

	public static Color shadow = new Color(0, 0, 0, 0.4f);

	public void addFirstPerson3DPolygon(Polygon3D p) {
		currentPolygonSet.add(p);
	}

	public void addFirstPerson3DPolygon(Graphics g, double[] x, double[] y,
			double[] z, double xOff, double yOff, double zOff) {
		addFirstPerson3DPolygon(new Polygon3DPlain(currentPolygonSet, this, g,
				xOff, yOff, zOff, x, y, z));
	}

	public void addFirstPerson3DPolygon(double[] x, double[] y, double[] z,
			double xOff, double yOff, double zOff, Image texture) {
		addFirstPerson3DPolygon(new Polygon3DTexture(currentPolygonSet, this,
				texture, xOff, yOff, zOff, x, y, z));
	}

	public void fillFirstPerson3DShadow(Graphics g, Polygon3D p) {
		double[] shadowX = new double[p.xs.length];
		double[] shadowY = new double[p.xs.length];
		double[] shadowZ = fillWith(0, p.xs.length);
		for (int i = 0; i < p.xs.length; i++) {
			double shadowLength = (1 / Math.tan(gp.sunHeightAngle)) * p.zs[i];
			shadowX[i] = p.xs[i] + Math.cos(gp.sunAngle + Math.PI)
					* shadowLength;
			shadowY[i] = p.ys[i] + Math.sin(gp.sunAngle + Math.PI)
					* shadowLength;
		}
		if (shadowIndex >= shadows.length) {
			Polygon[] temp = new Polygon[shadows.length + 1];
			for (int i = 0; i < shadows.length; i++)
				temp[i] = shadows[i];
			shadows = temp;
		}
		shadows[shadowIndex] = getFirstPerson3DPolygon(new Polygon3D(
				currentPolygonSet, this, p.xOff, p.yOff, p.zOff, shadowX,
				shadowY, shadowZ));
		shadowIndex++;
	}

	public Polygon getFirstPerson3DPolygon(Polygon3D p) {
		for (int i = 0; i < p.xs.length; i++)
			if (!player
					.canSeeIgnoreDistance(p.xs[i] + p.xOff, p.ys[i] + p.yOff))
				return null;
		int[] convertedX = new int[p.xs.length];
		int[] convertedY = new int[p.ys.length];
		for (int i = 0; i < p.xs.length; i++) {
			convertedX[i] = (int) getAccurateFirstPersonX(p.xs[i] + p.xOff,
					p.ys[i] + p.yOff);
			convertedY[i] = (int) getFirstPersonCielingY(p.xs[i] + p.xOff,
					p.ys[i] + p.yOff, p.zs[i] + p.zOff);
		}
		return new Polygon(convertedX, convertedY, p.xs.length);
	}

	public void addFirstPerson3DPolygon(Graphics g, double[] x, double[] y,
			double[] z) {
		addFirstPerson3DPolygon(g, x, y, z, 0, 0, 0);
	}

	public static void getHorizontalBases(double x, double y, double z,
			double startAngle, double radius, double[] xs, double[] ys,
			double[] zs) {
		double angle = startAngle;
		double angleInc = (Math.PI * 2) / xs.length;
		for (int i = 0; i < xs.length; i++) {
			xs[i] = Math.cos(angle) * radius + x;
			ys[i] = Math.sin(angle) * radius + y;
			zs[i] = z;
			angle += angleInc;
		}
	}

	public void getDistance(Polygon3D p) {
		p.maxDistance = (Organism.length(player.distanceTo(p.xs[0] + p.xOff,
				p.yOff + p.ys[0]), p.zs[0] - player.height));
		p.minDistance = p.maxDistance;
		for (int i = 0; i < p.xs.length; i++) {
			double d = Organism.length(player.distanceTo(p.xs[i] + p.xOff,
					p.yOff + p.ys[i]), p.zs[i] - player.height);
			p.maxDistance = Math.max(d, p.maxDistance);
			p.minDistance = Math.min(d, p.minDistance);
		}
	}

	public static void getVerticalBases(double x, double y, double z,
			double startAngle, double direction, double radius, double[] xs,
			double[] ys, double[] zs) {
		direction += Math.PI / 2;
		double currentAngle = startAngle;
		double angleInc = (Math.PI * 2) / xs.length;
		for (int i = 0; i < xs.length; i++) {
			zs[i] = z + Math.sin(currentAngle) * radius;
			double xy = Math.cos(currentAngle) * radius;
			xs[i] = x + Math.cos(direction) * xy;
			ys[i] = y + Math.sin(direction) * xy;
			currentAngle += angleInc;
		}
	}

	public void fillVerticalCylindricalPolygon(Graphics g, double x, double y,
			double z, double startAngle, double radius, int sides, double height) {
		double[][] xs = new double[2][sides];
		double[][] ys = new double[2][sides];
		double[][] zs = new double[2][sides];
		for (int i = 0; i < 2; i++)
			getHorizontalBases(x, y, z + height * i, startAngle, radius, xs[i],
					ys[i], zs[i]);
		fillCylindricalPolygon(g, 0, 0, 0, xs, ys, zs);
	}

	public void fillHorizontalCylindricalPolygon(Graphics g, double x,
			double y, double z, double startAngle, double direction,
			double radius, int sides, double height) {
		double[][] xs = new double[2][sides];
		double[][] ys = new double[2][sides];
		double[][] zs = new double[2][sides];
		for (int i = 0; i < 2; i++)
			getVerticalBases(x + Math.cos(direction) * height * i, y
					+ Math.sin(direction) * height * i, z, startAngle,
					direction, radius, xs[i], ys[i], zs[i]);
		fillCylindricalPolygon(g, 0, 0, 0, xs, ys, zs);
	}

	public void fillHorizontalPyramid(Graphics g, double x, double y, double z,
			double startAngle, double direction, double radius, int sides,
			double height, double z2) {
		double[] xs = new double[sides];
		double[] ys = new double[sides];
		double[] zs = new double[sides];
		getVerticalBases(x, y, z, startAngle, direction, radius, xs, ys, zs);
		fillPyramid(g, x + Math.cos(direction) * height, y
				+ Math.sin(direction) * height, z2, xs, ys, zs);
	}

	public void fillPyramid(Graphics g, double x, double y, double z,
			double[] xs, double[] ys, double[] zs) {
		startPolygonSet();
		for (int i = 1; i < xs.length; i++) {
			addFirstPerson3DPolygon(g, new double[] { xs[i], xs[i - 1], x },
					new double[] { ys[i], ys[i - 1], y }, new double[] { zs[i],
							zs[i - 1], z }, 0, 0, 0);
		}
		int last = xs.length - 1;
		addFirstPerson3DPolygon(g, new double[] { xs[0], xs[last], x },
				new double[] { ys[0], ys[last], y }, new double[] { zs[0],
						zs[last], z }, 0, 0, 0);
		endPolygonSet();
	}

	public void fillCylindricalPolygon(Graphics g, double x, double y,
			double z, double[][] xs, double[][] ys, double[][] zs, Image texture) {
		startPolygonSet();
		for (int b = 0; b < xs.length - 1; b++) {
			int b2 = b + 1;
			for (int i = 0; i < xs[b].length; i++) {
				int i2 = (i + 1) % xs[b].length;
				addFirstPerson3DPolygon(new double[] { xs[b][i], xs[b][i2],
						xs[b2][i2], xs[b2][i] }, new double[] { ys[b][i],
						ys[b][i2], ys[b2][i2], ys[b2][i] }, new double[] {
						zs[b][i], zs[b][i2], zs[b2][i2], zs[b2][i] }, x, y, z,
						texture);
			}
			addFirstPerson3DPolygon(xs[b2], ys[b2], zs[b2], x, y, z, texture);
		}
		endPolygonSet();
	}

	public void fillCylindricalPolygon(Graphics g, double x, double y,
			double z, double[][] xs, double[][] ys, double[][] zs) {
		startPolygonSet();
		for (int b = 0; b < xs.length - 1; b++) {
			int b2 = b + 1;
			for (int i = 0; i < xs[b].length; i++) {
				int i2 = (i + 1) % xs[b].length;
				addFirstPerson3DPolygon(g, new double[] { xs[b][i], xs[b][i2],
						xs[b2][i2], xs[b2][i] }, new double[] { ys[b][i],
						ys[b][i2], ys[b2][i2], ys[b2][i] }, new double[] {
						zs[b][i], zs[b][i2], zs[b2][i2], zs[b2][i] }, x, y, z);
			}
			addFirstPerson3DPolygon(g, xs[b2], ys[b2], zs[b2], x, y, z);
		}
		endPolygonSet();
	}

	public double[] fillWith(double value, int length) {
		double[] returnValue = new double[length];
		for (int i = 0; i < length; i++)
			returnValue[i] = value;
		return returnValue;
	}

	public void drawFirstPerson(ArrayList<Organism> prey, Graphics g) {
		if (prey != null) {
			for (int i = 0; i < prey.size(); i++) {
				if (player.canSee(prey.get(i))) {
					prey.get(i).drawFirstPerson(this, g);
					prey.get(i).seen = true;
				}
				for (int b = 0; b < prey.get(i).bullets.length; b++)
					if (prey.get(i) != null && prey.get(i).bullets[b].live) {
						prey.get(i).bullets[b].drawFirstPerson(this, g);
					}
			}
		}
	}

	public static double sunHorizon = Math.PI / 4;
	public static double sunBlack = Math.PI / 4;
	public static Color sunsetRed = new Color(255, 90, 60);

	Polygon[] shadows;
	int shadowIndex;

	public void drawFirstPerson(Graphics g) {
		float sunAmount = (float) (Math.abs(Organism.angleDistance(
				gp.sunHeightAngle, Math.PI / 2)) / Math.PI);
		double sunsetAmount = Math.min((Math.PI - gp.sunHeightAngle),
				gp.sunHeightAngle)
				/ sunHorizon;
		Color currentSky;
		double drawSunHeightAngle = gp.sunHeightAngle % Math.PI;
		if (gp.quality) {
			int ovalWidth = getWidth() / 40, ovalHeight = getHeight() / 40, sunX = 0, sunY = 0;
			double drawSunAngle = gp.sunAngle;
			int hHeight = getHeight() / 2;
			if (drawSunHeightAngle > Math.PI / 2) {
				drawSunHeightAngle = Math.PI - drawSunHeightAngle;
				drawSunAngle = Organism.wrapAngle(drawSunAngle + Math.PI);
			}
			sunX = (int) ((-player.angleDistance(player.angle, drawSunAngle) / player.sightRange)
					* getWidth() + getWidth() / 2);
			sunY = (int) (getHeight() / 2 - getHeight()
					* (drawSunHeightAngle / Math.PI));
			float sunBlackAmount = (float) (hHeight - sunY) / ovalHeight;
			if (gp.sunHeightAngle <= Math.PI) {
				if (gp.sunHeightAngle < sunBlack
						|| Math.PI - gp.sunHeightAngle < sunBlack)
					currentSky = Predator.mergeColors(skyColor, Color.black,
							sunBlackAmount, 1);
				else
					currentSky = skyColor;
			} else
				currentSky = Color.black;
			g.setColor(currentSky);
			// draws sky
			g.fillRect(0, 0, (int) (width), (int) (getHeight() / 2) - 1);
			// draws sunset

			if (gp.sunHeightAngle < Math.PI) {
				double inc = 1 / 255.0;
				double r = hHeight;
				for (double i = 0; i < 1; i += inc) {
					g.setColor(Predator.mergeColors(currentSky, Predator
							.mergeColors(currentSky, sunsetRed,
									1 - (float) (1 - i), 1),
							(float) sunsetAmount, 1));
					g.fillRect(0, hHeight - (int) (r * i), getWidth(),
							(int) Math.ceil(r * inc));
				}
			}
			// draws sun (distance is neglegable)
			if (sunAmount <= 0.5)
				g.setColor(Color.YELLOW);
			else
				g.setColor(Color.WHITE);
			// draws sun / moon
			g.fillOval(sunX, sunY, ovalWidth, ovalHeight);
			// draws stars
			if (sunAmount >= 0.5)
				for (int i = 0; i < gp.stars.length; i++) {
					if (gp.stars[i].heightAngle < Math.PI) {
						double drawStarHeightAngle = gp.stars[i].heightAngle;
						double drawStarAngle = gp.stars[i].angle;
						if (drawStarHeightAngle > Math.PI / 2) {
							drawStarHeightAngle = Math.PI - drawStarHeightAngle;
							drawStarAngle = Organism.wrapAngle(drawStarAngle
									+ Math.PI);
						}
						g.setColor(new Color(1, 1, 1, Math.min(
								(float) (sunBlackAmount)
										* gp.stars[i].brightness, 1)));
						player.drawStar(g,
								(int) ((-player.angleDistance(player.angle,
										drawStarAngle) / player.sightRange)
										* getWidth() + getWidth() / 2),
								(int) (getHeight() / 2 - getHeight()
										* (drawStarHeightAngle / Math.PI)),
								gp.stars[i].width * getWidth(),
								gp.stars[i].points, gp.stars[i].startAngle);
					}
				}
		} else {
			g.setColor(skyColor);
			g.fillRect(0, 0, getWidth(), getHeight() / 2);
		}
		mainPolygonSet.reset();
		currentPolygonSet = mainPolygonSet;
		ArrayList<Organism> players = gp.players();
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i) != player) {
				players.get(i).drawFirstPerson(this, g);
				for (int b = 0; b < players.get(i).bullets.length; b++)
					if (players.get(i).bullets[b].live) {
						players.get(i).bullets[b].drawFirstPerson(this, g);
					}
			}
		}
		for (int i = 0; i < player.bullets.length; i++)
			if (player.bullets[i].live) {
				player.bullets[i].drawFirstPerson(this, g);
			}
		for (int i = 0; i < gp.plants.length; i++) {
			if (gp.plants[i] != null) {
				gp.plants[i].drawFirstPerson(this, g);
			}
		}
		drawFirstPerson(gp.prey, g);
		drawFirstPerson(gp.predators, g);
		g.setColor(gp.terrain.darker());

		shadows = new Polygon[] { null };
		shadowIndex = 0;
		mainPolygonSet.fillFirstPerson3DShadow(this, g);
		if (!gp.quality) {
			g.setColor(gp.terrain.darker().darker());
			if (shadows != null)
				for (int i = 0; i < shadows.length; i++)
					if (shadows[i] != null)
						g.fillPolygon(shadows[i]);
		}
		pixels = new boolean[getWidth()][getHeight()];
		mainPolygonSet.tryDrawWhole(this, g);
		// draws the floor (non moving)
		if (gp.quality) {
			boolean[][] shadowPixels = new boolean[getWidth()][getHeight()];
			Rectangle r;
			for (int i = 0; i < shadows.length; i++) {
				if (shadows[i] != null) {
					r = shadows[i].getBounds();
					for (int x = Math.max(r.x, 0); x < r.x + r.width
							&& x < getWidth(); x++)
						for (int y = Math.max(r.y, 0); y < r.y + r.height
								&& y < getHeight(); y++)
							if (!shadowPixels[x][y]
									&& shadows[i].contains(x, y))
								shadowPixels[x][y] = true;
				}
			}
			int dirtWidth = dirt.getWidth(this);
			if (dirtWidth > 1) {
				int dirtHeight = dirt.getHeight(this);
				double leftAngle = player.angle - player.sightRange / 2;
				double[] sinX = new double[getWidth()];
				double[] cosX = new double[getWidth()];
				for (int i = 0; i < getWidth(); i++) {
					double curAngle = player.sightRange * i / getWidth()
							+ leftAngle;
					sinX[i] = Math.sin(curAngle);
					cosX[i] = Math.cos(curAngle);
				}
				for (int y = getHeight() / 2; y < getHeight(); y++) {
					double distance = player.height
							* Math.tan((1 - ((double) y / getHeight()))
									* Math.PI);
					for (int x = 0; x < getWidth(); x++) {
						if (!pixels[x][y]) {
							int sourceY = (int) ((distance * sinX[x] + player.y) % (dirtWidth - 1));
							int sourceX = (int) ((distance * cosX[x] + player.x) % (dirtHeight - 1));
							if (sourceX >= 0 && sourceY >= 0)
								g
										.drawImage(dirt, x, y, x + 1, y + 1,
												sourceX, sourceY, sourceX + 1,
												sourceY + 1, this);
							if (shadowPixels[x][y]) {
								g.setColor(shadow);
								g.fillRect(x, y, 1, 1);
							}
						}
					}
				}
			}
		}
		// draws night time
		if (gp.quality && drawSunHeightAngle > Math.PI) {
			g.setColor(new Color(0, 0, 0, (float) (sunAmount * 0.8)));
			fillScreen(g);
		}
		// draws a red tint when injured
		final double accuracy = 0.25;
		float maxTintAlpha = Math.min(Math.max(
				1 - (float) (player.energy / (player.maxEnergy / 2)), 0), 1);
		if (player.energy < player.maxEnergy / 2) { // along x
			double maxInside = 0.5 * getWidth();
			int rateInt = (int) Math.max(getWidth() / (255 * accuracy * 2), 1);
			for (double i = 0; i < maxInside; i += rateInt) { // gradual fade
				g.setColor(new Color(1, 0, 0,
						(float) ((1 - (i / maxInside)) * maxTintAlpha)));
				g.fillRect((int) i, 0, rateInt, getHeight());
				g.fillRect((int) (getWidth() - i), 0, rateInt, getHeight());
			} // along y
			double maxInsideY = getHeight() / 2;
			int rateYInt = (int) Math
					.max(getHeight() / (255 * accuracy * 2), 1);
			for (double i = 0; i < maxInsideY; i += rateYInt) {
				g.setColor(new Color(1, 0, 0,
						(float) ((1 - (i / maxInsideY)) * maxTintAlpha)));
				g.fillRect(0, (int) i, getWidth(), rateYInt);
				g.fillRect(0, (int) (getHeight() - i - rateYInt), getWidth(),
						rateYInt);
			}
		}
		player.drawStun(this, g);
		if (gp.playType != GamePanel.NORMAL) {
			double r = 0.08;
			int hW;
			if (targetOnMouse)
				hW = mouseX;
			else
				hW = getWidth() / 2;
			int hH;
			if (targetOnMouse)
				hH = mouseY;
			else
				hH = getHeight() / 2;
			int w = (int) (getWidth() * r);
			int h = (int) (getHeight() * r);
			if (holdingGrenade) {
				if (nadeHoldTime <= Organism.nadeFuse) {
					float nonBaseAmount = 1 - (nadeHoldTime / (float) Organism.nadeFuse);
					g
							.setColor(new Color(nonBaseAmount, 1,
									nonBaseAmount, 0.3f));
				} else
					g.setColor(new Color(0, 0, 1, 0.3f));
			} else
				g.setColor(new Color(1, 1, 1, 0.3f));
			g.fillOval(hW - w, hH - h, w * 2, h * 2);
			g.setColor(Color.BLACK);
			w *= 0.1;
			h *= 0.1;
			g.drawLine(hW - w, hH - h, hW + w, hH + h);
			g.drawLine(hW + w, hH - h, hW - w, hH + h);
		}
	}

	public void fill2DPolygonTexture(Graphics g, int[] xs, int[] ys,
			double zoomX, double zoomY, Image texture) {
		fill2DPolygonTexture(g, new Texture(this, texture, new Polygon(xs, ys,
				xs.length), zoomX, zoomY));
	}

	public void fill2DPolygonTexture(Graphics g, Texture t) {
		t.drawWhole(this, g);
	}

	public void fillScreen(Graphics g) {
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	public void updateMouse(MouseEvent e) {
		if (gp.focused) {
			mouseX = e.getX();
			mouseY = e.getY();
			targetOnMouse = true;
			if (viewMode == FIRST_PERSON)
				repaint();
			else
				mouseMoved = true;
		}
	}

	public void paintComponent(Graphics g) {
		setOffset();
		setBackground(gp.terrain);
		super.paintComponent(g);
		if (viewMode == FIRST_PERSON)
			drawFirstPerson(g);
		else
			drawTopDown(g);
	}

	JPanel playerPanel;

	public void setPlayerPanel(Predator pred) {
		container.remove(playerPanel);
		playerPanel = pred.getJPanel(gp.predatorImg);
		container.add(playerPanel, BorderLayout.NORTH);
		if (gp.ports != null) // if still in constructer
			gp.reAddAllViewports();
	}

	public void reAddPlayerPanel() {
		container.remove(playerPanel);
		playerPanel = player.getJPanel(gp.predatorImg);
		container.add(playerPanel, BorderLayout.NORTH);
	}

	public double getFirstPersonAngle(double x) { // where x is the screen's x
		return x / getWidth() * player.sightRange + player.angle
				- player.sightRange / 2;
	}

	public double getFirstPersonY(GameItem g) {
		return getFirstPersonCielingY(g.x, g.y, g.height / 2); // shrink by
		// size
	}

	public double getFirstPersonCielingY(double x, double y, double z) {
		double d = player.distanceTo(x, y);
		return getHeight() / 2 - (Math.atan2(z - player.height, d) / Math.PI)
				* getHeight();
	}

	public double getAccurateFirstPersonX(double x, double y) {
		y -= player.y;
		x -= player.x;
		double invTan = 1 / Math.tan(player.angle);
		double tan = Math.tan(player.angle);
		double b2 = y + x * invTan;
		double x2 = b2 / (invTan + tan);
		double y2 = (b2 * tan) / (invTan + tan);
		double p = Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
		double hSR = player.sightRange / 2;
		double maxLength = 2 * (Organism.length(x2, y2) * Math.sin(hSR))
				/ Math.sin(90 - hSR);
		if (Organism.angleDistance(player.angle, Math.atan2(y, x)) > 0)
			p = -p;
		return ((p / maxLength) * getWidth()) + getWidth() / 2;
	}

	public double getScreenX(double relativeAngle) {
		return ((relativeAngle / player.sightRange) * getWidth())
				+ (getWidth() / 2.0);
	}

	public double distanceFrom(double relAngle, double screenX) {
		return Math.abs(getScreenX(relAngle) - screenX);
	}

	public double getHeight(double distance, double oHeight) {
		return (((Math.atan2(oHeight - player.height, distance)) + Math.atan2(
				player.height, distance)) / Math.PI)
				* getHeight();
	}

	public void resetFeet() {
		feet = new Footprint[feetToRecord];
		feetIndex = 0;
		maxFeetIndex = 0;
		distanceWalked = 0;
	}

	public void addFoot() {
		feet[feetIndex] = new Footprint(player, leftFoot);
		leftFoot = !leftFoot;
		if (maxFeetIndex < feetToRecord)
			maxFeetIndex++;
		else
			maxFeetIndex = feetToRecord;
		feetIndex++;
		if (feetIndex > feetToRecord - 1)
			feetIndex = 0;
	}

	public boolean updatePlayer(GamePanel gp, boolean invincible) {
		nadeHoldTime++;
		if (!gp.fastForward) {
			if (gp.flagged) {
				gp.flagged = !player.moveTorward(gp ,gp.mainFlagX, gp.mainFlagY,
						gp.goThrough);
				player.update(gp, gp.goThrough);
			} else {
				if (pressLeft)
					player.turnLeft();
				else if (pressRight)
					player.turnRight();
				else if (viewMode == FIRST_PERSON && targetOnMouse) {
					double r = 0.4;
					if (mouseX <= getWidth() * r)
						player.angle -= player.turnSpeed * 2
								* (1 - (mouseX / (getWidth() * r)));
					else if (mouseX >= getWidth() * (1 - r))
						player.angle += player.turnSpeed
								* 2
								* (1 - ((getWidth() - mouseX) / (getWidth() * r)));
					player.angle = Organism.wrapAngle(player.angle);
				} else if (mouseMoved)
					mouseMoved = !player.pointTorward(Math.atan2(mouseY
							- yOffset - player.y, mouseX - xOffset - player.x));

				if (pressUp)
					player.moveForward(gp);
				else if (pressDown)
					player.moveBackward(gp);

				player.update(gp, sprinting);
			}
			if (!player.alive && gp.playType != GamePanel.ALONE) {
				Predator.setPredText(gp);
			}
		} else
			player.updateAi(gp, gp.prey);
		if (gp.playType != GamePanel.ALONE)
			player.tryCreateChild(gp);
		else
			player.energy = Math.min(player.energy, player.maxEnergy);
		if (gp.playType != GamePanel.ALONE && gp.prey != null && gp.prey.size() == 0){
			JOptionPane
					.showMessageDialog(gp.frame,
							"You win! All the prey are dead!");
			gp.frame.dispose();
			return true;
		}
		else if (!player.alive) {
			if (invincible) {
				player.alive = true;
				player.energy = player.maxEnergy;
			} else {
				if (gp.playType != GamePanel.ALONE) {
					resetFeet();
					if (gp.predators == null || gp.predators.size() == 0) {
							JOptionPane.showMessageDialog(gp.frame,
									"You lose! All predators have died.");
						gp.frame.dispose();
						return true;
					} else
						((Predator) gp.predators.get(0)).gainPlayerControl(this);
				} else {
					player.x = gp.randomPosition();
					player.y = gp.randomPosition();
					player.energy = player.maxEnergy / 2;
					player.alive = true;
				}
			}
		}
		distanceWalked += GameItem.length(player.velocityX, player.velocityY);
		if (distanceWalked > player.width * 1.5) {
			addFoot();
			distanceWalked -= player.width * 1.5;
		}
		return false;
	}
}

class Drawable {
	public double maxDistance;
	public double minDistance;
	PolygonSet container;

	public Drawable(PolygonSet owner) {
		container = owner;
	}

	public void fillFirstPerson3DShadow(Viewport vp, Graphics g) {
	}

	public boolean drawFirstPerson(Viewport vp, Graphics g, int x, int y) {
		return false;
	}

	public void tryDrawWhole(Viewport vp, Graphics g) {
	}
}

class PolygonSet extends Drawable {
	Drawable[] polygons;
	int polygonIndex;

	public PolygonSet(PolygonSet owner) {
		super(owner);
		polygons = new Drawable[] { new Drawable(this) };
		reset();
	}

	public PolygonSet addNewPolygonSet() {
		PolygonSet new1 = new PolygonSet(this);
		add(new1);
		return new1;
	}

	public void reset() {
		polygonIndex = 0;
	}

	public void close() {
		maxDistance = polygons[0].maxDistance;
		minDistance = polygons[0].minDistance;
		for (int i = 1; i < polygons.length; i++) {
			maxDistance = Math.max(maxDistance, polygons[i].maxDistance);
			minDistance = Math.min(minDistance, polygons[i].minDistance);
		}
	}

	public void add(Drawable toDraw) {
		if (polygonIndex >= polygons.length) {
			Drawable[] temp = new Drawable[polygons.length + 1];
			for (int i = 0; i < polygons.length; i++)
				temp[i] = polygons[i];
			temp[polygonIndex] = toDraw;
			polygons = temp;
		} else
			polygons[polygonIndex] = toDraw;
		polygonIndex++;
	}

	public void fillFirstPerson3DShadow(Viewport vp, Graphics g) {
		for (int i = 0; i < polygons.length && polygons[i] != null; i++) {
			polygons[i].fillFirstPerson3DShadow(vp, g);
		}
	}

	public boolean drawFirstPerson(Viewport vp, Graphics g, int x, int y) {
		for (int i = 0; i < polygons.length && polygons[i] != null; i++)
			if (polygons[i].drawFirstPerson(vp, g, x, y))
				return true;
		return false;
	}

	public void tryDrawWhole(Viewport vp, Graphics g) {
		double minDistance = 0;
		double newMinDistance;
		boolean stillDraw = true;
		do { // draws objects in order from farthest to
			// closest
			stillDraw = false;
			newMinDistance = Double.MAX_VALUE;
			for (int i = 0; i < polygonIndex; i++) {
				if (polygons[i] != null) {
					if (polygons[i].minDistance == minDistance) {
						polygons[i].tryDrawWhole(vp, g);
					} else if (polygons[i].minDistance > minDistance) {
						newMinDistance = Math.min(polygons[i].minDistance,
								newMinDistance);
						stillDraw = true;
					}
				}
			}
			minDistance = newMinDistance;
		} while (stillDraw);
	}
}

class Polygon3D extends Drawable {
	double[] xs;
	double[] ys;
	double[] zs;
	double xOff;
	double yOff;
	double zOff;
	Rectangle r;

	public Polygon3D(PolygonSet owner) {
		super(owner);
	}

	public void fillFirstPerson3DShadow(Viewport vp, Graphics g) {
		vp.fillFirstPerson3DShadow(g, this);
	}

	public Polygon3D(PolygonSet owner, Viewport vp, double xO, double yO,
			double zO, double[] x, double[] y, double[] z) {
		super(owner);
		reset(vp, xO, yO, zO, x, y, z);
	}

	public void tryDrawWhole(Viewport vp, Graphics g) {
		if (r != null) {
			int maxX = Math.min(r.x + r.width, vp.getWidth());
			int maxY = Math.min(r.y + r.height, vp.getHeight());
			for (int x = Math.max(r.x, 0); x < maxX; x++) {
				for (int y = Math.max(r.y, 0); y < maxY; y++)
					if (!vp.pixels[x][y]) // if not drawn
						vp.pixels[x][y] = drawFirstPerson(vp, g, x, y);
			}
		}
	}

	public void reset(Viewport vp, double xO, double yO, double zO, double[] x,
			double[] y, double[] z) {
		xOff = xO;
		yOff = yO;
		zOff = zO;
		xs = x;
		ys = y;
		zs = z;
		vp.getDistance(this);
	}
}

class Polygon3DPlain extends Polygon3D {
	Color c;
	Polygon area;

	public Polygon3DPlain(PolygonSet owner, Viewport vp, Graphics g, double xO,
			double yO, double zO, double[] x, double[] y, double[] z) {
		super(owner, vp, xO, yO, zO, x, y, z);
		c = g.getColor();
		area = vp.getFirstPerson3DPolygon(this);
		if (area != null)
			r = area.getBounds();
	}

	public boolean drawFirstPerson(Viewport vp, Graphics g, int x, int y) {
		g.setColor(c);
		if (area != null && r.contains(x, y) && area.contains(x, y)) {
			g.fillRect(x, y, 1, 1);
			return true;
		}
		return false;
	}
}

class Polygon3DTexture extends Polygon3D {
	Texture texture;

	public Polygon3DTexture(PolygonSet owner, Viewport vp, Image i, double xO,
			double yO, double zO, double[] x, double[] y, double[] z) {
		super(owner, vp, xO, yO, zO, x, y, z);
		texture = new Texture(vp, i, this);
		r = texture.r;
	}

	public boolean drawFirstPerson(Viewport vp, Graphics g, int x, int y) {
		if (texture.area != null && texture.r.contains(x, y)
				&& texture.area.contains(x, y)) {
			texture.drawAt(vp, g, x, y);
			return true;
		}
		return false;
	}
}

class Texture {
	Image i;
	Polygon area;

	Rectangle r;
	double maxX, maxY, hR, wR, zX, zY;

	public Texture(Viewport vp, Image img, Polygon3D p3d) {
		i = img;
		area = vp.getFirstPerson3DPolygon(p3d);
		zX = 1;
		zY = 1;
		if (area != null)
			set(vp);
	}

	public Texture(JPanel jp, Image img, Polygon p, double zoomX, double zoomY) {
		i = img;
		area = p;
		zX = zoomX;
		zY = zoomY;
		set(jp);
	}

	public void set(JPanel io) {
		r = area.getBounds();
		maxX = r.getX() + r.getWidth();
		maxY = r.getY() + r.getHeight();
		hR = zY * i.getHeight(io) / (maxY - r.getY());
		wR = zX * i.getWidth(io) / (maxX - r.getX());
	}

	public void drawWhole(JPanel o, Graphics g) {
		boolean insideLine = false;
		double startX = 0;
		for (double y = r.getY(); y < maxY; y++) {
			for (double x = r.getX(); x < maxX; x++) {
				if (area.contains(x, y)) {
					if (!insideLine) {
						insideLine = true;
						startX = x;
					}
				} else if (insideLine) {
					g.drawImage(i, (int) startX, (int) y - 1, (int) x,
							(int) y + 1, (int) ((startX - r.getX()) * wR),
							(int) ((y - 1 - r.getY()) * hR), (int) ((x - r
									.getX()) * wR),
							(int) ((y + 1 - r.getY()) * hR), o);
					insideLine = false;
				}
			}
			if (insideLine)
				g.drawImage(i, (int) startX, (int) y - 1, (int) maxX,
						(int) y + 1, (int) ((startX - r.getX()) * wR),
						(int) ((y - 1 - r.getY()) * hR), (int) ((maxX - r
								.getX()) * wR),
						(int) ((y + 1 - r.getY()) * hR), o);
			insideLine = false;
		}
	}

	public void drawAt(JPanel j, Graphics g, int x, int y) {
		g.drawImage(i, (int) x - 1, (int) y - 1, (int) x + 1, (int) y + 1,
				(int) ((x - r.getX()) * wR), (int) ((y - r.getY()) * hR),
				(int) ((x + 1 - r.getX()) * wR),
				(int) ((y + 1 - r.getY()) * hR), j);
	}
}