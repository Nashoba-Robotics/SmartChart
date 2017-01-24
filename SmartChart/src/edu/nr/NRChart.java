package edu.nr;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by Nashoba1768 on 1/22/2017.
 */
class NRChart extends LineChart<Number, Number>
{
    private Series series = new Series();
    private long startTimeMillis;

    SmartChart chart;

    public BooleanProperty isAutoZooming;

    public NRChart(SmartChart chart)
    {
        super(new NumberAxis(), new NumberAxis());
        setAnimated(false);

        isAutoZooming = new SimpleBooleanProperty();
        setAutoZooming(true);

        ((NumberAxis)getXAxis()).setForceZeroInRange(false);
        ((NumberAxis)getYAxis()).setForceZeroInRange(false);
        setLegendVisible(false);
        getData().add(this.series);
        this.chart = chart;
    }

    public void addValue(double x)
    {
        if(this.series.getData().size() == 0)
        {
            startTimeMillis = System.currentTimeMillis();
        }

        double currentTime = (System.currentTimeMillis() - startTimeMillis)/1000d;


        this.series.getData().add(new Data(currentTime, x));
        if (this.series.getData().size() > 10000) {
            this.series.getData().remove(0);
        }
    }

    public void reset()
    {
        this.series.getData().clear();
    }

    public void save()
    {
        StringBuilder sb = new StringBuilder();
        for(Object x:this.series.getData()) {
            Data<Double,Double> y = (Data<Double, Double>) x;
            sb.append(y.getXValue());
            sb.append(",");
            sb.append(y.getYValue());
            sb.append('\n');
        }

        String fileName = System.getProperty("user.home") + "\\" + this.chart.getName().replace(' ', '_') + ".csv";

        try {
            // Create the empty file with default permissions, etc.
            Files.createFile(Paths.get(fileName));
        } catch (FileAlreadyExistsException x) {
            System.err.format("file named %s" +
                    " already exists%n", fileName);
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

    protected void setUpZooming(final Rectangle rect) {
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
        this.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                rect.setWidth(0);
                rect.setHeight(0);
            }
        });
        this.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                rect.setX(Math.min(x, mouseAnchor.get().getX()));
                rect.setY(Math.min(y, mouseAnchor.get().getY()));
                rect.setWidth(Math.abs(x - mouseAnchor.get().getX()));
                rect.setHeight(Math.abs(y - mouseAnchor.get().getY()));
            }
        });
    }

    public void doZoom(Rectangle zoomRect) {
        final NumberAxis yAxis = (NumberAxis) getYAxis();
        final NumberAxis xAxis = (NumberAxis) getXAxis();
        //Point2D yAxisInScene = yAxis.localToScene(0, 0);
        //Point2D xAxisInScene = xAxis.localToScene(0, 0);
        double xtopLeftCornerOfGraph = 52;
        double ytopLeftCornerOfGraph = 17;

        double x = zoomRect.getX() - xtopLeftCornerOfGraph;
        double y = zoomRect.getY() - ytopLeftCornerOfGraph;
        double w = zoomRect.getWidth();
        double h = zoomRect.getHeight();

        double width_pixels = this.getWidth() - 65; //65 is the number of pixels of padding
        double height_pixels = this.getHeight() - 65;

        double width_value = xAxis.getUpperBound() - xAxis.getLowerBound();
        double height_value = yAxis.getUpperBound() - yAxis.getLowerBound();

        //Point2D zoomTopLeft = new Point2D(zoomRect.getX(), zoomRect.getY());
        //Point2D zoomBottomRight = new Point2D(zoomRect.getX() + zoomRect.getWidth(), zoomRect.getY() + zoomRect.getHeight());
        //double xOffset = zoomTopLeft.getX() - yAxisInScene.getX();
        //double yOffset = zoomBottomRight.getY() - xAxisInScene.getY();
        double xAxisScale = width_pixels/width_value;
        double yAxisScale = height_pixels/height_value;

        double xLower = xAxis.getLowerBound();
        double yUpper = yAxis.getUpperBound();

        xAxis.setLowerBound(xLower + x / xAxisScale);
        xAxis.setUpperBound(xLower + (x+w)/ xAxisScale);
        yAxis.setUpperBound(yUpper - y / yAxisScale);
        yAxis.setLowerBound(yUpper - (y+h) / yAxisScale);
        zoomRect.setWidth(0);
        zoomRect.setHeight(0);
    }

    public double getLowestX() {
        if(this.series.getData().size() == 0) {
            return 0;
        }

        return ((Data<Double,Double>) this.series.getData().get(0)).getXValue();
    }

    public double getLowestY() {
        if(this.series.getData().size() == 0) {
            return 0;
        }
        double minValue = ((Data<Double,Double>) this.series.getData().get(0)).getYValue();
        for(int i=0; i<this.series.getData().size(); i++){
            if(((Data<Double,Double>) this.series.getData().get(i)).getYValue() < minValue) {
                minValue = ((Data<Double, Double>) this.series.getData().get(i)).getYValue();
            }
        }
        return minValue;
    }

    public double getHighestX() {
        if(this.series.getData().size() == 0) {
            return 110;
        }
        return ((Data<Double,Double>) this.series.getData().get(this.series.getData().size()-1)).getXValue();
    }

    public double getHighestY() {
        if(this.series.getData().size() == 0) {
            return 110;
        }
        double maxValue = ((Data<Double,Double>) this.series.getData().get(0)).getYValue();
        for(int i=0; i<this.series.getData().size(); i++){
            if(((Data<Double,Double>) this.series.getData().get(i)).getYValue() > maxValue){
                maxValue = ((Data<Double,Double>) this.series.getData().get(i)).getYValue();
            }
        }
        return maxValue;
    }

    public void setAutoZooming(boolean val) {
        getXAxis().setAutoRanging(val);
        getYAxis().setAutoRanging(val);

        isAutoZooming.set(val);
    }

    public boolean isAutoZooming() {
        return isAutoZooming.get();
    }

}
