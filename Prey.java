import java.awt.*;
import java.util.ArrayList;

public class Prey extends Organism {
	Predator fleeing = null;
	Autotroph feeding = null;
	static double maxSprintTime = 0.4; // runs away for __ seconds
	static double normalEnergyPerBite = 10;
	double sprintTime = 0; // in seconds
	double runningAngle;
	double energyPerBite;
	double wingFlap;

	public Prey(GamePanel gp, double xP, double yP, double rScale,
			double rSpeed, double rSprint, double rSight, Color c) {
		this.setAttributes(rScale, rSpeed, rSprint, rSight, c, gp.preyImg);
		setBasics(gp);
		setSpecificValues(gp, xP, yP);
	}

	public Prey(GamePanel gp, Prey parent) { // use when making a child
		this.setValuesRelativeTo(gp, parent, gp.preyImg);
		setBasics(gp);
		setSpecificValues(gp, parent.x, parent.y);
	}

	private void setSpecificValues(GamePanel gp, double xP, double yP) {
		// to shorten code don't use except in constructers
		x = xP;
		y = yP;
		energyPerBite = normalEnergyPerBite * rawScale * rawScale;
		energy = maxEnergy / 2;
		setRandomFlag(gp);
		wingFlap = Math.random() * Math.PI;
	}

	private void setBasics(GamePanel gp) { // only call in constructers
		gp.preyCreated++;
		name = "Prey #" + gp.preyCreated + " : Gen #" + generation;
		setPreyText(gp);
		addPrey(gp, this);
	}

	public void setPreyText(GamePanel gp) {
		String text = "Prey alive : " + gp.preyAlive();
		if (gp.prey != null)
			gp.stats.prey.setText(text + " :: Generations Passed : "
					+ getGeneration(gp.prey));
		else
			gp.stats.prey.setText(text);
	}

	public void die(GamePanel gp) {
		// kill this
		alive = false;
		removePrey(gp, this);
		setPreyText(gp);
	}

	public void addPrey(GamePanel gp, Prey toAdd) {
		gp.prey = add(gp.prey, toAdd);
	}

	public void removePrey(GamePanel gp, Prey prey) {
		gp.prey = remove(gp.prey, prey);
	}

	private void setFleeing(GamePanel gp, Predator p) {
		fleeing = p;
		feeding = null;
		setRandomFlag(gp);
		setRunningAngle();
	}

	public void update(GamePanel gp, boolean sprinting) {
		super.update(gp, sprinting);
		flapWings();
	}

	public void flapWings() {
		double r = Math.PI * 6 / 180;
		wingFlap += r;
		wingFlap %= Math.PI;
	}

	public void updateAi(GamePanel gp, ArrayList<Organism> predators,
			Autotroph[] plants) {
		boolean sprinting = false;
		if (selected && gp.flagged) {
			selected = gp.fastForward
					&& !moveTorward(gp,gp.mainFlagX, gp.mainFlagY, gp.goThrough);
			sprinting = gp.goThrough;
		} else {
			if (fleeing == null && predators != null) {
				for (int i = 0; i < predators.size(); i++) {
					if (canSee(predators.get(i))
							&& ((Predator) predators.get(i)).canEat(this)) {
						//if (gp.playType != GamePanel.NORMAL)
						//	shootAt(gp, gp.predators.get(i));
						setFleeing(gp, (Predator) predators.get(i));
						break;
					}
				}
				for (int i = 0; i < gp.ports.length; i++)
					if (canSee(gp.ports[i].player)
							&& gp.ports[i].player.canEat(this)) {
						//if (gp.playType != GamePanel.NORMAL)
						//	shootAt(gp, gp.ports[i].player);
						setFleeing(gp, gp.ports[i].player);
					}
			}
			if (fleeing != null) {
				if (fleeing.alive) {
					sprinting = true;
					sprintTime += 0.01;
					if (pointTorward(runningAngle))
						moveForward(gp);
					if (sprintTime > maxSprintTime)
						if (canSee(fleeing))
							setRunningAngle();
						else
							fleeing = null;
				} else
					fleeing = null;
			} else { // if not running
				sprinting = false;
				if (feeding != null) {
					if (feeding.energy > energyPerBite) {
						if (moveTorward(gp,flagX, flagY, false)) {
							devour(energyPerBite);
							feeding.energy -= energyPerBite;
						}
					} else {
						feeding = null;
						setRandomFlag(gp);
					}
				}
				if (feeding == null) {
					if (acidBuildup < maxAcidBuildup / 4
							&& moveTorward(gp,flagX, flagY, false))
						setRandomFlag(gp);
					for (int i = 0; i < plants.length; i++)
						if ((plants[i].energy > plants[i].maxEnergy / 2 || plants[i].energy > maxEnergy / 4)
								&& canSee(plants[i]) && Math.random() > 0.5) {
							feeding = plants[i];
							flagX = feeding.randomX();
							flagY = feeding.randomY();
							break;
						}
				}
			}
		}
		update(gp, sprinting && canSprint());
		if (!alive)
			die(gp);
	}

