package Tab;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.HashMap;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXButton.ButtonType;
import com.jfoenix.controls.JFXRadioButton;

import application.Util;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import zwave.fibaro.HC2Interface;

public class SettingsTab extends SecureTab 
{
	private HashMap<String, String> icons = new HashMap<String, String>();
	private HashMap<String, JFXButton> buttons = new HashMap<String, JFXButton>();
	private boolean screenOff;
	
	public SettingsTab() {
		try {
			Image lockIcon = new Image(getClass().getResource("/img/48x48/settings.png").openStream());
			setGraphic(new ImageView(lockIcon));
	    } catch (Exception e) {
	    	Util.logException(e);
	    }

		icons.put("weatherSetting", "/img/128x128/weather-setting.png");
		icons.put("scheduleScreenOff", "/img/128x128/schedule-scree-off.png");
		icons.put("screenInstantOff", "/img/128x128/screen-off.png");
		icons.put("updateFirmware", "/img/128x128/update.png");
	    icons.put("exitProgram", "/img/128x128/exit.png");
	    icons.put("information", "/img/128x128/info-setting.png");
		
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
	
	public void weatherSetting() {
		try {
			Stage stage = createSettingsStage(getContent().getScene().getWindow(), "Weather Setting", "ForecastProvider", new String[] {"YR", "OpenWeatherMap"}, new String[] {"YR", "Open weather map"});
			disableScreenUpdate(true);
			stage.showAndWait();
			disableScreenUpdate(false);
		} catch (Exception e) {
			Util.logException(e);
		}
	}
	
	public void scheduleScreenOff() {
		try {
			Stage stage = createSettingsStage(getContent().getScene().getWindow(), "Turn off screen at night?", "TurnOffScreenAtNight", new String[] {"19-7", "20-7", "21-7", "22-7", "no"}, new String[] {"Off - 19 to 07", "Off - 20 to 07", "Off - 21 to 07", "Off - 22 to 07", "Do not turn off"});
			disableScreenUpdate(true);
			stage.showAndWait();
			disableScreenUpdate(false);
		} catch (Exception e) {
			Util.logException(e);
		}
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
	
	public void information() {
		try {
			Stage stage = createInformationStage(getContent().getScene().getWindow());
			disableScreenUpdate(true);
			stage.showAndWait();
			disableScreenUpdate(false);
		} catch (Exception e) {
			Util.logException(e);
		}
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
	
	private Stage createSettingsStage(Window owner, String captionString, String setting, String[] keys, String[] descriptions) throws Exception {
		String currentValue = Util.getSetting(setting);
		
		Stage stage = new Stage();
		stage.initOwner(owner);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initStyle(StageStyle.TRANSPARENT);

		GridPane root = new GridPane();
		root.setHgap(10);
		root.setVgap(10);
		root.setAlignment(Pos.CENTER);
		root.setStyle("-fx-background-color: derive(white, 25%) ; -fx-border-color: black;"
				+ "-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
		// root.setStyle(root.getStyle() + " -fx-grid-lines-visible: true");

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setHalignment(HPos.CENTER);
		col1.setHgrow(Priority.ALWAYS);
		col1.setPercentWidth(15);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHalignment(HPos.CENTER);
		col2.setHgrow(Priority.ALWAYS);
		col2.setPercentWidth(35);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setHalignment(HPos.CENTER);
		col3.setHgrow(Priority.ALWAYS);
		col3.setPercentWidth(35);
		ColumnConstraints col4 = new ColumnConstraints();
		col4.setHalignment(HPos.CENTER);
		col4.setHgrow(Priority.ALWAYS);
		col4.setPercentWidth(15);
		root.getColumnConstraints().addAll(col1, col2, col3, col4);

		// Caption
		Text caption = new Text(captionString);
		caption.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 40;");
		root.add(caption, 0, 0, 4, 1);
		
		final ToggleGroup group = new ToggleGroup();
        for (int i = 0; i < keys.length; i++) {
        	JFXRadioButton radio = new JFXRadioButton(descriptions[i]);
        	radio.setSelected(keys[i].equals(currentValue) || (i == 0 && currentValue == null));
        	radio.setUserData(keys[i]);
        	radio.setPadding(new Insets(10));
        	radio.setPrefSize(350, 40);
        	radio.setToggleGroup(group);
        	radio.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 20;");
			root.add(radio, 1, i+1, 2, 1);
		}

		JFXButton okButton = new JFXButton("Close");
		okButton.setPrefSize(350, 40);
		okButton.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 20; -fx-background-color: #aabbcc;");
		okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try {
					String selectedValue = (String)group.getSelectedToggle().getUserData();
					if (!selectedValue.equals(currentValue)) {
						Util.setSetting(setting, selectedValue);
					}
					stage.hide();
				} catch (Exception ex) {
					Util.logException(ex);
				}
			}
		});
		root.add(okButton, 0, keys.length + 1, 4, 1);
		final Scene scene = new Scene(root, 460, (keys.length+2)*60, Color.TRANSPARENT);
		enableDragging(scene);
		stage.setScene(scene);
		centerStage(stage, 460, (keys.length+2)*80);
		return stage;
	}
	
	private Stage createInformationStage(Window owner) throws Exception {		
		Stage stage = new Stage();
		stage.initOwner(owner);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initStyle(StageStyle.TRANSPARENT);

		GridPane root = new GridPane();
		root.setHgap(10);
		root.setVgap(10);
		root.setAlignment(Pos.CENTER);
		root.setStyle("-fx-background-color: derive(white, 25%) ; -fx-border-color: black;"
				+ "-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
		// root.setStyle(root.getStyle() + " -fx-grid-lines-visible: true");

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setHalignment(HPos.CENTER);
		col1.setHgrow(Priority.ALWAYS);
		col1.setPercentWidth(15);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHalignment(HPos.LEFT);
		col2.setHgrow(Priority.ALWAYS);
		col2.setPercentWidth(35);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setHalignment(HPos.LEFT);
		col3.setHgrow(Priority.ALWAYS);
		col3.setPercentWidth(35);
		ColumnConstraints col4 = new ColumnConstraints();
		col4.setHalignment(HPos.CENTER);
		col4.setHgrow(Priority.ALWAYS);
		col4.setPercentWidth(15);
		root.getColumnConstraints().addAll(col1, col2, col3, col4);

		// Caption
		Text caption = new Text("Information");
		caption.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 40;");
		root.add(caption, 0, 0, 4, 1);
		
		Text ipHeader = new Text("IP Address:");
		ipHeader.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
		root.add(ipHeader, 1, 1, 1, 1);
		Text ipValue = new Text(InetAddress.getLocalHost().getHostAddress());
		ipValue.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
		root.add(ipValue, 2, 1, 1, 1);
		
		Text luxHeader = new Text("Lux value:");
		luxHeader.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
		root.add(luxHeader, 1, 2, 1, 1);
		Text luxValue = new Text(String.valueOf(HC2Interface.getLuxDeviceStatus(437)) + " (3)");
		luxValue.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
		root.add(luxValue, 2, 2, 1, 1);

		JFXButton okButton = new JFXButton("Close");
		okButton.setPrefSize(350, 40);
		okButton.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 20; -fx-background-color: #aabbcc;");
		okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try {
					stage.hide();
				} catch (Exception ex) {
					Util.logException(ex);
				}
			}
		});
		root.add(okButton, 0, 3, 4, 1);
		final Scene scene = new Scene(root, 460, 240, Color.TRANSPARENT);
		enableDragging(scene);
		stage.setScene(scene);
		centerStage(stage, 460, 240);
		return stage;
	}
}

