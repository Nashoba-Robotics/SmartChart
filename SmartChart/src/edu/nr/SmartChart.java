package edu.nr;

import dashfx.lib.controls.Category;
import dashfx.lib.controls.Control;
import dashfx.lib.controls.Designable;
import dashfx.lib.data.DataCoreProvider;
import dashfx.lib.data.SmartValue;
import dashfx.lib.data.SupportedTypes;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.io.*;
import java.lang.StringBuilder;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Designable(value="SmartChart", image = "/smartchart.png", description="Uses built-in graph and manual list storing. Includes a reset button (wow!)")
@SupportedTypes({dashfx.lib.data.SmartValueTypes.Number})
@Category("General")
public class SmartChart
        extends GridPane
        implements Control, ChangeListener<Object>
{
    StringProperty name = new SimpleStringProperty();

    @Designable(value="Name", description="The name the control binds to")
    public StringProperty nameProperty()
    {
        return this.name;
    }

    public String getName()
    {
        return this.name.getValue();
    }

    public void setName(String value)
    {
        this.name.setValue(value);
    }

    ChartImpl chartImpl;

    public SmartChart()
    {
        setAlignment(Pos.CENTER);

        chartImpl = new ChartImpl(this);
        add(chartImpl, 0, 0, 3, 1);

        Button resetButton = new Button("Reset Graph");
        resetButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                chartImpl.reset();
            }
        });
        add(resetButton, 0, 1, 3, 1);

        Button saveButton = new Button("Save Data");
        saveButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                chartImpl.save();
            }
        });
        add(saveButton, 0, 2, 3, 1);

    }

    public void registered(final DataCoreProvider provider)
    {
        if (getName() != null) {
            provider.getObservable(getName()).addListener(this);
        }
        this.name.addListener(new ChangeListener<String>()
        {
            public void changed(ObservableValue<? extends String> ov, String t, String t1)
            {
                if (t != null) {
                    provider.getObservable(t).removeListener(SmartChart.this);
                }
                provider.getObservable(t1).addListener(SmartChart.this);
            }
        });
    }

    public void changed(ObservableValue<? extends Object> ov, Object old, Object t1)
    {
        SmartValue sv = (SmartValue)ov;
        double x = sv.getData().asNumber().doubleValue();

        chartImpl.addValue(Double.valueOf(x));
    }

    public Node getUi()
    {
        return this;
    }
}

class ChartImpl extends LineChart<Number, Number>
{
    private Series series = new Series();
    private long startTimeMillis;

    SmartChart chart;

    public ChartImpl(SmartChart chart)
    {
        super(new NumberAxis(), new NumberAxis());
        setAnimated(false);
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
            Data<Double,Long> y = (Data<Double, Long>) x;
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


}

