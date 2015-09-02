import java.awt.*;

import javax.swing.*;

import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Organism extends GameItem {
	public double angle;// in radians
	// attributes
	// statics
	static double minSightAngle = Math.toRadians(50);
	static double maxSightAngle = Math.toRadians(270);
	// sightRanges
	static double maxSight = 250;
	static double minSight = 150;
	static double turnSpeed = Math.toRadians(7.5);
	static double normalMaxEnergy = 40;
	static double normalEnergyConsumption = 0.3;
	static double normalHomeostasisEnergyConsuption = 2.0;// 0.043;
	static double normalSlowDownAcceleration = 0.00055;
	static double energyConservation = 0.3; // energy retained after eating
	// food
	static double forcePerMuscle = 0.021; // moves __ its wieght
	// raw variables
	double rawScale; // also used to represent mass (in this case
	// interchangable)
	double rawWalkMuscle; // percentage of mass
	double rawSprintMuscle; // percentage of mass
	double rawSight; // distance between eyes (180 degrees = 0 distance)(0
	// degrees = maxDistance)

	// non-raw
	// acid
	double acidBuildup = 50;
	static double maxAcidBuildup = 100;
	static double acidRemoval = 0.52; // due to respritory system stuff
	static double acidPerMuscle = 2.97;

	// energy
	double energy; // out of max
	double maxEnergy;// then reproduces
	double energyConsumption;
	double homeostasisEnergyConsumption;
	double halfWidth;

	Color color;

	double sprintAcceleration;
	double walkAcceleration;

	public double sightRange; // total angle of view
	public double sightDistance; // radius of sight
	public double hearing;

	boolean hasChild = false;

	// movement
	double slowDownAcceleration;
	double walkForce;
	double sprintForce;
	double muscleUsed;

	boolean seen = false;

	static double maxStunTime = 25;
	static double numberOfStars = 5;
	static int starPoints = 7;
	static double timesToCircle = 1;
	static double starSize = 10;
	public boolean stunned = false;
	double stunAngle;
	double stunTime = 0;

	// JPanel stuff
	JPanel panel = null;
	JProgressBar energyBar;
	JProgressBar acidBar;
	JLabel pic;
	String name;

	// alive / death
	public boolean alive = true;

	// movement
	double currentSpeed = 0; // determined by force and mass
	double velocityX = 0;
	double velocityY = 0;
	double newDirectionX = 0; // added to velocity
	double newDirectionY = 0;
	double currentForce = 0;
	public boolean selected = false;
	protected double flagX;
	protected double flagY;

	public Weapon[] bullets;
	static double maxFireTime = 11;
	double fireTime;

	int generation;

	protected void setAttributes(double rScale, double rMuscle,
			double rSprintMuscle, double rSight, Color c, Image img) {
		// image is img to be used (used to generate JPanel)
		generation = 0;
		fireTime = 0;
		bullets = new Weapon[10];
		for (int i = 0; i < bullets.length; i++)
			bullets[i] = new Weapon();
		setAngle(Math.random() * Math.toRadians(360));
		rawScale = Math.max(rScale, 1);
		rawWalkMuscle = rMuscle;
		rawSprintMuscle = rSprintMuscle;
		rawSight = rSight;
		color = c;

		width = rawScale;
		height = width * 5;
		double maxWidthHeight = Math.sqrt(Math.pow(width, 2)
				+ Math.pow(width, 2));
		halfWidth = maxWidthHeight / 2;
		double mass = width * height;

		walkForce = rawWalkMuscle * forcePerMuscle * mass;
		sprintForce = rawSprintMuscle * forcePerMuscle * mass;

		sightRange = rawSight; // assuming each eye can
		// see 90 degrees
		sightDistance = maxSight
				- (((rawSight - minSightAngle) / (maxSightAngle - minSightAngle)) * (maxSight - minSight))
				+ halfWidth;

		walkAcceleration = walkForce / rawScale;
		sprintAcceleration = sprintForce / rawScale;

		maxEnergy = normalMaxEnergy * mass;
		energyConsumption = normalEnergyConsumption
				* (rawWalkMuscle + rawSprintMuscle) * mass;
		homeostasisEnergyConsumption = normalHomeostasisEnergyConsuption
				* width; // proportional to circumference
		slowDownAcceleration = normalSlowDownAcceleration * mass;
	} // 39 - 109 //s & s // v & m //

	public String ToString() {
		return "Size : " + String.format("%.2f", getSize()) + "\nSpeed : "
				+ (String.format("%.2f", getSpeed())) + "\nSprint Speed : "
				+ String.format("%.2f", getSprint()) + "\nSight Angle : "
				+ String.format("%.2f", getEyes());
	}

	public double getSize() {
		return rawScale;
	}

	public double getSpeed() {
		return (walkAcceleration) * 100;
	}

	public double getSprint() {
		return (walkAcceleration + sprintAcceleration) * 100;
	}

	public double getEyes() {
		return Math.toDegrees(sightRange);
	}

	public void moveToRandom(GamePanel gp) {
		x = gp.randomPosition();
		y = gp.randomPosition();
	}

	protected void setValuesRelativeTo(GamePanel gp, Organism other, Image img) {
		// sort of like a constructer
		this.setAttributes(close(gp, other.rawScale),
				close(gp, other.rawWalkMuscle),
				close(gp, other.rawSprintMuscle),
				close(gp, other.rawSight, minSightAngle, maxSightAngle),
				other.color, img);
		generation = other.generation + 1;
	}

	public static int getGeneration(ArrayList<Organism> orgs) {
		double total = 0;
		int dividend = 0;
		for (int i = 0; i < orgs.size(); i++) {
			if (!orgs.get(i).hasChild) {
				dividend++;
				total += orgs.get(i).generation;
			}
		}
		return (int) Math.round(total / dividend);
	}

	public void loseGainEnergy(double change) {
		energy += change;
		if (energyBar != null)
			energyBar.setValue((int) energy);
		if (energy <= 0)
			alive = false;
	}

	public double stun() { // returns energy lost
		if (!stunned) {
			energy -= maxEnergy * 0.2;
			acidBuildup = maxAcidBuildup;
			stunned = true;
			stunTime = 0;
			stunAngle = Math.random() * (Math.PI * 2);
			return maxEnergy * 0.05;
		}
		return 0;
	}

	public void fireWeapon(GamePanel gp, int timeHold, double angle,
			double vertAngle, int weaponType) {
		if (gp.playType != GamePanel.NORMAL && !stunned) {
			for (int i = 0; i < bullets.length; i++) {
				if (!bullets[i].live) {
					fireWeapon(bullets[i], angle, vertAngle, weaponType,
							timeHold);
					lastFired = i;
					return;
				}
			}
			lastFired++;
			if (lastFired >= bullets.length)
				lastFired = 0;
			fireWeapon(bullets[lastFired], angle, vertAngle, weaponType,
					timeHold);
		}
	}

	int lastFired = 0;

	private void fireWeapon(Weapon t, double angle, double vertAngle,
			int weaponType, int timeHold) {
		if (weaponType == TRANQ)
			t.fireTranq(this, angle, vertAngle);
		else
			t.fireNade(this, timeHold, angle, vertAngle);
	}

	public static final int NADE = 0;
	public static final int TRANQ = 1;
	public static final int EXPLOSION = 2;
	public static final double explosionGrowth = 30;
	public static final int nadeFuse = 60;
	public static final double gravity = 0.581;

	class Weapon extends GameItem {
		Organism owner;
		double velocityX;
		double velocityY;
		double velocityZ;
		double accX;
		double accY;
		boolean live;
		double angle;
		double z;
		double explosionCenterZ;
		double explosionHeight;
		int type;
		int time;

		public Weapon() {
			live = false;
		}

		public void draw(Viewport vp, Graphics g) {
			if (live) {
				setColor(g);
				int hW = (int) (width / 2);
				g.fillOval((int) (x - hW + vp.xOffset),
						(int) (y - hW + vp.yOffset), (int) width, (int) width);
			}
		}

		public void fireTranq(Organism o, double angle, double verticalAngle) {
			fire(o, 10, 10, 40, -0.005, angle, verticalAngle);
			type = TRANQ;
		}

		public void fireNade(Organism o, int time2, double angle,
				double verticalAngle) {
			fire(o, 20, 20, 20, -0.05, angle, verticalAngle);
			type = NADE;
			time = time2;
		}

		public void fire(Organism o, double width2, double height2,
				double speed, double acc, double angle2, double verticalAngle) {
			owner = o;
			width = width2;
			height = height2;
			angle = angle2;
			accX = Math.cos(angle) * acc;
			accY = Math.sin(angle) * acc;
			velocityZ = Math.sin(verticalAngle) * speed;
			double xy = Math.cos(verticalAngle) * speed;
			velocityX = Math.cos(angle) * xy + o.velocityX;
			velocityY = Math.sin(angle) * xy + o.velocityY;
			x = o.x + Math.cos(angle) * ((o.width + width) / 2);
			y = o.y + Math.sin(angle) * o.width;
			z = o.height;
			live = true;
			time = 0;
		}

		public void update(GamePanel gp) {
			if (live) {
				time++;
				velocityX += accX;
				velocityY += accY;
				if (type != EXPLOSION) {
					velocityZ -= gravity;// gravity
					z += velocityZ;
				}
				if (z <= 0) {
					z = 0;
					if (type == NADE) {
						velocityZ = -velocityZ * 0.8; // buonces back with 80%
						if (velocityZ < 1) {
							velocityX *= 0.1;
							velocityY *= 0.1;
						}
					} else if (type == EXPLOSION)
						z = 0;
					else {// TRANQ
						die();
						return;
					}
				}
				if (time > nadeFuse && type == NADE) {
					explode();
				}
				if (type == EXPLOSION) {
					width += explosionGrowth;
					explosionHeight += explosionGrowth;
					if (explosionCenterZ - height / 2 < 0) {
						z = 0;
						height = explosionHeight / 2 + explosionCenterZ;
					} else {
						height = explosionHeight;
						z = explosionCenterZ - height / 2;
					}
					if (width >= 400) {
						die();
						return;
					}
				}
				x += velocityX;
				y += velocityY;
				if (type != NADE) {
					if (gp.prey != null)
						for (int i = 0; i < gp.prey.size(); i++)
							if (checkAgainst(gp.prey.get(i)))
								return;
					if (gp.predators != null)
						for (int i = 0; i < gp.predators.size(); i++)
							if (checkAgainst(gp.predators.get(i)))
								return;
					ArrayList<Organism> players = gp.players();
					for (int i = 0; i < players.size(); i++)
						checkAgainst(players.get(i));
				}
			}
		}

		public void explode() {
			type = EXPLOSION;
			explosionCenterZ = z;
			explosionHeight = height;
		}

		public void setColor(Graphics g) {
			if (type == EXPLOSION) {
				g.setColor(new Color(0, 0, 1, 0.5f));
			} else if (type == TRANQ)
				g.setColor(Color.RED);
			else {
				float nonBase = Math.max(
						Math.min(1 - (time / (float) nadeFuse), 1), 0);
				g.setColor(new Color(nonBase, 1, nonBase));
			}

		}

		public void drawFirstPerson(Viewport vp, Graphics g) {
			if (live) {
				int sides = 3;
				if (type == EXPLOSION)
					sides = 7;
				setColor(g);
				vp.fillVerticalCylindricalPolygon(g, x, y, z - height / 2,
						angle, width, sides, height);
			}
		}

		public void die() {
			live = false;
		}

		public boolean checkAgainst(Organism o) { // assuming live
			if (z - height / 2 <= o.height + height / 2 && intersects(o)) {
				owner.loseGainEnergy(o.stun());
				if (type == TRANQ) {
					die();
					return true;
				}
			}
			return false;
		}
	}

	public void unstun() {
		stunned = false;
	}

	public double power() {
		return Math.random() * (width / 2) + (width / 2);
	}

	public boolean canSee(GameItem other) {
		return distanceTo(other) < hearing + other.width // can hear
				|| (distanceTo(other) < sightDistance + other.width // checks
				// distance
				&& canSeeIgnoreDistance(other));
	}

	public boolean canSeeIgnoreDistance(GameItem g) {
		return canSeeIgnoreDistance(g.x, g.y);
	}

	public boolean canSeeIgnoreDistance(double x2, double y2) {
		return Math.abs(angleDistance(wrapAngle(Math.atan2( // checks angle
				y2 - this.y, x2 - this.x)), angle)) <= sightRange / 2;
	}

	public boolean canSee(double x, double y) {
		double d = distanceTo(x, y);
		return d < hearing // can hear
				|| (d < sightDistance // checks
				// distance
				&& Math.abs(angleDistance(wrapAngle(Math.atan2( // checks angle
						y - this.y, x - this.x)), angle)) <= sightRange / 2);

	}

	public void setRandomFlag(GamePanel gp) {
		flagX = gp.randomPosition();
		flagY = gp.randomPosition();
	}

	public boolean pointTorward(double angle2) { // returns whether or not
		// already pointing
		double diff = angleDistance(angle, angle2);
		if (Math.abs(diff) + Math.toRadians(1) > turnSpeed) {
			if (diff > 0)
				turnLeft();
			else
				turnRight();
			return false;
		}
		// angle = angle2;
		return true;
	}

	public boolean moveTorward(GamePanel gp,double x2, double y2, boolean goThrough) {
		// true if no longer needs moving
		double distanceTo = distanceTo(x2, y2);
		if (distanceTo > width / 2) {
			if ((pointTorward(wrapAngle(Math.atan2(y2 - y, x2 - x))) && (goThrough || (Math
					.pow(length(velocityX, velocityY), 2) / (2 * slowDownAcceleration)) <= distanceTo))) {
				moveForward(gp);
			}
			return false;
		}
		return true;
	}

	public ArrayList<Organism> remove(ArrayList<Organism> from,
			Organism toRemove) {
		from.remove(toRemove);
		return from;
	}

	public void drawFirstPerson(Viewport vp, Graphics g) {
		this.drawStun(vp, g);
	}

	public static ArrayList<Organism> add(ArrayList<Organism> from,
			Organism toAdd) {
		// add to an array
		if (from != null) {
			from.add(toAdd);
			return from;
		} else
			return new ArrayList<Organism>(
					Arrays.asList(new Organism[] { toAdd }));
	}

	public static ArrayList<Organism> add(ArrayList<Organism> from,
			ArrayList<Organism> toAdd) {
		// add to an array
		if (from != null) {
			from.addAll(toAdd);
			return from;
		} else
			return toAdd;
	}

	public static double angleDistance(double angleA, double angleB) {
		double a = -rightDistance(angleA, angleB);
		double b = leftDistance(angleA, angleB);
		if (Math.abs(b) > Math.abs(a))
			return a;
		return b;
	}

	public static double leftDistance(double angleA, double angleB) {
		if (angleA > angleB)
			return angleA - angleB;
		else
			return Math.toRadians(360) - angleB + angleA;
	}

	public static double rightDistance(double angleA, double angleB) {
		return leftDistance(angleB, angleA);
	}

	public void devour(double energy) {
		loseGainEnergy(energy * energyConservation);
	}

	public double angleTo(double x2, double y2) {
		return wrapAngle(Math.atan2(y2 - y, x2 - x));
	}

	public double angleTo(GameItem g) {
		return angleTo(g.x, g.y);
	}

	public void update(GamePanel gp, boolean sprint) {
		// reset vars
		currentSpeed = 0;
		currentForce = 0;
		muscleUsed = 0;
		seen = false;

		getSpeed(sprint);
		if (gp.playType == GamePanel.ALONE)
			loseGainEnergy(homeostasisEnergyConsumption);
		else {
			loseGainEnergy(-homeostasisEnergyConsumption); // lose energy for living
		}
		if (muscleUsed > 0) {
			if (gp.playType == GamePanel.NORMAL
					|| (gp.playType != GamePanel.NORMAL && sprint))
				loseGainAcid(acidGain(muscleUsed));
		} else
			loseGainAcid(-acidRemoval);

		if (stunned) {
			if (stunTime >= maxStunTime)
				unstun();
			else {
				stunTime++;
				stunAngle += ((Math.PI * 2) / maxStunTime) * timesToCircle;
			}
		}
		// update movement based on direction and current speed
		velocityX += newDirectionX * currentSpeed;
		velocityY += newDirectionY * currentSpeed;

		if (currentSpeed == 0)
			slowDown();

		x = Math.max(0, Math.min(gp.mapWidth, x + velocityX));
		y = Math.max(0, Math.min(gp.mapHeight, y + velocityY));

		if (gp.playType != GamePanel.NORMAL) {
			fireTime++;
			for (int i = 0; i < bullets.length; i++)
				bullets[i].update(gp);
		}
		// reset variables
		still = true;
	}

	public void shootAt(GamePanel gp, Organism o) {
		if (freeBullet() && !o.stunned) {
			if (fireTime > maxFireTime) {
				fireWeapon(gp, 0, this.angleTo(o), 0, TRANQ);
				fireTime = 0;
			}
		}
	}

	public boolean freeBullet() {
		for (int i = 0; i < bullets.length; i++)
			if (!bullets[i].live)
				return true;
		return false;
	}

	private void slowDown() {// apply friction onto current velocity
		// (normalized)
		double vectorLength = Math.sqrt(Math.pow(velocityX, 2)
				+ Math.pow(velocityY, 2));// the length
		if (slowDownAcceleration < vectorLength) {
			double scale = slowDownAcceleration / vectorLength;
			velocityX -= velocityX * scale;
			velocityY -= velocityY * scale;
		} else {
			velocityX = 0;
			velocityY = 0;
		}

	}

	private double acidGain(double muscle) {
		return muscle * acidPerMuscle;
	}

	public boolean canSprint() {
		return acidBuildup + acidGain(rawWalkMuscle + rawSprintMuscle) < maxAcidBuildup;
	}

	public double distanceCanTravel() {
		// before acid buildup is 99% (assuming sprinting)
		return (length(velocityX, velocityY) * timeCanTravel()) + 0.5
				* (walkAcceleration + sprintAcceleration)
				* Math.pow(timeCanTravel(), 2);
	}

	public double timeCanTravel() {
		// time can travel sprinting
		return (maxAcidBuildup - acidBuildup)
				/ ((rawSprintMuscle + rawWalkMuscle) * acidPerMuscle);
	}

	public JPanel getJPanel(Image img) {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.setToolTipText("Shows Current Status");

			// picture
			pic = new JLabel(name, new ImageIcon(img.getScaledInstance(
					(int) width, (int) width, Image.SCALE_DEFAULT)),
					SwingConstants.CENTER);
			panel.add(pic, BorderLayout.WEST);

			// energy bar
			energyBar = new JProgressBar(0, (int) maxEnergy);
			energyBar.setValue((int) energy);
			energyBar.setStringPainted(true);
			energyBar.setString("Energy Level");
			energyBar.setToolTipText("Current Energy Level");
			panel.add(energyBar, BorderLayout.NORTH);

			// acid bar
			acidBar = new JProgressBar(0, (int) maxAcidBuildup);
			acidBar.setValue((int) acidBuildup);
			acidBar.setStringPainted(true);
			acidBar.setString("Fatigue");
			acidBar.setToolTipText("Acid buildup in muscles");
			panel.add(acidBar, BorderLayout.SOUTH);

			JTextArea info = new JTextArea(ToString());
			info.setToolTipText("Traits");
			info.setEditable(false);
			panel.add(info, BorderLayout.CENTER);
		}
		return panel;
	}

	public static double close(GamePanel gp, double a) {
		double range = a * gp.variationLevel;
		return Math.max(Math.random() * range + (a - range / 2), 0.01);
	}

	public static double close(GamePanel gp, double a, double min, double max) {
		double range = a * gp.variationLevel;
		return Math.min(Math.max(Math.random() * range + (a - range / 2), min),
				max);
	}

	public void setAngle(double newAngle) {
		angle = wrapAngle(newAngle);
	}

	public static double wrapAngle(double newAngle) {
		// between
		newAngle %= Math.PI * 2; // eliminates complete circles
		if (newAngle < 0)
			newAngle += Math.PI * 2; // eliminates negative angles
		return newAngle;
	}

	private void getSpeed(boolean sprint) {
		if (!alive)
			return;
		if (!still && acidBuildup + acidGain(rawWalkMuscle) < maxAcidBuildup) {
			currentForce = walkForce; // set basic varaibles
			currentSpeed = walkAcceleration;
			muscleUsed = rawWalkMuscle;
			if (sprint && canSprint()) { // add onto each variable if sprinting
				// that way sprinting > walking always
				currentSpeed += sprintAcceleration;
				currentForce += sprintForce;
				muscleUsed += rawSprintMuscle;
			}
		}
	}

	public void loseGainAcid(double lg) {
		acidBuildup = Math.min(Math.max(lg + acidBuildup, 0), maxAcidBuildup);
		if (acidBar != null)
			acidBar.setValue((int) acidBuildup);
	}

	boolean still = true; // not moving

	protected void moveForward(GamePanel gp) {
		move(gp, angle);
	}

	public void moveLeft(GamePanel gp) {
		move(gp,angle - Math.PI / 2);
	}

	public void moveRight(GamePanel gp) {
		move(gp, angle + Math.PI / 2);
	}

	private void move(GamePanel gp, double angle) // where d is the
	// distance to move
	{
		still = false;
		newDirectionX = Math.cos(angle);
		newDirectionY = Math.sin(angle);
		loseGainEnergy(-energyConsumption * currentSpeed);
	}

	protected void moveBackward(GamePanel gp) {
		move(gp, angle + Math.toRadians(180));
	}

	protected void turnLeft() {
		setAngle(angle - turnSpeed);
	}

	protected void turnRight() {
		setAngle(angle + turnSpeed);
	}

	boolean stillChangingImage = true;

	public void drawStar(Graphics g, double x, double y, double width,
			int points, double startAngle) {
		int[] xs = new int[points * 2];
		int[] ys = new int[points * 2];
		double currentAngle = startAngle;
		double angleIncrement = Math.PI / points; // simplified equation
		double longDistance = width;
		double shortDistance = width / 2;
		for (int i = 0; i < xs.length; i++) {
			double d;
			if (i % 2 == 0) // small point (even indici)
				d = shortDistance;
			else
				d = longDistance;
			xs[i] = (int) ((Math.cos(currentAngle) * d) + x);
			ys[i] = (int) ((Math.sin(currentAngle) * d) + y);
			currentAngle += angleIncrement;
		}
		g.fillPolygon(xs, ys, points * 2);
	}

	public void drawImage(Viewport vp, Graphics2D g, Image img) {
		if (stillChangingImage) {
			if (pic != null)
				pic.setIcon(new ImageIcon(img.getScaledInstance((int) width,
						(int) width, Image.SCALE_DEFAULT)));
		}
		// draws the top down image
		stillChangingImage = !drawImage(vp, g, img, x + vp.xOffset, y
				+ vp.yOffset, width, width, angle + Math.toRadians(90), color);
	}

	public void drawBullets(Viewport vp, Graphics g) {
		if (bullets != null)
			for (int i = 0; i < bullets.length; i++) {
				bullets[i].draw(vp, g);
			}
	}

	public void drawStun(Viewport vp, Graphics g) {
		if (stunned) {
			g.setColor(Viewport.stunColor);
			double drawAngle = stunAngle;
			double starAngleInc = (Math.PI * 2) / numberOfStars;
			double d = width + starSize;
			for (int i = 0; i < numberOfStars; i++) {
				double starX = Math.cos(drawAngle) * d + x;
				double starY = Math.sin(drawAngle) * d + y;
				if (vp.viewMode == Viewport.FIRST_PERSON) { // if first person
					double y2;
					if (vp.player == this)
						y2 = vp.height / 2;
					else
						y2 = vp.getFirstPersonY(this);
					drawStar(g, vp.getAccurateFirstPersonX(starX, starY), y2,
							vp.getHeight(vp.player.distanceTo(starX, starY),
									starSize), starPoints, stunAngle); // draws
					// stars
					// at
					// half
					// height
				} else
					drawStar(g, starX + vp.xOffset, starY + vp.yOffset,
							starSize, starPoints, stunAngle); // draws stars
				// circling
				drawAngle += starAngleInc;
			}
		}
	}

	public static AffineTransform rotate(Graphics2D g, double angle, double x,
			double y) { // rotate a graphics2d
		AffineTransform original = g.getTransform();
		AffineTransform rotated = ((AffineTransform) original.clone());
		rotated.rotate(angle, x, y);
		g.setTransform(rotated);
		return original;
	}

	public static boolean drawImage(JPanel jp, Graphics2D g, Image i, double x,
			double y, double width, double height, double angle, Color c) { // less
		// parameters
		return drawImage(jp, g, i, x, y, width, height, (width / 2),
				(height / 2), angle, c);
	}

	public static boolean drawImage(JPanel jp, Graphics2D g, Image i, double x,
			double y, double width, double height, double originX,
			double originY, double angle, Color c) { // draws a rotated image
		AffineTransform original = rotate(g, angle, x, y);
		boolean returnValue = g.drawImage(i, (int) (x - originX),
				(int) (y - originY), (int) width, (int) height, jp);

		g.setTransform(original);
		return returnValue;
	}
}