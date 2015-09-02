import java.awt.*;
import javax.swing.*;

public class GameHUD extends JPanel {
	WestPanel west;
	GamePanel gp;
	public CardContainer cc;

	public GameHUD(CardContainer c, float variation, Color background,
			double mapWidth, double mapHeight, double plantSize,
			double plantPercent) {
		cc = c;
		setLayout(new BorderLayout());
		gp = new GamePanel(cc, variation, background, mapWidth, mapHeight,
				plantSize, plantPercent);
		add(gp.cardContainer, BorderLayout.CENTER);
		west = new WestPanel(gp);
		add(west, BorderLayout.WEST);
	}

	class WestPanel extends JPanel {
		public WestPanel(GamePanel gp) {
			setLayout(new BorderLayout());
			add(gp.stats, BorderLayout.NORTH);
			add(gp.miniMap);
			setPreferredSize(new Dimension(300, 500));
		}
	}
}