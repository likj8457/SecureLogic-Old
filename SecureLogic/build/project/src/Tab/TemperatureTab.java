package Tab;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.BroadcastingClient;
import com.BroadcastingServer;
import application.Util;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import zwave.fibaro.HC2Interface;

public class TemperatureTab extends SecureTab {
	
	private Text[] temperatureTexts;
	private String[] descriptions;
	private String[] iconUrls;
	private HashMap<Integer, Integer> indexToDeviceId;
	private HashMap<Integer, Group> temperatureIcons; //key is deviceId, value is icon group for blend
	private BroadcastingServer broadcastingServer;
    private ImageView[] alarmIcon = null;
	
	public TemperatureTab() {
		try {
			Image indoorTempIcon = new Image(getClass().getResource("/img/48x48/house-temp.png").openStream());
			setGraphic(new ImageView(indoorTempIcon));
	    } catch (Exception e) {
	    	Util.logException(e);
	    }
		
		indexToDeviceId = new HashMap<Integer, Integer>();
		indexToDeviceId.put(0, 296);
		indexToDeviceId.put(1, 436);
		indexToDeviceId.put(2, 269);
		indexToDeviceId.put(3, 247);
		indexToDeviceId.put(4, 359);
		indexToDeviceId.put(5, 421);
		indexToDeviceId.put(6, 275);
		indexToDeviceId.put(7, 345);
		indexToDeviceId.put(8, 451);
		
	    try {
	        Image icon = new Image(getClass().getResource("/img/16x16/bell.png").openStream());
	        alarmIcon = new ImageView[indexToDeviceId.size()];
	        for(int i = 0; i < alarmIcon.length; i++) {
	        	alarmIcon[i] = new ImageView(icon);
	        	alarmIcon[i].setBlendMode(BlendMode.SRC_ATOP);
	        }
	    } catch (Exception e) {
	    	Util.logException(e);
	    }
		temperatureIcons = new HashMap<>();
				
		descriptions = new String[indexToDeviceId.size()];
		descriptions[0] = "Kitchen";
		descriptions[1] = "Livingroom";
		descriptions[2] = "Master bedroom";
		descriptions[3] = "Washroom";
		descriptions[4] = "Bathroom";
		descriptions[5] = "Upstairs living";
		descriptions[6] = "Mirandas";
		descriptions[7] = "Garage (storage)";
		descriptions[8] = "Patio";

		iconUrls = new String[indexToDeviceId.size()];
		iconUrls[0] = "/img/48x48/kitchen.png";
		iconUrls[1] = "/img/48x48/livingroom.png";
		iconUrls[2] = "/img/48x48/bedroom.png";
		iconUrls[3] = "/img/48x48/washroom.png";
		iconUrls[4] = "/img/48x48/toilet.png";
		iconUrls[5] = "/img/48x48/upstairs.png";
		iconUrls[6] = "/img/48x48/kidsroom.png";
		iconUrls[7] = "/img/48x48/garage.png";
		iconUrls[8] = "/img/48x48/patio.png";
		
		temperatureTexts = new Text[indexToDeviceId.size()];
		
		initialize();
		try {
			broadcastingServer = new BroadcastingServer(this);
			broadcastingServer.start();
		} catch (Exception e) {
			Util.logException(e);
		}
		
		//Send notification to any other terminal that this one just started, and that we need info about set alarms.
		//Sending 0, 0 as deviceId and temperature will notify other terminals, and they will respond with what they have.
		byte[] buffer = Util.concat(Util.intToByteArray(0), Util.intToByteArray(0));
		
		try {
			BroadcastingClient bC = new BroadcastingClient();
			bC.broadcastPacket(buffer);
		} catch (Exception e) {
			Util.logException(e);
		}
	}
	
