package Tab;

import java.util.ArrayList;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;

import application.RGBStage;
import application.Util;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import zwave.fibaro.HC2Interface;
import zwave.fibaro.RGBState;
import zwave.fibaro.ZWaveDevice;

public class ZWaveDeviceTab extends SecureTab 
{
	protected ArrayList<ZWaveDevice> devices = new ArrayList<ZWaveDevice>();
	private JFXButton[] buttons;
	
	protected Image deviceOn;
	protected Image deviceOff;
	
	public ZWaveDeviceTab() {

	}
	
	protected void initialize() {
		buttons  = new JFXButton[devices.size()];
		
		GridPane grid = new GridPane();
	    grid.setHgap(10);
	    grid.setVgap(30);
	    grid.setMinHeight(-1);
	    grid.setMinWidth(720);
	    grid.setPrefHeight(-1);
	    grid.setPrefWidth(-1);
	    grid.setAlignment(Pos.CENTER);
        //grid.setStyle("-fx-background-color: white; -fx-grid-lines-visible: true");
	    
	    ColumnConstraints col1 = new ColumnConstraints();
	    col1.setHalignment(HPos.CENTER);
	    col1.setHgrow(Priority.ALWAYS);
        col1.setPercentWidth(5);
        ColumnConstraints col2 = new ColumnConstraints();
	    col2.setHalignment(HPos.CENTER);
	    col2.setHgrow(Priority.ALWAYS);
        col2.setPercentWidth(40);    
        ColumnConstraints col3= new ColumnConstraints();
	    col3.setHalignment(HPos.CENTER);
	    col3.setHgrow(Priority.ALWAYS);
        col3.setPercentWidth(0);
        ColumnConstraints col4 = new ColumnConstraints();
	    col4.setHalignment(HPos.CENTER);
	    col4.setHgrow(Priority.ALWAYS);
        col4.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);
        
	    //Left spacing
	    Pane pane1 = new Pane();
	    pane1.minHeight(10);
	    grid.add(pane1, 0, 0, 1, 1);
	    GridPane.setVgrow(pane1, Priority.ALWAYS);

	    JFXButton buggButton = new JFXButton(" ", new ImageView(deviceOff));
	    grid.add(buggButton, 0, 1, 1, 1);
	    buggButton.setVisible(false);
	    
	    for(int i = 0; i < devices.size(); i++) 
	    {
	    	ZWaveDevice zDevice = devices.get(i);
	    	if (zDevice == null) 
	    	{
	    		continue;
	    	}
	    	
	    	buttons[i] = new JFXButton(zDevice.getName(), new ImageView(deviceOff));
	    	buttons[i].setMinWidth(300);
	    	buttons[i].setAlignment(Pos.BASELINE_LEFT);
	    	buttons[i].setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30; -fx-background-color: #aabbcc;");
	    	if (i < 4) {
	    		grid.add(buttons[i], 1, 1 + i, 1, 1);
	    	} else {
	    		grid.add(buttons[i], 3, i - 3, 1, 1);
	    	}
	    	
	    	switch (zDevice.getDeviceType()) 
	    	{
	    		case RGBDimmable : 
	    			dimOrSwitch(buttons[i], zDevice);	    			
	    			break;
	    		case OnOff : 
	    			buttons[i].setOnAction(new EventHandler<ActionEvent>() {
	    				@Override public void handle(ActionEvent e) {
	    					for(int deviceId : zDevice.getDeviceIds()) {
		    					try {
		    						HC2Interface.setLightDeviceStatus(deviceId, !zDevice.getState()?1:0);
		    					} catch (Exception ex) {
		    						Util.logException(ex);
		    					}
	    					}
	    				}
	    			});
	    			break;
	    		case Door : break;
	    	}
	    }
	    
	    //Middle spacing
	    Pane pane2 = new Pane();
	    pane2.minHeight(40);
	    grid.add(pane2, 2, 0, 1, 1);
	    GridPane.setVgrow(pane2, Priority.ALWAYS);
	    	    
	    //Middle spacing
	    Pane pane3 = new Pane();
	    pane3.minHeight(40);
	    grid.add(pane3, 0, 5, 4, 1);
	    GridPane.setVgrow(pane3, Priority.ALWAYS);

	    setContent(grid);
	}
	
	public void UpdateStates() throws Exception {
		
		for(int i = 0; i < devices.size(); i++) 
		{
			ZWaveDevice zDevice = devices.get(i);
			
			boolean target = false;
			switch (zDevice.getDeviceType()) {
				case RGBDimmable:
					target = zwave.fibaro.HC2Interface.getRGBDeviceStatus(zDevice.getDeviceIds()[0]).getLevel() != 0;
					break;
				case OnOff:
					target = zwave.fibaro.HC2Interface.getLightDeviceStatus(zDevice.getDeviceIds()[0]);
					break;
				default: continue;
			}
			
			if (zDevice.getState() || !Boolean.valueOf(target).equals(zDevice.getState())) {
				zDevice.setState(target);
				if (target) {
					buttons[i].setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30; -fx-background-color: #009688;");
					buttons[i].setGraphic(new ImageView(deviceOn));
				} else {
					buttons[i].setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30; -fx-background-color: #aabbcc;");
					buttons[i].setGraphic(new ImageView(deviceOff));
				}
			}
		}
	}
}
