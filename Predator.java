import java.awt.*;
import java.util.ArrayList;

public class Predator extends Organism {
	// Image background
	Prey chasing = null;

	public Predator(GamePanel gp, double xP, double yP, double rScale,
			double rSpeed, double rSprint, double rSight, Color c) {
		x = xP;
		y = yP;
		this.setAttributes(rScale, rSpeed, rSprint, rSight, c, gp.predatorImg);
		setBasics(gp);
		setRandomFlag(gp);
		energy = maxEnergy / 2;
	}

	public void drawBackground(Viewport vp, Graphics g) {
		// draws fade of vision
		
		int startAngle = (int) -Math.toDegrees(sightRange / 2 + angle);
		int arcAngle = (int) Math.toDegrees(sightRange);
		g.setColor(new Color(GamePanel.viewColor.getRed(), GamePanel.viewColor
				.getGreen(), GamePanel.viewColor.getBlue(), 128));
		g.fillArc((int) (x - sightDistance + vp.xOffset), (int) (y
				- sightDistance + vp.yOffset), (int) sightDistance * 2,
				(int) sightDistance * 2, startAngle, arcAngle);
	}

	public Predator(GamePanel gp, Predator parent) { // use when making a child
		this.setValuesRelativeTo(gp, parent, gp.predatorImg);
		setBasics(gp);
		setRandomFlag(gp);
		x = parent.x;
		y = parent.y;
		energy = maxEnergy / 2;
	}

	private void setBasics(GamePanel gp) { // only call in constructors
		gp.predatorsCreated++;
		addPredator(gp, this);
		name = "Predator #" + gp.predatorsCreated + " : Gen #" + generation;
		setPredText(gp);
	}

	public void gainPlayerControl(Viewport vp) {
		vp.player = this;
		removePredator(vp.gp, this);
		vp.setPlayerPanel(this);
	}

	public static Color mergeColors(Color start, Color end, float amount,
			float alpha) {
		float endRed = end.getRed() / 255f;
		float endGreen = end.getGreen() / 255f;
		float endBlue = end.getBlue() / 255f;
		float diffRed = (start.getRed() / 255f - endRed);
		float diffBlue = (start.getBlue() / 255f - endBlue);
		float diffGreen = (start.getGreen() / 255f - endGreen);

		float d = Math.max(Math.min(amount, 1), 0);
		float a = Math.max(Math.min(alpha, 1), 0);
		return new Color(endRed + diffRed * d, endGreen + diffGreen * d,
				endBlue + diffBlue * d, a);
	}

	public void losePlayerControl(Viewport vp) {
		Viewport[] temp = new Viewport[vp.gp.ports.length - 1];
		for (int i = 0; i < temp.length; i++)
			temp[i] = vp.gp.ports[i];
		vp.gp.ports = temp;
		vp.gp.reAddAllViewports();
		addPredator(vp.gp, this);
	}

	public static void setPredText(GamePanel gp) {
		String text = "Predators alive : " + gp.predatorsAlive();
		if (gp.predators != null)
			gp.stats.pred.setText(text + " :: Generations Passed : "
					+ getGeneration(gp.predators));
		else
			gp.stats.pred.setText(text);
	}

	public void die(GamePanel gp) {
		alive = false;
		removePredator(gp, this);
		setPredText(gp);
	}

	public void addPredator(GamePanel gp, Predator toAdd) {
		if (gp != null && gp.ports != null && toAdd == gp.ports[0].player){
			System.out.println("ADDING PLAYER");
		}
		if (gp != null && gp.predators != null && gp.predators.contains(toAdd))
			System.out.println("Already ADDED");
		gp.predators = add(gp.predators, toAdd);
	}

	public void removePredator(GamePanel gp, Predator pred) {
		gp.predators = remove(gp.predators, pred);
	}

	boolean slowingDown = false;