	private void initialize( ) {
		GridPane grid = new GridPane();
	    grid.setHgap(0);
	    grid.setVgap(0);
	    grid.setMinHeight(-1);
	    grid.setMinWidth(720);
	    grid.setPrefHeight(-1);
	    grid.setPrefWidth(-1);
	    grid.setAlignment(Pos.CENTER);
        //grid.setStyle("-fx-background-color: white; -fx-grid-lines-visible: true");
	    
	    ColumnConstraints col1 = new ColumnConstraints();
	    col1.setHalignment(HPos.CENTER);
	    col1.setHgrow(Priority.ALWAYS);
        col1.setPercentWidth(0);
        ColumnConstraints col2 = new ColumnConstraints();
	    col2.setHalignment(HPos.CENTER);
	    col2.setHgrow(Priority.ALWAYS);
        col2.setPercentWidth(30);    
        ColumnConstraints col3= new ColumnConstraints();
	    col3.setHalignment(HPos.CENTER);
	    col3.setHgrow(Priority.ALWAYS);
        col3.setPercentWidth(5);
        ColumnConstraints col4 = new ColumnConstraints();
	    col4.setHalignment(HPos.CENTER);
	    col4.setHgrow(Priority.ALWAYS);
        col4.setPercentWidth(30);
        ColumnConstraints col5 = new ColumnConstraints();
	    col5.setHalignment(HPos.CENTER);
	    col5.setHgrow(Priority.ALWAYS);
        col5.setPercentWidth(5);
        ColumnConstraints col6 = new ColumnConstraints();
	    col6.setHalignment(HPos.CENTER);
	    col6.setHgrow(Priority.ALWAYS);
        col6.setPercentWidth(30);
        ColumnConstraints col7 = new ColumnConstraints();
	    col7.setHalignment(HPos.CENTER);
	    col7.setHgrow(Priority.ALWAYS);
        col7.setPercentWidth(0);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6, col7);
        
	    RowConstraints row1 = new RowConstraints();
	    row1.setPercentHeight(5);
	    RowConstraints row2 = new RowConstraints();
	    row2.setPercentHeight(30);	    
	    RowConstraints row3 = new RowConstraints();
	    row3.setPercentHeight(30);        
	    RowConstraints row4 = new RowConstraints();
	    row4.setPercentHeight(30);
	    RowConstraints row5 = new RowConstraints();
	    row5.setPercentHeight(5);
        grid.getRowConstraints().addAll(row1, row2, row3, row4, row5);
        
