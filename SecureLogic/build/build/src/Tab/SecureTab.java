package Tab;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;

import application.RGBStage;
import application.Util;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import zwave.fibaro.HC2Interface;
import zwave.fibaro.RGBState;
import zwave.fibaro.ZWaveDevice;

public class SecureTab extends Tab {
	
	protected long buttonPressStart;

	protected void centerStage(Stage stage, double width, double height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - width) / 2);
        stage.setY((screenBounds.getHeight() - height) / 2);
    }
	
	protected void enableDragging(Scene scene) {
        final ObjectProperty<Point2D> mouseLocation = new SimpleObjectProperty<>();
        scene.setOnMousePressed(event -> mouseLocation.set(new Point2D((float)event.getScreenX(), (float)event.getScreenY())));
        scene.setOnMouseDragged(event -> {
            double mouseX = event.getScreenX();
            double mouseY = event.getScreenY();
            double deltaX = mouseX - mouseLocation.get().getX();
            double deltaY = mouseY - mouseLocation.get().getY();
            Window window = scene.getWindow();
            window.setX(window.getX() + deltaX);
            window.setY(window.getY() + deltaY);
            mouseLocation.set(new Point2D((float)mouseX, (float)mouseY));
        });
    }
	
	protected void disableScreenUpdate(boolean value) 
	{
    	if (getSecureTabPane().getDisableScreenUpdate()) 
    	{
    		this.getSecureTabPane().setLastInteraction(System.currentTimeMillis());
    	}
    	
    	getSecureTabPane().setDisableScreenUpdate(value);
	}
	
	protected SecureTabPane getSecureTabPane() {
		return ((SecureTabPane)getTabPane());
	}
	
	protected void dimOrSwitch(Node node, ZWaveDevice zDevice) {
		
		node.pressedProperty().addListener((observable, wasPressed, pressed) -> {
	        if (pressed) {
	        	buttonPressStart = System.currentTimeMillis();
	        } else {
	        	if (System.currentTimeMillis() - buttonPressStart > 1000) {
	        		try {

	        	        RGBState rgbState = HC2Interface.getRGBDeviceStatus(zDevice.getDeviceIds()[0]);
	        	        		
	        			Scene s = node.getScene();
	        			Window w = s.getWindow();
	        			RGBStage stage = createRGBDialog(w, rgbState);
						disableScreenUpdate(true);
			            stage.showAndWait();
			            disableScreenUpdate(false);
			            
			            int[] rgb = Util.hex2Rgb(stage.getColor());
			            
			            RGBState rgbStateAfter = new RGBState(rgb[0], rgb[1], rgb[2], 0, stage.getLevel());
			            for(int deviceId : zDevice.getDeviceIds()) {
			            	HC2Interface.setRGBDeviceStatus(deviceId, rgbStateAfter);
			            }
	        		} catch (Exception ex) {
						Util.logException(ex);
					}
	        	} else {
	        		//Short press, flip on/off
	        		for(int deviceId : zDevice.getDeviceIds()) {
		        		try {
							HC2Interface.setRGBDeviceOnOff(deviceId, !zDevice.getState());
						} catch (Exception ex) {
							Util.logException(ex);
						}
	        		}
	        	}
	        }
	    });
    }
	
	protected RGBStage createRGBDialog(Window owner, RGBState rgbState) throws Exception {
		RGBStage stage = new RGBStage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        
        
        JFXButton okButton = new JFXButton("Set");
        okButton.setMinSize(150, 50);
        String currentColor = String.format("#%02x%02x%02x", rgbState.getR(), rgbState.getG(), rgbState.getB()); 
        okButton.setStyle("-fx-background-color:" + currentColor);
        
        String[] colors = new String[] {"#9C27B0", "#4CAF50", "#FFF59D", "#FF8A65"};
        
        JFXButton[] buttons = new JFXButton[colors.length];
        
        for(int i = 0; i < colors.length; i++) 
        {
        	final int index = i;
        	buttons[i] = new JFXButton();
        	buttons[i].setStyle("-fx-background-color: " + colors[i]);
        	buttons[i].setMinSize(80, 80);
        	buttons[i].pressedProperty().addListener((observable, wasPressed, pressed) -> {okButton.setStyle("-fx-background-color:" + colors[index]); stage.setColor(colors[index]); });
        }
        
        HBox colorContainer = new HBox(10, buttons);
        JFXSlider jS = new JFXSlider();
        jS.setMax(100);
        jS.setValue(rgbState.getLevel());
        jS.valueProperty().addListener((observable) -> {
            stage.setLevel((int)jS.getValue());
        });        
        
        okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					stage.hide();
				} catch (Exception ex) {
					Util.logException(ex);	
				}
			}
		});
        
        VBox dialogRoot = new VBox(30, colorContainer, jS, okButton);
        dialogRoot.setPadding(new Insets(10, 50, 0, 50));
        dialogRoot.setAlignment(Pos.CENTER);
        dialogRoot.setStyle("-fx-background-color: derive(white, 25%) ; -fx-border-color: black;"
                    + "-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
        final Scene scene = new Scene(dialogRoot, 450, 300,
                Color.TRANSPARENT);
        enableDragging(scene);
        stage.setScene(scene);
        centerStage(stage, 450, 300);
        return stage;
    }
}
