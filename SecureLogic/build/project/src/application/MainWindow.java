package application;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import Tab.SecureTabPane;
import cam.MJpegFXViewer;
import cam.MjpegFXRunner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import zwave.fibaro.HC2Interface;

public class MainWindow extends Application implements SLCommons.MainScene
{
	private Timer weatherTimer;
	private TimerTask weatherTimerTask;
	private Timer guiTimer;
	private TimerTask guiTimerTask;
	private Timer nightModeTimer;
	private TimerTask nightModeTimerTask;
	private Timer secondTimer;
	private TimerTask secondTimerTask;
	private Timer minuteTimer;
	private TimerTask minuteTimerTask;
	private LocalTime onTime;
	private LocalTime offTime;
	
	private static long lastThumb = 0;
	
	private static Stage mainStage;
	
	private boolean nightMode = false;
		
	private HashMap<String, String> settings;
	
	private SecureTabPane tabPane;
	
	private boolean shutDown;
	
	@Override
	public void start(Stage primaryStage) {
		initialize();
	}
	
	public void initialize() {
		settings = Util.readProperties();
				
		String ip = settings.get("hc2ip");
		String user = settings.get("hc2user");
		String pass = settings.get("hc2pass");
		HC2Interface.init("http://" + ip, user + ":" + pass);
		HC2Interface.start();
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {

			        Util.startLogger();
			        
					mainStage = new Stage();
					VBox root = generateLayout();
					Scene scene = new Scene(root, 800, 480);
					scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
					mainStage.setScene(scene);
					mainStage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("Ctrl+c"));
					mainStage.show();			
					scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
					    @Override
					    public void handle(MouseEvent mouseEvent) {
					    	if (tabPane != null) {
					    		tabPane.setLastInteraction(System.currentTimeMillis());
					    	}
					        
					    	//Wake screen now when something happened...
					        if (tabPane!= null && tabPane.getScreenOff()) {
					        	tabPane.setScreenOff(false);
					        }
					    }
					});
				} catch (Exception ex) 
				{
					Util.logException(ex);
					ex.printStackTrace();
				}
				
				controlSetup();
			}
		});
	}
	
	private VBox generateLayout() throws Exception {
		Font.loadFont(MainWindow.class.getResource("Roboto-Thin.ttf").toExternalForm(), 10);
	    tabPane = new SecureTabPane(this);
	    
	    AnchorPane anchorpane = new AnchorPane();
	    anchorpane.maxHeight(-1);
	    anchorpane.maxWidth(-1);
	    anchorpane.prefHeight(-1);
	    anchorpane.prefWidth(-1);
	    anchorpane.getChildren().addAll(tabPane);   // Add grid from Example 1-5
	    AnchorPane.setBottomAnchor(tabPane, 0.0);
	    AnchorPane.setRightAnchor(tabPane, 0.0);
	    AnchorPane.setLeftAnchor(tabPane, 0.0);
	    AnchorPane.setTopAnchor(tabPane, 0.0);
	    BorderPane.setAlignment(tabPane, Pos.CENTER);
			
		VBox display = new VBox(0, anchorpane);
		VBox.setVgrow(anchorpane, Priority.ALWAYS);
		
		return display;
	}
	
	public void controlSetup() 
	{		
		tabPane.setControlBackAndForeground();
		
		mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent event) {
		        stop();
		    }
		});
		
		Font.loadFont(MainWindow.class.getResource("Roboto-Thin.ttf").toExternalForm(), 10);
						
		weatherTimer = new Timer();
		weatherTimerTask = new TimerTask() {

            @Override
            public void run() {
            	tabPane.updateWeather();
            }
		};
        weatherTimer.schedule(weatherTimerTask, 0, 60000);
		
		nightModeTimer = new Timer();
		nightModeTimerTask = new TimerTask() {
            @Override
            public void run() {
            	if(checkLightConditions()) {
            		tabPane.setControlBackAndForeground();
            	}
            }
		};
		nightModeTimer.schedule(nightModeTimerTask, 1000, 10000);
		
        try 
        {
        	TimeService.start();
        } catch (Exception e) {
        	Util.logException(e);
        }
        		
        guiTimer = new Timer();
		guiTimerTask = new TimerTask() {

            @Override
            public void run() {
            	Platform.runLater(new Runnable() {
					@Override
					public void run() {
						try {
							tabPane.updateCurrentDisplay();							
						} catch (Exception e) {
							Util.logException(e);
						}
					}
		        });
            }
		};		
		guiTimer.schedule(guiTimerTask, 1000, 1000);
		

		secondTimer = new Timer();
		secondTimerTask = new TimerTask() {
            @Override
            public void run() {
            	try {
            		if (shutDown) {
            			stop();
            			return;
            		}
            		
            		if (tabPane != null) {
	            		//Return to homescreen
		            	if (System.currentTimeMillis() - tabPane.getLastInteraction() > 30000 && tabPane != null && tabPane.getSelectionModel().getSelectedIndex() > 0 && !tabPane.getDisableScreenUpdate()) 
		            	{
	
		            		Platform.runLater(new Runnable() {
		            			@Override
		    					public void run() {
		            				tabPane.getSelectionModel().select(0);
		            			}
		            		});
		            	}
		            	
		            	//Turn off screen at night - if selected
		            	LocalTime now = LocalTime.now();
		            	if ((now.isAfter(offTime) || now.isBefore(onTime)) && tabPane.getScreenOffAtNight() && !tabPane.getScreenOff() && System.currentTimeMillis() - tabPane.getLastInteraction() > 9000 && !tabPane.getDisableScreenUpdate()) 
		            	{
		            		tabPane.setScreenOff(true);
		            	}
            		}
            	} catch (Exception e) {
					Util.logException(e);
            	}
            }
		};
		secondTimer.schedule(secondTimerTask, 0, 1000);
		
		minuteTimer = new Timer();
		minuteTimerTask = new TimerTask() {
            @Override
            public void run() {
            	try {
            		settings = Util.readProperties();
            		
            		//Setup time of day variables for screen blanking
            		String onTimeStr = settings.getOrDefault("ScreenOnTime", "21:00");
            	    onTime = LocalTime.parse(onTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
            	    
            		String offTimeStr = settings.getOrDefault("ScreenOffTime", "07:00");
            	    offTime = LocalTime.parse(offTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
            	    
            	} catch (Exception e) {
					Util.logException(e);
            	}
            	
            	try 
            	{
            		if (!TimeService.isRunning()) 
            		{
            			Util.LogToFile("TimeService NOT running");
            			TimeService.stop();
            			Util.LogToFile("TimeService stopped");
            			TimeService.start();
            			Util.LogToFile("TimeService started");
            		}
            		else 
            		{
            			Util.LogToFile("TimeService running");
            		}
	        	} catch (Exception e) {
					Util.logException(e);
	        	}
            }
		};
		minuteTimer.schedule(minuteTimerTask, 0, 60000);
		

		//indoor siren
		/*toggle_ida.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
		toggle_ida.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					JFXToggleButton toggle = (JFXToggleButton)e.getSource();
					HC2Interface.setLightDeviceStatus(306, toggle.isSelected()?1:0);
				} catch (Exception ex) {
					Util.LogToException(ex);
				}
			}
		});*/
		
		//outdoor siren
		/*toggle_oda.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
		toggle_oda.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					JFXToggleButton toggle = (JFXToggleButton)e.getSource();
					HC2Interface.setLightDeviceStatus(252, toggle.isSelected()?1:0);
				} catch (Exception ex) {
					Util.LogToException(ex);
				}
			}
		});*/
		
		//Disable screen turnoff
		if (!System.getProperty("os.name").startsWith("Windows")) {
			Util.disableDPMS();
		}		
		
		
	}
	
	public void stop() {
	    weatherTimerTask.cancel();
	    weatherTimer.cancel();
	    guiTimerTask.cancel();
	    guiTimer.cancel();
	    nightModeTimerTask.cancel();
	    nightModeTimer.cancel();
	    HC2Interface.stop();
	    TimeService.stop();
	    Util.stopLogger();
	    secondTimerTask.cancel();
	    secondTimer.cancel();
	}
	
	/**
	 * Check if nightmode or daymode is to be used
	 * @return true if there is a need to switch between modes
	 */
	private boolean checkLightConditions() {
		try {
			int lux = HC2Interface.getLuxDeviceStatus(437); //Light conditions for livingroom
			
			if (lux < 3 && !nightMode) {
				//Nightmode
				tabPane.setFGAndBG("#696969", "Black");
				nightMode = true;
				return true;
			} else if (lux > 3 && nightMode) {
				//Daymode
				tabPane.setFGAndBG("Black", "White");
				nightMode = false;
				return true;
			}
		} catch (Exception e) {
			Util.logException(e);	
		}
		return false;
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	public void shutDown() {
		this.shutDown = true;
	}    
}
