import java.awt.*;
public class GameItem{
public double x; //center
public double y;
public double width;
public double height;

	public double distanceTo(GameItem other) {
		return distanceTo(other.x, other.y); // distance formula to calculate
	}

	public double distanceTo(double x2, double y2) {
		return length(x - x2, y - y2);
	}

	public static double length(double x2, double y2) {
		return Math.sqrt(Math.pow(x2, 2) + Math.pow(y2, 2));
	}
	
	public boolean intersects(GameItem other){
		return distanceTo(other) < (width + other.width) / 2;
	}
	
	public void drawFirstPerson(Viewport vp, Graphics g){
	}
}