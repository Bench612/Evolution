import java.awt.*;

public class Autotroph extends GameItem {
	float energy;
	static float normalMaxEnergy = 50f;
	public float maxEnergy;
	float surfaceArea;
	public Color color;
	public boolean visible = false;
	double angle;

	public static int trunkSides = 4;
	public static int leafSides = 6;

	public void load(GamePanel gp) {
		gp.trunkX = new double[2][trunkSides];
		gp.trunkY = new double[2][trunkSides];
		gp.trunkZ = new double[2][trunkSides];
		for (int i = 0; i < gp.trunkX.length; i++)
			Viewport.getHorizontalBases(0, 0, i * height * 10, 0, width / 2,
					gp.trunkX[i], gp.trunkY[i], gp.trunkZ[i]);
		gp.leafX = new double[2][leafSides];
		gp.leafY = new double[2][leafSides];
		gp.leafZ = new double[2][leafSides];
		for (int i = 0; i < gp.leafX.length; i++)
			Viewport.getHorizontalBases(0, 0, i * height * 2 + height * 10, 0,
					width * 2, gp.leafX[i], gp.leafY[i], gp.leafZ[i]);
	}

	public Autotroph(GamePanel gp, double w, double h) {
		x = gp.randomPosition(); // the plants starts at a random posistion
		y = gp.randomPosition();
		width = w; // sets the width and height
		height = h;
		surfaceArea = (float) (width * height); // total contact with sun is set
		maxEnergy = (float) (normalMaxEnergy * surfaceArea); // setting energy
		energy = (float) (maxEnergy / 2);
		angle = Math.random() * Math.PI * 2;
		update(gp);
	}

	public double centerX() {
		return x + width / 2; // getsCenter
	}

	public double centerY() { // gets Center
		return y + height / 2;
	}

	public boolean contains(double x2, double y2) { // inside the square
		return x2 >= x && x2 <= x + width && y2 >= y && y2 <= y + height;
	}

	public double randomX() { // random point in plant
		return Math.random() * width + x;
	}

	public double randomY() {
		return Math.random() * height + y;
	}

	public void update(GamePanel gp) {
		// set its own color
		float rb = ((1 - (energy / maxEnergy)) * darkness) % 1;
		color = new Color(rb, darkness + 0.1f, rb, 1);
		energy = (float) Math.max(Math.min(energy
				+ (gp.autoEnergyGain * surfaceArea), maxEnergy), 0);

		// check if visible (for drawing)
		if (!visible)
			if (gp.ports != null)
				for (int i = 0; i < gp.ports.length; i++)
					if (isSeen(gp.ports[i])) {
						visible = true;
						break;
					}
	}

	public boolean isSeen(Viewport vp) {
		double hw = width / 2;
		double hh = height / 2;
		return (x + hw > -vp.xOffset && y + hh > -vp.yOffset
				&& x + vp.xOffset - hw < vp.width && y + vp.yOffset - hh < vp.height);
	}

	static float darkness = 0.6f;

	public void draw(Viewport vp, Graphics g) {
		if (isSeen(vp)) {
			g.setColor(color); // set color
			int[] xs = new int[leafSides];
			int[] ys = new int[leafSides];
			double cAngle = angle;
			double angleInc = (Math.PI * 2) / leafSides;
			double hW = width / 2.0;
			for (int i = 0; i < xs.length; i++) {
				xs[i] = (int) (Math.cos(cAngle) * hW + x + vp.xOffset);
				ys[i] = (int) (Math.sin(cAngle) * hW + y + vp.yOffset);
				cAngle += angleInc;
			}
			vp.fill2DPolygonTexture(g, xs, ys, 0.5, 0.5, leaves);
		}
	}

	public static Image leaves;
	public static Image wood;

	public void drawFirstPerson(Viewport vp, Graphics g) {
		if (vp.player.canSeeIgnoreDistance(this)) {
			vp.startPolygonSet();
			if (vp.gp.quality) {
				vp.fillCylindricalPolygon(g, x, y, 0, vp.gp.leafX, vp.gp.leafY,
						vp.gp.leafZ, leaves);
				vp.fillCylindricalPolygon(g, x, y, 0, vp.gp.trunkX,
						vp.gp.trunkY, vp.gp.trunkZ, wood);
			} else {
				g.setColor(color); // set color
				vp.fillCylindricalPolygon(g, x, y, 0, vp.gp.leafX, vp.gp.leafY,
						vp.gp.leafZ);
				g.setColor(vp.gp.terrain.darker());
				vp.fillCylindricalPolygon(g, x, y, 0, vp.gp.trunkX,
						vp.gp.trunkY, vp.gp.trunkZ);
			}

			vp.endPolygonSet();
		}
	}
}