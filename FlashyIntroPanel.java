import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

class FlashyIntroPanel extends FlashyPanel {
	int phase;
	double fontSize;
	double widthToDraw = 0.1;
	double angle;
	int time;
	int maxTime = 90;
	static String message;

	public FlashyIntroPanel() {
		super(1, 500, 90, 8.6, 100);
		setBackground(Color.BLACK);
		message = "Ben Chang";
		setPreferredSize(new Dimension(0, 350));
		new Thread(this).start();
	}

	boolean running;
	double slowDownSpeed;

	public void run() {
		if (running)
			return;
		reset(1, 500, 90, 8.6, 100);
		running = true;
		resetParticles();
		phase = 0;
		angle = 0;
		time = 0;
		fontSize = 200;
		while (widthToDraw < 0.9) {
			widthToDraw += 0.02;
			angle += 0.8;
			sleep(40);
		}
		phase = 2;
		slowDownSpeed = 0.8;
		angle %= Math.PI * 2;
		while (angle < Math.PI * 2) {
			angle += slowDownSpeed;
			slowDownSpeed *= 0.9;
			if (Math.PI * 2 - angle < slowDownSpeed)
				angle = Math.PI * 2;
			sleep(40);
		}
		for (time = 0; time < maxTime; time++) {
			createParticlesAt(0, 0.5 * getWidth(), 0.5 * getHeight(), 0);
			particlesMade += particlesPerUpdate;
			super.update();
			sleep(40);
			phase = 2;
		}
		requestFocus();
		if (running && focused) {
			phase = 3;
			reset(10, 100, 10, 7.6, 200);
			focusX = new double[particles.length];
			focusY = new double[focusX.length];
			randomPopsIndex = 0;
			timePerChangePops = maxTimePerChangePops;
			do
				randomPops();
			while (focused);
			running = false;
		}
	}

	public void focusGained(FocusEvent e) {
		focused = true;
		if (!running)
			new Thread(this).start();
	}

	public void focusLost(FocusEvent e) {
		super.focusLost(e);
		running = false;
	}

	int randomPopsIndex;
	int randomPopsMade;
	int timePerChangePops;
	int maxTimePerChangePops = 10;
	double[] focusX;
	double[] focusY;

	private void randomPops() {
		timePerChangePops++;
		if (timePerChangePops > maxTimePerChangePops) {
			timePerChangePops = 0;
			randomPopsIndex++;
			if (randomPopsIndex >= particles.length)
				randomPopsIndex = 0;
			focusX[randomPopsIndex] = Math.random() * getWidth();
			focusY[randomPopsIndex] = Math.random() * getHeight();
			randomPopsMade++;
		}
		for (int i = 0; i < particles.length && i < randomPopsMade; i++) {
			boolean toSkip = true;
			if (particles[i] != null) {// if shot all
				for (int b = 0; b < particles[i].length; b++) {
					if (particles[i][b] == null) {
						toSkip = false;
						break;
					}
					particles[i][b].velocityY += 0.3;
					if (particles[i][b].time > maxTime)
						toSkip = false;
				}
			} else
				toSkip = false;
			if (!toSkip)
				createParticlesAt(i, focusX[i], focusY[i], 0);
		}
		particlesMade += particlesPerUpdate;
		if (particlesMade > particles[0].length)
			particlesMade = 0;
		super.update();
		sleep(40);
	}

	private void sleep(int milli) {
		repaint();
		try {
			Thread.sleep(milli);
		} catch (InterruptedException e) {
		}

	}

