package edu.nr;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by garrison on 24-1-17.
 */
class NRChartDouble extends GenericNRChart {
	private ArrayList<Series<Double, Double>> series = new ArrayList<>();

	private boolean firstTime = true;

	public NRChartDouble(SmartChartDouble chart) {
		super(chart);
	}

	int countNumDelimiter(String str, char delimiter) {
		return str.length() - str.replace(String.valueOf(delimiter), "").length();
	}

	public void addValue(String str) {
		ArrayList<Double> inputs = new ArrayList<>();

		int numDelimiters = countNumDelimiter(str, ':');

		if (firstTime) {
			for (int i = 0; i < numDelimiters + 1; i++)
				series.add(new Series<>());

			for (Series<Double, Double> s : series) {
				getData().add(s);
			}

			firstTime = false;
		}

		System.err.println("String input: " + str);
		System.err.println("Num delimiters: " + numDelimiters);
		int prevIndex = -1;
		for (int i = 0; i < numDelimiters; i++) {
			if (prevIndex + 1 == str.length()) {
				System.err.println(
						"Something's screwy with num delimiters, it says there are fewer than were found originally");
			}
			int currentIndex = str.indexOf(':', prevIndex + 1);
			System.err.println("Current index: " + currentIndex);
			System.err.flush();
			inputs.add(Double.parseDouble(str.substring(prevIndex + 1, currentIndex)));
			prevIndex = currentIndex;
		}
		inputs.add(Double.parseDouble(str.substring(prevIndex + 1)));

		inputs.forEach(y -> {
			System.err.println("Val: " + y);
		});
		System.err.flush();

		final boolean[] startFlag = { true };

		series.forEach(z -> {
			if (z.getData().size() != 0)
				startFlag[0] = false;
		});

		if (startFlag[0]) {
			startTimeMillis = System.currentTimeMillis();
		}

		double currentTime = (System.currentTimeMillis() - startTimeMillis) / 1000d;

		for (int i = 0; i < inputs.size(); i++) {
			series.get(i).getData().add(new Data(currentTime, inputs.get(i)));
		}

		series.forEach(s -> {
			if (s.getData().size() > 500)
				s.getData().remove(0);
		});

	}

	@Override
	public void reset() {
		series.forEach(s -> {
			s.getData().clear();
		});
	}

	@Override
	public double getLowestX() {

		boolean areAllSeriesEmpty = true;

		double lowestValue = Double.MAX_VALUE;

		for (Series<Double, Double> s : this.series) {
			if (s.getData().size() != 0) {
				areAllSeriesEmpty = false;

				double lowValue = s.getData().get(0).getXValue();

				if (lowValue < lowestValue) {
					lowestValue = lowValue;
				}

			}
		}

		if (areAllSeriesEmpty) {
			return 0;
		}
		return lowestValue;
	}

	@Override
	public double getLowestY() {

		boolean areAllSeriesEmpty = true;

		double lowestValue = Double.MAX_VALUE;

		for (Series<Double, Double> s : this.series) {
			if (s.getData().size() != 0) {
				areAllSeriesEmpty = false;

				double lowValue = s.getData().get(0).getYValue();
				for (int i = 0; i < s.getData().size(); i++) {
					if (s.getData().get(i).getYValue() < lowValue) {
						lowValue = s.getData().get(i).getYValue();
					}
				}

				if (lowValue < lowestValue) {
					lowestValue = lowValue;
				}

			}
		}

		if (areAllSeriesEmpty) {
			return 0;
		}
		return lowestValue;
	}

	@Override
	public double getHighestX() {

		boolean areAllSeriesEmpty = true;

		double highestValue = Double.MIN_VALUE;

		for (Series<Double, Double> s : this.series) {
			if (s.getData().size() != 0) {
				areAllSeriesEmpty = false;

				double highValue = s.getData().get(s.getData().size() - 1).getXValue();

				if (highValue > highestValue) {
					highestValue = highValue;
				}

			}
		}

		if (areAllSeriesEmpty) {
			return 110;
		}
		return highestValue;
	}

	@Override
	public double getHighestY() {

		boolean areAllSeriesEmpty = true;

		double highestValue = Double.MIN_VALUE;

		for (Series<Double, Double> s : this.series) {
			if (s.getData().size() != 0) {
				areAllSeriesEmpty = false;

				double highValue = s.getData().get(0).getYValue();
				for (int i = 0; i < s.getData().size(); i++) {
					if (s.getData().get(i).getYValue() > highValue) {
						highValue = s.getData().get(i).getYValue();
					}
				}

				if (highValue > highestValue) {
					highestValue = highValue;
				}

			}
		}

		if (areAllSeriesEmpty) {
			return 110;
		}
		return highestValue;

	}

	@Override
	public void save() {
		StringBuilder sb = new StringBuilder();
		series.forEach(s -> {
			for (Data<Double, Double> x : s.getData()) {
				sb.append(x.getXValue());
				sb.append(",");
				sb.append(x.getYValue());
				sb.append(",,");
			}
			sb.append('\n');
		});

		String fileName = System.getProperty("user.home") + "\\" + this.chart.getName().replace(' ', '_') + ".csv";

		try {
			// Create the empty file with default permissions, etc.
			Files.createFile(Paths.get(fileName));
		} catch (FileAlreadyExistsException x) {
			System.err.format("file named %s" + " already exists%n", fileName);
		} catch (IOException x) {
			// Some other sort of failure, such as permissions.
			System.err.format("createFile error: %s%n", x);
		}

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"))) {
			writer.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList getAverage(Rectangle zoomRect) {
		boolean areAllSeriesEmpty = true;
		ArrayList<Double> finalList = new ArrayList<Double>();
		for (Series<Double, Double> s : this.series) {
			if (s.getData().size() != 0) {
				areAllSeriesEmpty = false;
				
				ArrayList<Data<Double, Double>> goodPoints = new ArrayList<Data<Double, Double>>();
				for (int i = 0; i < s.getData().size(); i++) {
					if (((Data<Double, Double>) s.getData().get(i)).getXValue() > zoomRect.getX()
							&& ((Data<Double, Double>) s.getData().get(i)).getXValue() < zoomRect.getX()
									+ zoomRect.getWidth()
							&& ((Data<Double, Double>) s.getData().get(i)).getYValue() < zoomRect.getY()
							&& ((Data<Double, Double>) s.getData().get(i)).getYValue() > zoomRect.getY()
									- zoomRect.getHeight()) {
						goodPoints.add((Data<Double, Double>) s.getData().get(i));
					}
				}
				double areaSum = 0;
				double yAvg;
				double dx;
				for(int i = 0; i < goodPoints.size() - 1; i++) {
					yAvg = (((Data<Double, Double>) s.getData().get(i)).getYValue()
							+ ((Data<Double, Double>) s.getData().get(i + 1)).getYValue()) / 2;
					dx = ((Data<Double, Double>) s.getData().get(i + 1)).getXValue()
							- ((Data<Double, Double>) s.getData().get(i)).getXValue();
					areaSum += yAvg * dx;
				}
				double dxTotal = ((Data<Double, Double>) s.getData().get(goodPoints.size() - 1)).getXValue()
						- ((Data<Double, Double>) s.getData().get(0)).getXValue();
				finalList.add(areaSum / dxTotal);
			}
		}

		if (areAllSeriesEmpty) {
			finalList.add(110.0);
		}
		return finalList;
	}

}