	    int arrayIndex = 0;
	    for (int i = 0; i < 5; i++) {
	    	if (i == 0 || i == 4) {
	    		
	    	} else {
				for (int j = 0; j <= 6; j++ ) {
		    		//One even j:s put a pane (every other column)
		    		if ((j%2) == 0) {
		    	        Pane pane = new Pane();
		    		    pane.minHeight(10);
		    		    grid.add(pane, j, i, 1, 1);
		    		    GridPane.setVgrow(pane, Priority.ALWAYS);	    			
		    		} else {
			    		//On odd, place the temps...
			    		GridPane loopGrid = new GridPane();
			    		loopGrid.setHgap(10);
			    		loopGrid.setVgap(10);
			    		loopGrid.setAlignment(Pos.CENTER);
			            //loopGrid.setStyle("-fx-background-color: white; -fx-grid-lines-visible: true");
			    		
			    		final int finalArrayIndex = arrayIndex;
			    		loopGrid.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->{
			    			try {
			    				Stage stage = createStatisticsStage(loopGrid.getScene().getWindow(), indexToDeviceId.get(finalArrayIndex), temperatureTexts[finalArrayIndex].getText());
    							disableScreenUpdate(true);
    				            stage.showAndWait();
    				            disableScreenUpdate(false);
			    			} catch (Exception ex) {
			    				Util.logException(ex);
			    			}
			    		});
			    	    
			    	    ColumnConstraints loopCol1 = new ColumnConstraints();
			    	    loopCol1.setHalignment(HPos.RIGHT);
			    	    loopCol1.setHgrow(Priority.ALWAYS);
			    	    loopCol1.setPercentWidth(50);
			            ColumnConstraints loopCol2 = new ColumnConstraints();
			            loopCol2.setHalignment(HPos.LEFT);
			            loopCol2.setHgrow(Priority.ALWAYS);
			            loopCol2.setPercentWidth(50);
			            loopGrid.getColumnConstraints().addAll(loopCol1, loopCol2);
			            
			            ImageView roomIcon = null;
			            try {
				            Image icon = new Image(getClass().getResource(iconUrls[arrayIndex]).openStream());
				            roomIcon = new ImageView(icon);
			            } catch (Exception e) {
			            	Util.logException(e);
			            }

			            Group blend = new Group(
			            		roomIcon
			            );
			            
			            temperatureIcons.put(indexToDeviceId.get(finalArrayIndex), blend);
			            
			            loopGrid.add(blend, 0, 0, 1, 1);
			            
			            temperatureTexts[arrayIndex] = new Text("--");
			            temperatureTexts[arrayIndex].setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 40;");
			            loopGrid.add(temperatureTexts[arrayIndex], 1, 0, 1, 1);
			            Text description = new Text(descriptions[arrayIndex]);
			            description.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
			            loopGrid.add(description, 0, 1, 2, 1);
			            GridPane.setHalignment(description, HPos.CENTER);
			    	    
			            grid.add(loopGrid, j, i, 1, 1);
			            arrayIndex++;
		    		}
		    	}
	    	}
	    }
	    
	    setContent(grid);
	}

	
	private void setAlarmIconStatus(int deviceId, boolean addAlarmIcon) {
		if (addAlarmIcon) {
            temperatureIcons.get(deviceId).getChildren().add(alarmIcon[getIndexFromDeviceId(deviceId)]);
		} else {
            temperatureIcons.get(deviceId).getChildren().remove(alarmIcon[getIndexFromDeviceId(deviceId)]);			
		}
	}
	
	public int getIndexFromDeviceId(int deviceId) {
	    for (Entry<Integer, Integer> entry : indexToDeviceId.entrySet()) {
	        if (Objects.equals(deviceId, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return Integer.MIN_VALUE;
	}
		
	public void UpdateTemeratures() {
		for (int i = 0; i < temperatureTexts.length; i++) {
			try {
				final int deviceId = indexToDeviceId.get(i);
				String temp = HC2Interface.getTempDeviceStatus(deviceId);
				temperatureTexts[i].setText(temp);
				if(Util.getAlarmTemperature(deviceId) != Integer.MIN_VALUE) 
				{
					String degreeRemoved = temp.trim().substring(0, temp.length() -1);
					NumberFormat numberFormat =  NumberFormat.getInstance(Locale.FRANCE);
					Double returnValue = Double.valueOf(numberFormat.parse(degreeRemoved).doubleValue());
										
					int iTemp = (int)Math.round(returnValue);
					if (iTemp == Util.getAlarmTemperature(deviceId)) 
					{
						//Alarm
					}
					
					if (!temperatureIcons.get(deviceId).getChildren().contains(alarmIcon[getIndexFromDeviceId(deviceId)])) {
						Platform.runLater(() -> {
					        try {
					    		setAlarmIconStatus(deviceId, true);
					        } catch (Exception e) {
					        	Util.logException(e);
					        }
					    });
					}
				} else if (temperatureIcons.get(deviceId).getChildren().contains(alarmIcon[getIndexFromDeviceId(deviceId)])) {
					Platform.runLater(() -> {
				        try {
				    		setAlarmIconStatus(deviceId, false);
				        } catch (Exception e) {
				        	Util.logException(e);
				        }
				    });
				}
			} catch (Exception e) {
				Util.logException(e);
			}
		}
		Util.serializeTemperatureHistoryToDisk();
	}	
	
	public void destruct() {
		super.destruct();
		
		if (broadcastingServer != null) {
			broadcastingServer.stopServer();
		}
	}
}