	public static final String title1 = "evo";
	public static final String title2 = "LUTION";
	public static int stringWidth1;
	public static int stringWidth2;
	public static Font small;
	public static Font large;
	static FontMetrics fmSmall;
	static FontMetrics fmLarge;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(new Color(255, 18, 32, (int) (255 * (Math.max((float) time
				/ maxTime, 0.1f)))));
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform original = Organism.rotate(g2d, angle,
				getWidth() / 2.0, getHeight() / 2.0);
		drawEvolutionText(this, widthToDraw * getWidth(), g, phase == 3,
				fontSize, new Color(115, 13, 23), 25, true);
		g2d.setTransform(original);
		if (phase > 0)
			super.paintComponent(false, g);
	}

	public static void drawEvolutionText(JPanel j, double width, Graphics g1,
			boolean b, double size, Color textColor, int size2, boolean centered) {
		large = new Font("serif", Font.PLAIN, (int) (size));
		g1.setFont(large);
		fmLarge = g1.getFontMetrics();
		stringWidth2 = fmLarge.stringWidth(title2);
		stringWidth1 = (int) (stringWidth2 * 0.4);
		int ascent = fmLarge.getAscent() - fmLarge.getDescent();
		Image img = j.createImage(stringWidth1 + stringWidth2, ascent);
		Graphics g = img.getGraphics();
		g.setColor(j.getBackground());
		g.fillRect(0, 0, stringWidth1 + stringWidth2, ascent);
		g.setFont(small);
		g.drawImage(EvolutionFrame.evo, 0, 0, stringWidth1, ascent, j);
		g.setColor(textColor);
		g.setFont(large);
		g.drawString(title2, stringWidth1, ascent);
		int height = (int) (width * 0.2);
		int y = (j.getHeight() - height) / 2;
		if (!centered)
			y = 0;
		g1.drawImage(img, (int) (j.getWidth() - (width)) / 2, y, (int) width,
				height, j);
		if (b) {
			g1.setColor(textColor);
			g1.setFont(new Font("palatino linotype", Font.ITALIC, size2));
			FontMetrics fm = g1.getFontMetrics();
			g1.drawString(message,
					(j.getWidth() - fm.stringWidth(message)) / 2,
					y + fm.getHeight() + height);
		}
	}
}

class SinePanel extends FlashyPanel implements MouseListener,
		MouseMotionListener {
	boolean firstTime = true;
	double[] focusX;
	double[] focusY;
	double[] startFocusX;
	double[] startFocusY;
	double[] angle;
	double[] graphX;
	double[] graphY;
	double[] radius;

	public SinePanel() {
		super(2, 300, 5, 2, 50);
		setBasics();
	}

	public void setBasics() {
		addMouseListener(this);
		focusX = new double[particles.length];
		focusY = new double[particles.length];
		startFocusX = new double[particles.length];
		startFocusY = new double[particles.length];
		angle = new double[particles.length];
		graphX = new double[particles.length];
		graphY = new double[particles.length];
		radius = new double[particles.length];
		setNewFocus(0);
		setNewFocus(1);
		addMouseMotionListener(this);

	}

	public void setNewFocus(int index) {
		if (mouseDown) {
			angle[index] = Math.atan2(focusY[index] - mouse.getY(),
					focusX[index] - mouse.getX());
			radius[index] = Organism.length(mouse.getX() - focusX[index],
					mouse.getY() - focusY[index]);
			startFocusX[index] = mouse.getX();
			startFocusY[index] = mouse.getY();
		} else {
			if (Math.random() > 0.5) {
				angle[index] = Math.random() * Math.PI * 2;
				if (Math.random() > 0.5)
					focusX[index] = 0;
				else
					focusX[index] = getWidth();
				focusY[index] = Math.random() * getHeight();
			} else {
				if (Math.random() > 0.5)
					focusY[index] = 0;
				else
					focusY[index] = getHeight();
				focusX[index] = Math.random() * getWidth();
			}
			startFocusX[index] = focusX[index];
			startFocusY[index] = focusY[index];
		}
		graphX[index] = 0;
		graphY[index] = 0;
	}

	int updateSpeed = 5;

	public boolean outSide(int b) {
		return focusX[b] > getWidth() || focusY[b] > getHeight()
				|| focusX[b] < 0 || focusY[b] < 0;
	}

	public void update() {
		for (int b = 0; b < 2; b++) {
			if (mouseDown) {
				radius[b] -= 2;
				angle[b] += 0.05;
				focusX[b] = startFocusX[b] + Math.cos(angle[b]) * radius[b];
				focusY[b] = startFocusY[b] + Math.sin(angle[b]) * radius[b];
			} else {
				do {
					if (outSide(b))
						setNewFocus(b);
					graphX[b] += updateSpeed;
					graphY[b] = Math.sin(graphX[b] / 110.0) * (90) * 0.9;
					focusX[b] = Math.cos(angle[b]) * graphX[b] + startFocusX[b]
							+ Math.cos(angle[b] + Math.PI / 2) * graphY[b];
					focusY[b] = Math.sin(angle[b]) * graphX[b] + startFocusY[b]
							+ Math.sin(angle[b] + Math.PI / 2) * graphY[b];
				} while (outSide(b));
			}
			for (int i = particlesMade; i < 5 + particlesMade
					&& i < particles[b].length; i++) {
				int bright = 100;
				if (b == 1)
					bright = -100;
				particles[b][i] = new FireworksParticle(focusX[b], focusY[b],
						bright);
			}
		}
		particlesMade = Math.min(particlesMade + particlesPerUpdate,
				particles[0].length);
		super.update();
		repaint();
		if (particlesMade == particles[0].length)
			resetParticles();
	}

	public void resetParticles() {
		particlesMade = 0;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(true, g);
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
		if (firstTime)
			requestFocus();
		firstTime = false;
	}

	public void mouseExited(MouseEvent arg0) {
	}

	boolean mouseDown = false;
	MouseEvent mouse;

	public void mousePressed(MouseEvent arg0) {
		requestFocus();
		mouseDown = true;
		mouse = arg0;
		setNewFocus(0);
		setNewFocus(1);
	}

	public void mouseDragged(MouseEvent e) {
		mouse = e;
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent arg0) {
		mouseDown = false;
	}
}

