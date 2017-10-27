package edu.nr;

import java.util.ArrayList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

/**
 * Created by garrison on 24-1-17.
 */
public abstract class GenericNRChart<T> extends LineChart<Number, Number> {

    public long startTimeMillis;

    public BooleanProperty isAutoZooming;

    GenericSmartChart chart;


    public GenericNRChart(GenericSmartChart chart) {
        super(new NumberAxis(), new NumberAxis());
        setAnimated(false);

        isAutoZooming = new SimpleBooleanProperty();
        setAutoZooming(true);

        ((NumberAxis)getXAxis()).setForceZeroInRange(false);
        ((NumberAxis)getYAxis()).setForceZeroInRange(false);

        setLegendVisible(false);

        this.chart = chart;



    }

    public void setAutoZooming(boolean val) {
        getXAxis().setAutoRanging(val);
        getYAxis().setAutoRanging(val);

        isAutoZooming.set(val);
    }

    public boolean isAutoZooming() {
        return isAutoZooming.get();
    }

    public abstract void save();

    public abstract void reset();

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

    public abstract double getLowestX();

    public abstract double getLowestY();

    public abstract double getHighestX();

    public abstract double getHighestY();
    
    public abstract ArrayList<Double> getAverage(Rectangle zoomRect);

}
