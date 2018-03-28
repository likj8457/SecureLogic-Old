package Tab;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXButton.ButtonType;

import application.ConfirmStage;
import application.PinStage;
import application.Util;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import zwave.fibaro.HC2Interface;
import zwave.fibaro.ZWaveDevice;

public class LockTab extends SecureTab {
	private ArrayList<ZWaveDevice> devices = new ArrayList<ZWaveDevice>();
	private JFXButton[] buttons;
	
	private final String BULLET = "\u2022";
	
	public LockTab() {
		try {
			Image lockIcon = new Image(getClass().getResource("/img/48x48/padlock.png").openStream());
			setGraphic(new ImageView(lockIcon));
	    } catch (Exception e) {
	    	Util.logException(e);
	    }

	    devices.add(new ZWaveDevice("", ZWaveDevice.DeviceTypes.Scene, "/img/128x128/house.png", "Home", new int[]{7}));
		devices.add(new ZWaveDevice("", ZWaveDevice.DeviceTypes.Scene, "/img/128x128/alarm-perimeter.png", "Perimeter", new int[]{11}));
		devices.add(new ZWaveDevice("", ZWaveDevice.DeviceTypes.Scene, "/img/128x128/alarm-away.png", "Away", new int[]{12}));
	    devices.add(new ZWaveDevice("", ZWaveDevice.DeviceTypes.Scene, "/img/128x128/alarm-vacation.png", "Vacation", new int[]{13}));
		devices.add(new ZWaveDevice("", ZWaveDevice.DeviceTypes.OnOff, "/img/128x128/garage-open.png", new int[]{412}));
		devices.add(new ZWaveDevice("", ZWaveDevice.DeviceTypes.Door, "/img/128x128/door-lock.png", new int[]{393}));
		devices.add(new ZWaveDevice("", ZWaveDevice.DeviceTypes.Scene, "/img/128x128/mute.png", new int[]{29}));
		
	    initialize();
	}

	private void initialize() {
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
        col2.setPercentWidth(25);    
        ColumnConstraints col3= new ColumnConstraints();
	    col3.setHalignment(HPos.CENTER);
	    col3.setHgrow(Priority.ALWAYS);
        col3.setPercentWidth(25);
        ColumnConstraints col4 = new ColumnConstraints();
	    col4.setHalignment(HPos.CENTER);
	    col4.setHgrow(Priority.ALWAYS);
        col4.setPercentWidth(25);
        ColumnConstraints col5= new ColumnConstraints();
	    col5.setHalignment(HPos.CENTER);
	    col5.setHgrow(Priority.ALWAYS);
        col5.setPercentWidth(25);
        ColumnConstraints col6 = new ColumnConstraints();
	    col6.setHalignment(HPos.LEFT);
	    col6.setHgrow(Priority.ALWAYS);
        col6.setPercentWidth(0);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6);
        
	    //Left spacing
	    Pane pane1 = new Pane();
	    pane1.minHeight(10);
	    grid.add(pane1, 0, 0, 1, 1);
	    GridPane.setVgrow(pane1, Priority.ALWAYS);

	    /*JFXButton buggButton = new JFXButton(" ", new ImageView(new Image(getClass().getResource("/img/48x48/padlock.png").openStream())));
	    grid.add(buggButton, 0, 1, 1, 1);
	    buggButton.setVisible(false);*/
	    
	    buttons = new JFXButton[devices.size()];
	    