	public void updateAi(GamePanel gp, ArrayList<Organism>  prey) {
		boolean sprinting = true;
		if (selected && gp.flagged) {
			selected = gp.fastForward
					&& !moveTorward(gp, gp.mainFlagX, gp.mainFlagY, gp.goThrough);
			sprinting = gp.goThrough;
		} else {
			if (chasing == null) {
				if (prey != null) {
					for (int i = 0; i < prey.size(); i++) {
						if (shouldEat(prey.get(i)) && canSee(prey.get(i))) {
							if (intersects(prey.get(i)))
								eat(gp, (Prey) prey.get(i)); // eat prey if touching
							else if (distanceCanTravel() > distanceTo(prey.get(i))) {
								chasing = (Prey) prey.get(i);
								break;
							}
						}
					}
				}
				if (chasing == null) {
					sprinting = false;
					if (acidBuildup > 0.9 * maxAcidBuildup)
						slowingDown = true;
					else if (!slowingDown
							&& this.moveTorward(gp, flagX, flagY, false))
						setRandomFlag(gp); // drift around
					if (slowingDown)
						slowingDown = acidBuildup > maxAcidBuildup / 10;
				}
			}
			if (chasing != null) {
				shootAt(gp, chasing);
				if (distanceCanTravel() > distanceTo(chasing))
					this.moveTorward(gp ,chasing.x, chasing.y, true);
				if (!canSee(chasing))
					chasing = null;
				else if (intersects(chasing)) {
					eat(gp, chasing);
					chasing = null;
				}
			}
		}
		update(gp, sprinting && canSprint());
		if (!alive)
			die(gp);
	}

	public void eat(GamePanel gp, Prey chasing) {
		if (chasing.alive) {
			if (!stunned) {
				devour((chasing.energy  + chasing.maxEnergy)/2);
				chasing.die(gp);
			}
		}
	}

	public void drawFirstPerson(Viewport vp, Graphics g) {
		vp.startPolygonSet();
		super.drawFirstPerson(vp, g);
		double legHeight = height * 0.2;
		double bodyHeight = height - legHeight;
		double hBodyWidth = width / 2;
		g.setColor(Color.BLACK);
		vp.fillVerticalCylindricalPolygon(g, x, y, legHeight, angle + Math.PI
				/ 4, hBodyWidth, 4, bodyHeight);
		double legWidth = width / 8;
		double hLeg = legWidth / 2;
		for (int i = 0; i < 2; i++) {
			double angle2 = (-Math.PI / 2) + angle + i * Math.PI;
			vp
					.fillVerticalCylindricalPolygon(g, Math.cos(angle2)
							* (hBodyWidth - hLeg) + x, Math.sin(angle2)
							* (hBodyWidth - hLeg) + y, 0, angle, legWidth, 4,
							legHeight);
		}
		double headSize = width / 2;
		double headCenterD = hBodyWidth + headSize;
		double headCenterX = Math.cos(angle) * headCenterD + x;
		double headCenterY = Math.sin(angle) * headCenterD + y;
		double headCenterZ = legHeight + bodyHeight + headSize / 2;
		// draws head
		g.setColor(Color.BLACK);
		vp.fillVerticalCylindricalPolygon(g, headCenterX, headCenterY, height
				- headSize, angle - Math.PI / 2, headSize, 5, headSize * 2);

		// draws tail
		double tailWidth = height * 0.1;
		double tailLength = width * 0.9;
		vp.fillHorizontalCylindricalPolygon(g, x, y, legHeight + tailWidth / 2,
				Math.PI / 3, angle + Math.PI, tailWidth / 2, 3, tailLength);
		g.setColor(Color.WHITE);
		// draws tip of tail
		vp.fillHorizontalPyramid(g, x + Math.cos(angle + Math.PI) * tailLength,
				y + Math.sin(angle + Math.PI) * tailLength, legHeight
						+ tailWidth / 2, Math.PI / 3, angle + Math.PI,
				tailWidth / 2, 3, width * 0.3, legHeight + tailWidth / 2);

		// draws whiskers
		for (int i = 0; i < 2; i++) {
			double dir = angle + Math.PI / 2 + Math.PI * i;
			vp
					.fillHorizontalPyramid(g, headCenterX + Math.cos(dir)
							* (width), headCenterY + Math.sin(dir) * (width),
							headCenterZ, Math.PI / 4, dir + Math.PI, headSize,
							4, width, headCenterZ);
		}
		vp.endPolygonSet();
	}

	public boolean canEat(Organism p) {
		return true;
	}

	public boolean shouldEat(Organism p) {
		return true;
	}

	public boolean tryCreateChild(GamePanel gp) {
		if (energy >= maxEnergy) {
			Predator child = new Predator(gp, this);
			loseGainEnergy(-child.energy);
			return true;
		}
		return false;
	}
}