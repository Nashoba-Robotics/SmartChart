package edu.nr;

import dashfx.lib.controls.Control;
import dashfx.lib.controls.Designable;
import dashfx.lib.data.DataCoreProvider;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Created by garrison on 24-1-17.
 */
public abstract class GenericSmartChart extends GridPane
        implements Control, ChangeListener<Object> {

    StringProperty name = new SimpleStringProperty();

    @Designable(value="Name", description="The name the control binds to")
    public final StringProperty nameProperty()
    {
        return this.name;
    }

    public final String getName()
    {
        return this.name.getValue();
    }

    public final void setName(String value)
    {
        this.name.setValue(value);
    }


    public GenericSmartChart () {

        setAlignment(Pos.CENTER);


    }


    public final void registered(final DataCoreProvider provider)
    {
        if (getName() != null) {
            provider.getObservable(getName()).addListener(this);
        }
        this.name.addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                if (t != null) {
                    provider.getObservable(t).removeListener(GenericSmartChart.this);
                }
                provider.getObservable(t1).addListener(GenericSmartChart.this);
            }
        });
    }

    public final Node getUi()
    {
        return this;
    }

    public abstract void changed(ObservableValue<? extends Object> ov, Object old, Object t1);

}
