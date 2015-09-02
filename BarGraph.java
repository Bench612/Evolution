import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;

public class BarGraph extends JPanel implements ActionListener, ChangeListener {
	Bar graph;
	int predPreyIndex;
	int timeIndex;
	JSlider time;
	public JLabel stats;

	public BarGraph() {
		stats = new JLabel("", JLabel.CENTER); //has a label
	}

	public void actionPerformed(ActionEvent e) {
		getData();//retrieve data when data is changed
	}

	public void stateChanged(ChangeEvent e) {
		timeIndex = time.getValue() - graph.gp.timesShifted;
		updateData();
	}
	
	public double getOverallMax(double[][] data, int maxTimeIndex){
		double max = data[0][0];
		for (int i = 0; i < maxTimeIndex; i++){
			for (int j = 0; j < data[i].length; j++)
				max = Math.max(max, data[i][j]);
		}
		return max;
	}
	
	public double getOverallMin(double[][] data, int maxTimeIndex){
		double min = data[0][0];
		for (int i = 0; i < maxTimeIndex; i++){
			for (int j = 0; j < data[i].length; j++)
				min = Math.min(min, data[i][j]);
		}
		return min;
	}
	
	public void updateData(){
		double[] data;
		graph.xLabel.setText(graph.trait.getSelectedItem() + "");
		switch (graph.trait.getSelectedIndex()) {
		case 0:
			 data = graph.gp.sizeData[predPreyIndex][timeIndex];
			 break;
		case 1:
			data = graph.gp.speedData[predPreyIndex][timeIndex];
			break;
		case 2:
			data = graph.gp.sprintData[predPreyIndex][timeIndex];
			break;
		default:
			data = graph.gp.eyeData[predPreyIndex][timeIndex];
			break;
		}
		graph.updateData(this, data);
	}

	public void getData() { //get the data based off combo box
		double[] data;
		double minX;
		double maxX;
		graph.xLabel.setText(graph.trait.getSelectedItem() + "");
		switch (graph.trait.getSelectedIndex()) {
		case 0:
			 data = graph.gp.sizeData[predPreyIndex][timeIndex];
			 minX = getOverallMin(graph.gp.sizeData[predPreyIndex],graph.gp.maxIndex + 1);
			 maxX = getOverallMax(graph.gp.sizeData[predPreyIndex],graph.gp.maxIndex + 1);
			 break;
		case 1:
			data = graph.gp.speedData[predPreyIndex][timeIndex];
			 minX = getOverallMin(graph.gp.speedData[predPreyIndex],graph.gp.maxIndex + 1);
			 maxX = getOverallMax(graph.gp.speedData[predPreyIndex],graph.gp.maxIndex + 1);
			break;
		case 2:
			data = graph.gp.sprintData[predPreyIndex][timeIndex];
			 minX = getOverallMin(graph.gp.sprintData[predPreyIndex],graph.gp.maxIndex + 1);
			 maxX = getOverallMax(graph.gp.sprintData[predPreyIndex],graph.gp.maxIndex + 1);
			break;
		default:
			data = graph.gp.eyeData[predPreyIndex][timeIndex];
			 minX = getOverallMin(graph.gp.eyeData[predPreyIndex],graph.gp.maxIndex + 1);
			 maxX = getOverallMax(graph.gp.eyeData[predPreyIndex],graph.gp.maxIndex + 1);
			break;
		}
		graph.getData(this, data, minX, maxX);
	}

	public void load(GamePanel gp, int index) {
		predPreyIndex = index;

		removeAll(); //empty all

		graph = new Bar(gp);
		graph.setToolTipText("The Current Bell Curve");

		setLayout(new BorderLayout());

		//a JSlider
		JPanel east = new JPanel(new BorderLayout());
		east.add(graph.trait, BorderLayout.NORTH);

		// a JSlider
		time = new JSlider(JSlider.VERTICAL, gp.timesShifted, gp.timesShifted + gp.maxIndex, gp.timesShifted + gp.maxIndex);
		timeIndex = gp.maxIndex;
		time.setToolTipText("Adjusts the time that the bar graph shows.");
		time.setPaintTicks(true);
		time.setMinorTickSpacing(1);
		time.setMajorTickSpacing(5);
		time.setSnapToTicks(true);
		time.setLabelTable(time.createStandardLabels(5));
		time.setPaintLabels(true);
		east.add(new SlideLabel("Time", time, this), BorderLayout.CENTER);
		
                
		add(east, BorderLayout.EAST);

		graph.trait.addActionListener(this);
		time.addChangeListener(this);
		getData();
		add(graph, BorderLayout.CENTER);
		JPanel north = new JPanel(new GridLayout(0, 1));
		north.add(graph.back);
		north.add(stats);
		add(north, BorderLayout.NORTH);
	}
}

class SlideLabel extends JPanel { // a JPanel width a slider and a label
	public SlideLabel(String s, JSlider slide, ChangeListener t) {
		slide.addChangeListener(t);
		setLayout(new BorderLayout());
		add(new JLabel(s, JLabel.CENTER), BorderLayout.NORTH);
		add(slide, BorderLayout.CENTER);
	}
}

class Bar extends Graph {
	double data[];
	public int numTicks = 10;
	double minX; //for the current time
	double maxX; //for the current time
	double totalMaxX;
	double totalMinX;

	public Bar(GamePanel g) {
		setBasics(g, false);
		yLabel.setText("Number with value");
	}

	public void getData(BarGraph bg, double[] from,double totMinX, double totMaxX) {
		totalMaxX = totMaxX;
		totalMinX = totMinX;
		numTicks = Math.min(from.length, 15);
		updateData(bg, from);
	}

	public void updateData(BarGraph bg, double[] from) {
		minX = getMin(from, from.length);
		maxX = getMax(from, from.length);
		numTicks = Math.min((int)Math.ceil(numTicks + ((totalMaxX - maxX)+(minX - totalMinX))/((maxX - minX)/numTicks)), 20);
		data = new double[numTicks];
		double inc = (totalMaxX - totalMinX) / numTicks;
		for (int i = 0; i < from.length; i++)
			data[(int) (Math.min(((from[i] - totalMinX) / inc), numTicks - 1))]++;

		double mean = GamePanel.getAverage(from); //get average
		double standardD = 0;
		for (int i = 0; i < from.length; i++)
			standardD += Math.pow((from[i] - mean), 2);
		standardD = Math.sqrt(standardD / (from.length - 1)); //calc standard D
		bg.stats.setText("Mean : " + String.format("%.3f",mean)
				+ "              Standard Deviation : " + String.format("%.3f",standardD)
				+ "              Variance : " + String.format("%.3f",Math.pow(standardD, 2))); //set Text
		repaint(); //redraw updated data
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g); //clear screen
		double minY = getMin(data, data.length);
		double maxY = getMax(data, data.length);
		drawBorders(g, totalMinX, totalMaxX, minY * 0.9, maxY * 1.1, numTicks); //draw borders
		double x = x0;
		for (int i = 0; i < data.length; i++) { //graph data
			int y = (int) screenY(data[i]);
			int height = (int) (y0 - screenY(data[i]));
			g.setColor(Color.RED);
			g.fillRect((int) x, y, (int) xTickInc, height);
			g.setColor(Color.WHITE);
			g.drawRect((int) x, y, (int) xTickInc, height);
			x += xTickInc;
		}
	}
}