	public void drawFirstPerson(Viewport vp, Graphics g) {
		vp.startPolygonSet();
		super.drawFirstPerson(vp, g);
		double perpAngle = angle + Math.PI / 2;
		double headHeight = height * 0.3;
		double hW = width / 2;
		double[][] headXs = new double[2][4];
		double[][] headYs = new double[2][4];
		double[][] headZs = new double[2][4];
		double[] centerX = new double[] { x + Math.cos(angle) * hW,
				x + Math.cos(angle + Math.PI) * hW };
		double[] centerY = new double[] { y + Math.sin(angle) * hW,
				y + Math.sin(angle + Math.PI) * hW };
		for (int i = 0; i < 2; i++)
			Viewport.getVerticalBases(centerX[i], centerY[i], height
					- headHeight / 2, Math.PI / 4, angle,
					Math.sqrt(2 * Math.pow(headHeight / 2, 2)), headXs[i],
					headYs[i], headZs[i]);
		g.setColor(Color.YELLOW);
		vp.fillCylindricalPolygon(g, 0, 0, 0, headXs, headYs, headZs);
		g.setColor(Color.ORANGE);
		vp.fillHorizontalPyramid(g, centerX[0], centerY[0], height - headHeight
				/ 2, Math.PI / 4, angle, headHeight / 4, 4, width / 2, height
				- headHeight / 2);
		// draws legs
		double bodyWidth = width * 0.8;
		double legHeight = height * 0.4;
		double legWidth = width * 0.2;
		double z2 = legHeight * 0.1;
		double legSpacing = (bodyWidth - legWidth) / 2;
		g.setColor(Color.ORANGE);
		for (int i = 0; i < 2; i++) {
			double theAngle = angle + Math.PI / 2 + Math.PI * i;
			double x2 = x + Math.cos(theAngle) * legSpacing;
			double y2 = y + Math.sin(theAngle) * legSpacing;
			// draws a foot
			vp.fillHorizontalPyramid(g, x2, y2, z2, Math.PI / 4, angle,
					Math.sqrt(Math.pow(z2, 2) * 2), 4, width * 0.3, 0);
			// draws a leg
			vp.fillVerticalCylindricalPolygon(g, x2, y2, 0, angle,
					legWidth / 2, 3, legHeight);
		}
		// draws body
		double bodyHeight = height - legHeight - headHeight;
		double hBW = bodyWidth / 2;
		g.setColor(Color.YELLOW);
		vp.fillVerticalCylindricalPolygon(g, x, y, legHeight, angle, hBW, 4,
				bodyHeight);
		// draws wings (as pyramids)
		double flapAngle = wingFlap + Math.PI;// that way always negative (add
		// sugar to coffee)
		double wingLength = width;
		double wingZ = Math.sin(flapAngle) * wingLength + legHeight
				+ bodyHeight / 2;
		double xyDistance = Math.cos(flapAngle) * wingLength;
		for (int i = 0; i < 2; i++) {
			double dir = perpAngle + Math.PI * i;
			vp.fillHorizontalPyramid(g, x + Math.cos(dir) * hBW,
					y + Math.sin(dir) * hBW, legHeight + bodyHeight / 2,
					Math.PI / 4, dir, bodyHeight / 2, 4, Math.abs(xyDistance),
					wingZ);
		}
		vp.endPolygonSet();
	}

	private boolean canSee(Autotroph plant) {
		return distanceTo(plant.centerX(), plant.centerY()) + (plant.width / 2) < sightDistance;
	}

	static double fleeAngleCloseness = Math.toRadians(40);

	private void setRunningAngle() { // run at a random angle
		sprintTime = 0;
		runningAngle = wrapAngle(-(Math.random() * fleeAngleCloseness * 2
				+ Math.atan2(fleeing.y - y, fleeing.x - x) - fleeAngleCloseness));

	}

	public void tryCreateChild(GamePanel gp) {
		if (energy >= maxEnergy) { // saftey in numbers
			Prey child = new Prey(gp, this);
			loseGainEnergy(-child.energy);
			hasChild = true;
		}
	}
}