class FlashyPanel extends JPanel implements Runnable, FocusListener,
		KeyListener {
	public FireworksParticle[][] particles;
	boolean focused = false;
	public static double fireworksSpeed = 2;
	public int particlesMade = 0;
	public int particlesPerUpdate = 2;
	public int maxTime;

	public FlashyPanel(int i1, int i2, int particlesPerUpdat,
			double worksSpeed, int fadeTime) {
		reset(i1, i2, particlesPerUpdat, worksSpeed, fadeTime);
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(0, 150));
		addFocusListener(this);
		addKeyListener(this);
	}

	public void reset(int i1, int i2, int particlesPerUpdat, double worksSpeed,
			int fadeTime) {
		particlesPerUpdate = particlesPerUpdat;
		maxTime = fadeTime;
		fireworksSpeed = worksSpeed;
		drawFireworks = true;
		particles = new FireworksParticle[i1][i2];
	}

	public void paintComponent(boolean clear, Graphics g) {
		if (clear)
			super.paintComponent(g);
		for (int b = 0; b < particles.length; b++)
			for (int i = 0; i < particles[b].length && particles[b][i] != null; i++)
				particles[b][i].draw(g);
	}

	public boolean drawFireworks;

	public void run() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
		do {
			update();
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
			}
		} while (focused && drawFireworks);
	}

	public void update() {
		for (int b = 0; b < particles.length; b++)
			for (int i = 0; i < particles[b].length && particles[b][i] != null; i++)
				particles[b][i].update();
	}

	public void createParticlesAt(int b, double x, double y, int brightness) {
		for (int i = particlesMade; i < particlesPerUpdate + particlesMade
				&& i < particles[0].length; i++)
			particles[b][i] = new FireworksParticle(x, y, brightness);
	}

	class FireworksParticle extends GameItem {
		double velocityX;
		public double velocityY;
		Color c1;
		Color c2;
		int time = 0;

		public FireworksParticle(double x1, double y1, int brightness) {
			x = x1;
			y = y1;
			width = 10;
			height = 10;
			double angle = Math.random() * Math.PI * 2;
			velocityX = Math.cos(angle) * fireworksSpeed;
			velocityY = Math.sin(angle) * fireworksSpeed;
			c2 = randomColor(brightness);
			c1 = randomColor(brightness);
		}

		public Color randomColor(int brightness) {
			return new Color(randomPart(brightness), randomPart(brightness),
					randomPart(brightness));
		}

		public int randomPart(int brightness) {
			return Math.max(
					Math.min((int) (Math.random() * (255 - brightness))
							+ brightness, 255), 0);
		}

		public void update() {
			time++;
			x += velocityX;
			y += velocityY;
		}

		public void draw(Graphics g) {
			float merge = (float) time / maxTime;
			g.setColor(Predator.mergeColors(c1, c2, merge, 1 - merge));
			g.fillOval((int) (x + (-width) / 2), (int) (y + (-height) / 2),
					(int) width, (int) height);
		}
	}

	public void resetParticles() {
		particlesMade = 0;
		particles = new FireworksParticle[particles.length][particles[0].length];
	}

	public void focusGained(FocusEvent e) {
		if (!focused) {
			focused = true;
			new Thread(this).start();
		}
	}

	public void focusLost(FocusEvent e) {
		focused = false;
	}

	public void keyPressed(KeyEvent arg0) {
	}

	public void keyReleased(KeyEvent arg0) {
	}

	public void keyTyped(KeyEvent arg0) {
	}
}