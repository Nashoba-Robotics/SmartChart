package edu.nr;

import dashfx.lib.controls.Category;
import dashfx.lib.controls.Control;
import dashfx.lib.controls.Designable;
import dashfx.lib.data.DataCoreProvider;
import dashfx.lib.data.SmartValue;
import dashfx.lib.data.SupportedTypes;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.beans.binding.BooleanBinding;


import java.io.*;
import java.lang.StringBuilder;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Point2D;

import javax.swing.event.ChangeEvent;

@Designable(value="SmartChart", image = "/smartchart.png", description="Uses built-in graph and manual list storing. Includes a reset button (wow!)")
@SupportedTypes({dashfx.lib.data.SmartValueTypes.Number})
@Category("General")
public class SmartChart extends GenericSmartChart
{

    NRChart chart;

    public SmartChart()
    {
        super();
        chart = new NRChart(this);
        add(chart, 0, 0, 3, 1);

        Button resetButton = new Button("Reset Graph");
        resetButton.setOnAction(event -> chart.reset());
        add(resetButton, 0, 1, 3, 1);

        Button saveButton = new Button("Save Data");
        saveButton.setOnAction(event -> chart.save());
        add(saveButton, 0, 2, 3, 1);

        final Rectangle zoomRect = new Rectangle();
        zoomRect.setManaged(false);
        zoomRect.setFill(Color.LIGHTSEAGREEN.deriveColor(0, 1, 1, 0.5));
        getChildren().add(zoomRect);

        chart.setUpZooming(zoomRect);

        final Button zoomButton = new Button("Zoom");
        final Button resetZoomButton = new Button("Reset Zoom");
        zoomButton.setOnAction(event -> chart.doZoom(zoomRect));
        resetZoomButton.setOnAction(event -> {
            final NumberAxis xAxis = (NumberAxis)chart.getXAxis();
            xAxis.setLowerBound(chart.getLowestX());
            xAxis.setUpperBound(chart.getHighestX());
            final NumberAxis yAxis = (NumberAxis)chart.getYAxis();
            yAxis.setLowerBound(chart.getLowestY());
            yAxis.setUpperBound(chart.getHighestY());

            zoomRect.setWidth(0);
            zoomRect.setHeight(0);
        });

        final Button prepareToZoomButton = new Button("Prepare to zoom");

        prepareToZoomButton.setOnAction(event -> {
            if(chart.isAutoZooming()) {
                chart.setAutoZooming(false);
                prepareToZoomButton.setText("Finish zooming");
            } else {
                chart.setAutoZooming(true);
                prepareToZoomButton.setText("Prepare to zoom");
            }
        });

        final Button averageButton = new Button("Average");
        
        averageButton.setOnAction(event -> chart.getAverage(zoomRect));
        
        

        final BooleanBinding disableControls =
                zoomRect.widthProperty().lessThan(5)
                        .or(zoomRect.heightProperty().lessThan(5)).or(chart.isAutoZooming);
        zoomButton.disableProperty().bind(disableControls);
        resetZoomButton.disableProperty().bind(chart.isAutoZooming);


        add(zoomButton, 0, 3, 3, 1);

        add(resetZoomButton, 0, 4, 3, 1);

        add(prepareToZoomButton, 0, 5, 3, 1);
        
        add(averageButton, 0, 6, 3, 1);
    }

    @Override
    public void changed(ObservableValue<? extends Object> ov, Object old, Object t1)
    {
        SmartValue sv = (SmartValue)ov;
        double x = sv.getData().asNumber().doubleValue();

        chart.addValue(Double.valueOf(x));
    }
}

