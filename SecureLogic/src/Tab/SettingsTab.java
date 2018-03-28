package Tab;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXButton.ButtonType;
import application.Util;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class SettingsTab extends SecureTab 
{
	private HashMap<String, String> icons = new HashMap<String, String>();
	private HashMap<String, JFXButton> buttons = new HashMap<String, JFXButton>();
	
	private boolean toggleScreenOff;
	private boolean screenOff;
	
	public SettingsTab() {
		try {
			Image lockIcon = new Image(getClass().getResource("/img/48x48/settings.png").openStream());
			setGraphic(new ImageView(lockIcon));
	    } catch (Exception e) {
	    	Util.logException(e);
	    }

		icons.put("nightMode", "/img/128x128/night-mode.png");
		icons.put("scheduleScreenOff", "/img/128x128/schedule-scree-off.png");
		icons.put("screenInstantOff", "/img/128x128/screen-off.png");
		icons.put("updateFirmware", "/img/128x128/update.png");
	    icons.put("exitProgram", "/img/128x128/exit.png");
		
	    initialize();
	}
	
	public void initialize() {
		GridPane grid = new GridPane();
	    grid.setHgap(30);
	    grid.setVgap(0);
	    grid.setMinHeight(-1);
	    grid.setMinWidth(720);
	    grid.setPrefHeight(-1);
	    grid.setPrefWidth(-1);
	    grid.setAlignment(Pos.CENTER);
        //grid.setStyle("-fx-background-color: white; -fx-grid-lines-visible: true");
		
	    ColumnConstraints col1 = new ColumnConstraints();
	    col1.setHalignment(HPos.LEFT);
	    col1.setHgrow(Priority.ALWAYS);
        col1.setPercentWidth(0);
        ColumnConstraints col2 = new ColumnConstraints();
	    col2.setHalignment(HPos.CENTER);
	    col2.setHgrow(Priority.ALWAYS);
        col2.setPercentWidth(33);    
        ColumnConstraints col3= new ColumnConstraints();
	    col3.setHalignment(HPos.CENTER);
	    col3.setHgrow(Priority.ALWAYS);
        col3.setPercentWidth(34);
        ColumnConstraints col4 = new ColumnConstraints();
	    col4.setHalignment(HPos.CENTER);
	    col4.setHgrow(Priority.ALWAYS);
        col4.setPercentWidth(33);
        ColumnConstraints col5 = new ColumnConstraints();
	    col5.setHalignment(HPos.LEFT);
	    col5.setHgrow(Priority.ALWAYS);
        col5.setPercentWidth(0);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5);
        
	    //Left spacing
	    Pane pane1 = new Pane();
	    pane1.minHeight(10);
	    grid.add(pane1, 0, 0, 1, 1);
	    GridPane.setVgrow(pane1, Priority.ALWAYS);
	    
	    int index = 0;
	    for (String key : icons.keySet()) {
	    	    	
	    	JFXButton button = null;
	    	try {
	    		button = new JFXButton("", new ImageView(new Image(getClass().getResource(icons.get(key)).openStream())));
	    		buttons.put(key, button);
	    	} catch (Exception e) {
	    		Util.logException(e);
	    		continue;
	    	}
	    	
	    	button.setMinSize(150, 150);
	    	button.setAlignment(Pos.CENTER);
	    	button.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30; -fx-background-color: #aabbcc;");
	    	button.setButtonType(ButtonType.RAISED);
	    	
	    	if (index < 3) {
	    		grid.add(button, index+1, 1, 1, 1);
	    	} else {
	    		grid.add(button, index-2, 3, 1, 1);
	    	}
	    	
	    	SettingsTab sT = this;
			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent e) {
					try {
						Class<?> base = Class.forName("Tab.SettingsTab");
						Method method = base.getMethod(key);
					    method.invoke(sT);
					} catch (Exception ex) {
						Util.logException(ex);
					}
				}
			});
	    
			index++;			
	    }
	    
	    //Middle spacing
	    Pane pane2 = new Pane();
	    pane2.minHeight(30);
	    grid.add(pane2, 0, 2, 1, 1);
	    GridPane.setVgrow(pane2, Priority.ALWAYS);
	    

	    //Middle spacing
	    Pane pane3 = new Pane();
	    pane3.minHeight(30);
	    grid.add(pane3, 0, 4, 1, 1);
	    GridPane.setVgrow(pane3, Priority.ALWAYS);
	    
	    setContent(grid);
	}
	
	public void nightMode() {
		
	}
	
	public void scheduleScreenOff() {
		toggleScreenOff = !toggleScreenOff;
	}
	
	public void screenInstantOff() {
		setScreenOff(true);
	}
	
	public void updateFirmware() {
		Util.updateFirmware();
	}
	
	public void exitProgram() {
		SecureTabPane.destruct();
        System.exit(0);
	}
	
	public boolean getToggleScreenOff() {
		return toggleScreenOff;
	}
	
	public boolean getScreenOff() {
		return screenOff;
	}
	
	public void setScreenOff(boolean screenOff) {
		if (screenOff) {
			Util.turnOffScreen();
		} else {
			Util.turnOnScreen();
		}
		this.screenOff = screenOff;
	}
}

