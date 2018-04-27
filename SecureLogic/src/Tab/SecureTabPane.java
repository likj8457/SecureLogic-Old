package Tab;

import java.util.ArrayList;

import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXToggleButton;

import application.MainWindow;
import application.Util;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class SecureTabPane extends JFXTabPane {
	private String backgroundColor = "#ffffff";
	private String foregroundColor = "Black";
	private WeatherTab weatherTab;
	private ZWaveDeviceTab indoorLightingTab;
	private ZWaveDeviceTab outdoorLightingTab;
	private TemperatureTab temperatureTab;
	private IrrigationTab irrigationTab;
	private LockTab lockTab;
	private SettingsTab settingsTab;
	private CameraTab cameraTab;
	
	private long lastInteraction = 0;
	private boolean disableScreenUpdate = false;
	
	private static MainWindow mainWindow;
	
	public SecureTabPane(MainWindow mW) {
		this.mainWindow = mW;
		
		weatherTab = new WeatherTab();
    	indoorLightingTab = new IndoorLightsTab();
    	outdoorLightingTab = new OutdoorLightsTab();
    	temperatureTab = new TemperatureTab();
	    irrigationTab = new IrrigationTab();
	    lockTab = new LockTab();
	    settingsTab = new SettingsTab();
	    cameraTab = new CameraTab();
	    		    
		getTabs().addAll(weatherTab,indoorLightingTab,outdoorLightingTab,temperatureTab,cameraTab,irrigationTab,lockTab,settingsTab);
	    
		setStyle("-fx-background-color: ffffff");
	}
	
	public void setFGAndBG(String fG, String bG) {
		backgroundColor = bG;
		foregroundColor = fG;
	}
	
	public void setControlBackAndForeground() {
		SecureTabPane sTB = this;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					sTB.setStyle("-fx-background-color: " + backgroundColor);
					
					ArrayList<Node> nodes = new ArrayList<Node>();
					addAllDescendents(sTB, nodes);
					
					for(Node node : nodes) {
						String style = node.getStyle();
						if (node instanceof JFXToggleButton) {
							String oldStyle = removePreviousForegroundStyle(node.getStyle(), "-fx-text-fill").trim();
									oldStyle += (!oldStyle.trim().endsWith(";") && oldStyle.trim().length() > 0) ? ";" : "";
							((JFXToggleButton)node).setStyle(oldStyle + "; -fx-text-fill: " + foregroundColor + ";");
						} else if (node instanceof Button) {
							String oldStyle = removePreviousForegroundStyle(node.getStyle(), "-fx-text-fill").trim();
							oldStyle += (!oldStyle.trim().endsWith(";") && oldStyle.trim().length() > 0) ? ";" : "";
							((Button)node).setStyle(oldStyle + "; -fx-text-fill: " + foregroundColor + ";");
						} else if (node instanceof Text) {
							String oldStyle = removePreviousForegroundStyle(node.getStyle(), "-fx-fill").trim();
							oldStyle += (!oldStyle.trim().endsWith(";") && oldStyle.trim().length() > 0) ? ";" : "";
							((Text)node).setStyle(oldStyle + " -fx-fill: " + foregroundColor + ";");
						}
					}
				} catch (Exception e) {
					Util.logException(e);	
				}
			}
		});
	}
	
	private static String removePreviousForegroundStyle (String hayStack, String style) {
		if (hayStack.startsWith(style)) {
			return hayStack.substring(hayStack.indexOf(";"));
		} else if (hayStack.indexOf(style) != -1) {
			int indexOfStyleStart = hayStack.indexOf(style);
			int indexOfStyleEnd = hayStack.indexOf(";", indexOfStyleStart);
			String end = "";
			if (indexOfStyleEnd != -1) 
			{
				end = hayStack.substring(indexOfStyleEnd + 1);
			}
			return hayStack.substring(0, indexOfStyleStart) + end;
			
		}
		return hayStack;
	}
	
	private static void addAllDescendents(Parent parent, ArrayList<Node> nodes) {
	    for (Node node : parent.getChildrenUnmodifiable()) {
	        nodes.add(node);
	        if (node instanceof Parent)
	            addAllDescendents((Parent)node, nodes);
	    }
	}
	
	public void updateWeather() {
		if (getSelectionModel().getSelectedItem() == weatherTab) 
		{
			weatherTab.updateWeather();
		}
	}
	
	public void updateCurrentDisplay () {
		if (getSelectionModel().getSelectedItem() == indoorLightingTab) 
		{
			try {
				indoorLightingTab.UpdateStates();
			} catch (Exception e) {
				Util.logException(e);
			}
		}
		else if (getSelectionModel().getSelectedItem() == outdoorLightingTab) 
		{
			try {
				outdoorLightingTab.UpdateStates();
			} catch (Exception e) {
				Util.logException(e);
			}
		}
		else if (getSelectionModel().getSelectedItem() == temperatureTab) 
		{
			try {
				temperatureTab.UpdateTemeratures();
			} catch (Exception e) {
				Util.logException(e);
			}
		}
		else if (getSelectionModel().getSelectedItem() == irrigationTab) 
		{
			try {
				irrigationTab.UpdateStates();
			} catch (Exception e) {
				Util.logException(e);
			}
		}
		else if (getSelectionModel().getSelectedItem() == lockTab) 
		{
			try {
				lockTab.Update();
			} catch (Exception e) {
				Util.logException(e);
			}
		}
		else if (getSelectionModel().getSelectedItem() == cameraTab) 
		{
			try {
				cameraTab.Update();
			} catch (Exception e) {
				Util.logException(e);
			}
		}
		else if (getSelectionModel().getSelectedItem() == weatherTab) 
		{
			try {
				weatherTab.UpdateTemperatures();
			} catch (Exception e) {
				Util.logException(e);
			}
		}
	}

	public long getLastInteraction() {
		return lastInteraction;
	}

	public void setLastInteraction(long lastInteraction) {
		this.lastInteraction = lastInteraction;
	}

	public boolean getDisableScreenUpdate() {
		return disableScreenUpdate;
	}

	public void setDisableScreenUpdate(boolean disableScreenUpdate) {
		this.disableScreenUpdate = disableScreenUpdate;
	}
	
	public boolean getScreenOff() {
		return settingsTab.getScreenOff();
	}
	
	public void setScreenOff(boolean screenOff) {
		settingsTab.setScreenOff(screenOff);
	}
	
	public static void destruct() {
		mainWindow.shutDown();
	}
}