	    for(int i = 0; i < devices.size(); i++) 
	    {
	    	ZWaveDevice zDevice = devices.get(i);
	    	if (zDevice == null) 
	    	{
	    		continue;
	    	}
	    	
	    	try {
	    		buttons[i] = new JFXButton(zDevice.getName(), new ImageView(new Image(getClass().getResource(zDevice.getImageURL()).openStream())));
	    	} catch (Exception e) {
	    		Util.logException(e);
	    		continue;
	    	}
	    	
	    	buttons[i].setMinSize(150, 150);
	    	buttons[i].setPrefSize(150, 150);
	    	buttons[i].setAlignment(Pos.CENTER);
	    	buttons[i].setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30; -fx-background-color: #aabbcc;");
	    	buttons[i].setButtonType(ButtonType.RAISED);
	    	if (i < 4) {
	    		grid.add(buttons[i], i+1, 1, 1, 1);
	    	} else {
	    		grid.add(buttons[i], i-3, 3, 1, 1);
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
	    		case Scene:
	    			final JFXButton button = buttons[i];
	    			button.setOnAction(new EventHandler<ActionEvent>() {
	    				@Override public void handle(ActionEvent e) {
	    					try {
	    						if (!HC2Interface.getVariableValue("AlarmType").equals(zDevice.getReqSystemState())) {
	    							PinStage stage = createKeypadDialog(button.getScene().getWindow());
	    							disableScreenUpdate(true);
	    				            stage.showAndWait();
	    				            disableScreenUpdate(false);
	    				            	    				            
	    				            if (stage.getConfirmed()) {
	    				            	if (zDevice.getReqSystemState().equalsIgnoreCase("home")) {
	    				            		for(int deviceId : zDevice.getDeviceIds()) {
		    			    					try {
		    			    						HC2Interface.runScene(deviceId, stage.GetPin());
		    			    					} catch (Exception ex) {
		    			    						Util.logException(ex);
		    			    					}
		    		    					}
	    				            	} else {
	    				            		Window w =button.getScene().getWindow();
		    				            	ConfirmStage armingWaitDialog = createArmingDialog(w);
		    				            	disableScreenUpdate(true);
		    				            	armingWaitDialog.showAndWait();
		    				            	disableScreenUpdate(false);
		    				            	if (armingWaitDialog.getConfirmed()) {
		    				            		for(int deviceId : zDevice.getDeviceIds()) {
			    			    					try {
			    			    						HC2Interface.runScene(deviceId, stage.GetPin());
			    			    					} catch (Exception ex) {
			    			    						Util.logException(ex);
			    			    					}
			    		    					}
		    				            	}
	    				            	}
	    			            		HC2Interface.forceUpdate();
	    				            }
	    						}
	    					} catch (Exception ex) {
	    						Util.logException(ex);	
	    					}
	    				}
	    			});
	    			break;
	    		case Door : break;
	    	}
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
	
	public void Update() {
		try {
			//Update arming buttons
			String armingType = HC2Interface.getVariableValue("AlarmType");
			
			for(int i = 0; i < devices.size()-2; i++) { //-2 means dont disable the two last buttons (door and garage)
				if (armingType.equalsIgnoreCase(devices.get(i).getReqSystemState())) {
					buttons[i].setStyle("-fx-background-color: #A84545;");
					
					for(int j = 0; j < devices.size()-2; j++) {
						if (i == 0) {
							buttons[j].setDisable(false);
						} else if (j != 0) {
							buttons[j].setDisable(true);
						}
						if (j == i) {
							continue;
						}
						
						buttons[j].setStyle("-fx-background-color: #aabbcc;");
					}
				}
			}		
		} catch (Exception e) {
			Util.logException(e);
		}
	}
	
	private PinStage createKeypadDialog(Window owner) {
        PinStage stage = new PinStage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        
        Text tF = new Text("Enter code");
        tF.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 40; -fx-stroke: white;");
        
    	Image tempImageback = null;
    	Image tempImageOk = null;
    	Image tempImageCancel = null;
        try {
        	tempImageback = new Image(getClass().getResource("/img/32x32/left-arrow.png").openStream());
        	tempImageOk = new Image(getClass().getResource("/img/32x32/right-arrow.png").openStream());
        	tempImageCancel = new Image(getClass().getResource("/img/32x32/cancel.png").openStream());
        } catch (Exception e) {
			Util.logException(e);
        }
        final Image imageback = tempImageback;
    	final Image imageOk = tempImageOk;
    	final Image imageCancel = tempImageCancel;
        
        Button[] buttonsNum = new Button[10];
        Button buttonOk = new Button();
        Button buttonBack = new Button();
        
        for (int i = 0; i < buttonsNum.length; i++) {
        	buttonsNum[i] = new Button(String.valueOf(i));
        	buttonsNum[i].setTextAlignment(TextAlignment.CENTER);
        	buttonsNum[i].setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 40;");
        	buttonsNum[i].setMinWidth(100);
        	buttonsNum[i].setMaxWidth(100);
        	buttonsNum[i].setMaxHeight(70);
        	buttonsNum[i].setMinHeight(70);
        	buttonsNum[i].setOnAction(new EventHandler<ActionEvent>() {
    			@Override public void handle(ActionEvent e) {
    				try {
    					if (tF.getText().equals("Enter code")) {
    				        tF.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 60; -fx-stroke: white;");
        					tF.setText(BULLET);
        					
        					if (e.getSource() instanceof Button) 
        					{
        						String digit = ((Button)e.getSource()).getText();
        						char c = digit.trim().charAt(0);
        						stage.AddPinDigit(c);
        					}
    			        	
        					buttonBack.setGraphic(new ImageView(imageback));
    					} else if (tF.getText().length() < 8){
    						tF.setText(tF.getText() + BULLET);
    						
        					if (e.getSource() instanceof Button) 
        					{
        						String digit = ((Button)e.getSource()).getText();
        						char c = digit.trim().charAt(0);
        						stage.AddPinDigit(c);
        					}
    					}
    				} catch (Exception ex) {
    					Util.logException(ex);	
    				}
    			}
    		});
        }

        try {            
        	buttonOk.setGraphic(new ImageView(imageOk));
        } catch (Exception e) {
			Util.logException(e);
        }
        
        buttonOk.setTextAlignment(TextAlignment.CENTER);
        buttonOk.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 35; -fx-background-color: green; -fx-background-radius: 0 0 25 0;");
        buttonOk.setMinWidth(100);
        buttonOk.setMaxWidth(100);
        buttonOk.setMaxHeight(70);
        buttonOk.setMinHeight(70);
        buttonOk.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					stage.setConfirmed(true);
					stage.hide();
				} catch (Exception ex) {
					Util.logException(ex);
				}
			}
		});

        try {                    
        	buttonBack.setGraphic(new ImageView(imageCancel));
        } catch (Exception e) {
			Util.logException(e);
        }
        buttonBack.setTextAlignment(TextAlignment.CENTER);
        buttonBack.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 45; -fx-background-color: #ad4038;-fx-background-radius: 0 0 0 25;");
        buttonBack.setMinWidth(100);
        buttonBack.setMaxWidth(100);
        buttonBack.setMaxHeight(70);
        buttonBack.setMinHeight(70);
        buttonBack.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					if (e.getSource() instanceof Button) 
					{
						stage.RemoveLastPinDigit();
					}
					
					if (tF.getText().equals("Enter code")) {
						stage.setConfirmed(false);
						stage.hide();
						return;
					}
					
					if (tF.getText().length() == 1) 
					{
			        	//Image imageback = new Image(getClass().getResource("/img/cancel.png").openStream());
			        	buttonBack.setGraphic(new ImageView(imageCancel));
					}
					
					if (tF.getText().length() > 0){
						tF.setText(tF.getText().substring(0, tF.getText().length()-1));
					} 
					
					if (tF.getText().length() == 0)
					{
						tF.setText("Enter code");
				        tF.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 40; -fx-stroke: white;");
					}
				} catch (Exception ex) {
					Util.logException(ex);
				}
			}
		});
        
        HBox display = new HBox(15, tF);
        display.setStyle("-fx-background-color: #909090;-fx-background-radius: 20px; -fx-min-height: 71");
        
        HBox displayRoot = new HBox(display);
        HBox.setHgrow(display, Priority.ALWAYS);
        displayRoot.setPadding(new Insets(20, 20, 20, 20));
        
        HBox numpad13 = new HBox(15, buttonsNum[1], buttonsNum[2], buttonsNum[3]);
        numpad13.setPadding(new Insets(0, 0, 0, 20));
        HBox numpad46 = new HBox(15, buttonsNum[4], buttonsNum[5], buttonsNum[6]);
        numpad46.setPadding(new Insets(0, 0, 0, 20));
        HBox numpad79 = new HBox(15, buttonsNum[7], buttonsNum[8], buttonsNum[9]);
        numpad79.setPadding(new Insets(0, 0, 0, 20));
        HBox numpadbottom = new HBox(15, buttonBack, buttonsNum[0], buttonOk);
        numpadbottom.setPadding(new Insets(0, 0, 20, 20));
        
        VBox dialogRoot = new VBox(10, displayRoot, numpad13, numpad46, numpad79, numpadbottom);
        dialogRoot.setAlignment(Pos.CENTER);
        dialogRoot.setStyle("-fx-background-color: derive(black, 25%) ; -fx-border-color: black;"
                    + "-fx-background-radius: 20px; -fx-border-radius: 20px; -fx-border-width: 1px;");
        final Scene scene = new Scene(dialogRoot, 370, 450,
                Color.TRANSPARENT);
        enableDragging(scene);
        stage.setScene(scene);
        centerStage(stage, 370, 450);
        

        displayRoot.setAlignment(Pos.CENTER);
        display.setAlignment(Pos.CENTER);
        
        return stage;
    }
	
	private ConfirmStage createArmingDialog(Window owner) throws Exception {
        ConfirmStage stage = new ConfirmStage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);


        ImageView imageView = new ImageView();
        imageView.setImage(new Image(getClass().getResource("/img/512x512/padlock-lock.gif").openStream(), 200, 200, false, false));
        Text label = new Text("30");
        label.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 60;");
        Instant start = Instant.now();
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					stage.setConfirmed(false);
					stage.hide();
				} catch (Exception ex) {
					Util.logException(ex);	
				}
			}
		});
        
        VBox dialogRoot = new VBox(0,imageView, label, cancelButton);
        dialogRoot.setPadding(new Insets(10, 50, 0, 50));
        dialogRoot.setAlignment(Pos.CENTER);
        dialogRoot.setStyle("-fx-background-color: derive(white, 25%) ; -fx-border-color: black;"
                    + "-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
        final Scene scene = new Scene(dialogRoot, 500, 400,
                Color.TRANSPARENT);
        enableDragging(scene);
        stage.setScene(scene);
        centerStage(stage, 500, 400);
        
        Timer armingTimer = new Timer();
        TimerTask armingTimerTask = new TimerTask() {
            @Override
            public void run() {            	
            	try 
            	{
            		Platform.runLater(new Runnable() {
            			@Override
            			public void run() {
            				try {
            					Instant now = Instant.now();
            					long elapsed = 30000 - Duration.between(start, now).toMillis();
            					double dElapsed = elapsed / 1000;
            					label.setText(String.valueOf(Math.round(dElapsed)));
            					
            					if (dElapsed < 1d) {
            						stage.close();
            					}
            				} catch (Exception e) {
            					Util.logException(e);	
            				}
            			}
            		});
	        	} catch (Exception e) {
					Util.logException(e);
	        	}
            }
		};
		armingTimer.schedule(armingTimerTask, 0, 1000);

		stage.setConfirmed(true);
        return stage;
    }
}
/*
<GridPane alignment="center" hgap="30" vgap="0" fx:id="flow" minHeight="-Infinity" minWidth="720" prefHeight="-Infinity" prefWidth="-Infinity" gridLinesVisible="false">
	<columnConstraints> 
		<ColumnConstraints halignment="LEFT" hgrow="always" percentWidth="0"/>
		<ColumnConstraints halignment="CENTER" hgrow="always" percentWidth="25"/>
		<ColumnConstraints halignment="CENTER" hgrow="always" percentWidth="25"/>
		<ColumnConstraints halignment="CENTER" hgrow="always" percentWidth="25"/>
		<ColumnConstraints halignment="CENTER" hgrow="always" percentWidth="25"/>
		<ColumnConstraints halignment="LEFT" hgrow="always" percentWidth="0"/>
	</columnConstraints>
	<children>
	<Pane GridPane.columnIndex="0" GridPane.rowIndex="0" GridPane.columnSpan="1"></Pane>
	
	<JFXButton fx:id="armingButtonHome" GridPane.columnIndex="1" GridPane.rowIndex="1" GridPane.columnSpan="1" buttonType="RAISED" prefHeight="150.0" prefWidth="150.0" ripplerFill="RED" style="-fx-background-color: #aabbcc;" text="">
		<graphic>
        	<ImageView fitHeight="128.0" fitWidth="128.0" pickOnBounds="true" preserveRatio="true">
               <image>
                  <Image url="@img/house.png" />
               </image>
            </ImageView>
        </graphic>
	</JFXButton>
	<JFXButton fx:id="armingButtonPerimeter" GridPane.columnIndex="2" GridPane.rowIndex="1" GridPane.columnSpan="1" buttonType="RAISED" prefHeight="150.0" prefWidth="150.0" ripplerFill="RED" style="-fx-background-color: #aabbcc;" text="">
		<graphic>
        	<ImageView fitHeight="128.0" fitWidth="128.0" pickOnBounds="true" preserveRatio="true">
               <image>
                  <Image url="@img/alarm-perimeter.png" />
               </image>
            </ImageView>
        </graphic>
	</JFXButton>
	<JFXButton fx:id="armingButtonAway" GridPane.columnIndex="3" GridPane.rowIndex="1" GridPane.columnSpan="1" buttonType="RAISED" prefHeight="150.0" prefWidth="150.0" ripplerFill="RED" style="-fx-background-color: #aabbcc;" text="">
		<graphic>
        	<ImageView fitHeight="128.0" fitWidth="128.0" pickOnBounds="true" preserveRatio="true">
               <image>
                  <Image url="@img/alarm-away.png" />
               </image>
            </ImageView>
        </graphic>
	</JFXButton>
	<JFXButton fx:id="armingButtonVacation" GridPane.columnIndex="4" GridPane.rowIndex="1" GridPane.columnSpan="1" buttonType="RAISED" prefHeight="150.0" prefWidth="150.0" ripplerFill="RED" style="-fx-background-color: #aabbcc;" text="">
		<graphic>
        	<ImageView fitHeight="128.0" fitWidth="128.0" pickOnBounds="true" preserveRatio="true">
               <image>
                  <Image url="@img/alarm-Vacation.png" />
               </image>
            </ImageView>
        </graphic>
	</JFXButton>
	
	<Pane GridPane.columnIndex="0" GridPane.rowIndex="2" GridPane.columnSpan="1" prefHeight="30.0" > </Pane>
	
	<JFXButton fx:id="garageButton" GridPane.columnIndex="2" GridPane.rowIndex="3" GridPane.columnSpan="1" buttonType="RAISED" prefHeight="150.0" prefWidth="150.0" ripplerFill="RED" style="-fx-background-color: #aabbcc;" text="">
		<graphic>
        	<ImageView fitHeight="128.0" fitWidth="128.0" pickOnBounds="true" preserveRatio="true">
               <image>
                  <Image url="@img/garage-open.png" />
               </image>
            </ImageView>
        </graphic>
	</JFXButton>
	<JFXButton fx:id="doorButton" GridPane.columnIndex="3" GridPane.rowIndex="3" GridPane.columnSpan="1" buttonType="RAISED" prefHeight="150.0" prefWidth="150.0" ripplerFill="RED" style="-fx-background-color: #aabbcc;" text="">
		<graphic>
        	<ImageView fitHeight="128.0" fitWidth="128.0" pickOnBounds="true" preserveRatio="true">
               <image>
                  <Image url="@img/door-lock.png" />
               </image>
            </ImageView>
        </graphic>
	</JFXButton>
	</children>
</GridPane> 
*/