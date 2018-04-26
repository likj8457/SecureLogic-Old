package Tab;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.BroadcastingClient;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;

import application.RGBStage;
import application.TemperatureStage;
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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
	protected static final String DEGREE  = "\u00b0";
	
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

	public void destruct() {
		
	}
	
	protected TemperatureStage createTemperatureDialog(Window owner, String startTemperature) {
		final TemperatureStage stage = new TemperatureStage();
		stage.setTemperature(20);
		try {
			if(startTemperature.trim().length() > 0 && !startTemperature.trim().equals("--")) {
				String degreeRemoved = startTemperature.trim().substring(0, startTemperature.length() -1);
				NumberFormat numberFormat =  NumberFormat.getInstance(Locale.FRANCE);
				Double returnValue = Double.valueOf(numberFormat.parse(degreeRemoved).doubleValue());
				
				stage.setTemperature((int)Math.round(returnValue));
			}
		} catch (NumberFormatException nFE) {
			Util.logException(nFE);
		} catch (ParseException pE) {
			Util.logException(pE);
		}
		
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        
        final Text tF = new Text(String.valueOf(stage.GetTemperature()) + DEGREE);
        tF.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 80; -fx-stroke: white; -fx-fill:white;");
        
    	Image tempImageUp = null;
    	Image tempImageDown = null;
        try {
        	tempImageUp = new Image(getClass().getResource("/img/32x32/up-arrow.png").openStream());
        	tempImageDown = new Image(getClass().getResource("/img/32x32/down-arrow.png").openStream());
        } catch (Exception e) {
			Util.logException(e);
        }
        final Image imageUp = tempImageUp;
    	final Image imageDown = tempImageDown;
        
        Button buttonDown = new Button();
        Button buttonUp = new Button();
        Button buttonOk = new Button("Set");
        
        try {            
        	buttonDown.setGraphic(new ImageView(imageDown));
        } catch (Exception e) {
			Util.logException(e);
        }
        
        buttonDown.setTextAlignment(TextAlignment.CENTER);
        buttonDown.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 35; -fx-background-color: green; -fx-background-radius: 0 0 25 25;");
        buttonDown.setMinWidth(100);
        buttonDown.setMaxWidth(100);
        buttonDown.setMaxHeight(70);
        buttonDown.setMinHeight(70);
        buttonDown.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					stage.setTemperature(stage.GetTemperature()-1);
					tF.setText(String.valueOf(stage.GetTemperature()) + DEGREE);
				} catch (Exception ex) {
					Util.logException(ex);
				}
			}
		});

        try {                    
        	buttonUp.setGraphic(new ImageView(imageUp));
        } catch (Exception e) {
			Util.logException(e);
        }
        buttonUp.setTextAlignment(TextAlignment.CENTER);
        buttonUp.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 45; -fx-background-color: green;-fx-background-radius: 25 25 0 0;");
        buttonUp.setMinWidth(100);
        buttonUp.setMaxWidth(100);
        buttonUp.setMaxHeight(70);
        buttonUp.setMinHeight(70);
        buttonUp.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					stage.setTemperature(stage.GetTemperature()+1);
					tF.setText(String.valueOf(stage.GetTemperature()) + DEGREE);
				} catch (Exception ex) {
					Util.logException(ex);
				}
			}
		});
        
        try {            
        	//buttonOk.setGraphic(new ImageView(imageCancel));
        } catch (Exception e) {
			Util.logException(e);
        }
        
        buttonOk.setTextAlignment(TextAlignment.CENTER);
        buttonOk.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 35; -fx-background-color: green; -fx-background-radius: 0 0 0 0; -fx-stroke: white; -fx-fill:white;");
        buttonOk.setMinWidth(100);
        buttonOk.setMaxWidth(100);
        buttonOk.setMaxHeight(70);
        buttonOk.setMinHeight(70);
        buttonOk.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					stage.hide();
				} catch (Exception ex) {
					Util.logException(ex);
				}
			}
		});
        
        HBox display = new HBox(0, tF);
        
        HBox displayRoot = new HBox(display);
        HBox.setHgrow(display, Priority.ALWAYS);
        displayRoot.setPadding(new Insets(20, 20, 20, 0));
        
        VBox keyPad = new VBox(15, buttonUp, buttonOk, buttonDown);
        keyPad.setPadding(new Insets(20, 20, 20, 20));
        
        HBox dialogRoot = new HBox(10, displayRoot, display, keyPad);
        dialogRoot.setAlignment(Pos.CENTER);
        dialogRoot.setStyle("-fx-background-color: derive(black, 25%) ; -fx-border-color: black;"
                    + "-fx-background-radius: 20px; -fx-border-radius: 20px; -fx-border-width: 1px;");
        final Scene scene = new Scene(dialogRoot, 300, 280,
                Color.TRANSPARENT);
        enableDragging(scene);
        stage.setScene(scene);
        centerStage(stage, 300, 280);
        

        displayRoot.setAlignment(Pos.CENTER);
        display.setAlignment(Pos.CENTER);
        
        return stage;
    }
	
	@SuppressWarnings("unchecked")
	protected Stage createStatisticsStage(Window owner, int deviceId, String currentTemperature) throws Exception {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        int highTemp = Integer.MIN_VALUE;
        int lowTemp = Integer.MAX_VALUE;
        
        final javafx.scene.chart.CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        //creating the chart
        final LineChart<String, Number> lineChart = 
                new LineChart<String, Number>(xAxis,yAxis);
        
        lineChart.setTitle("Temperature history");
        //defining a series
        XYChart.Series series = new XYChart.Series();        
        
        //loop over highTemps
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
        SimpleDateFormat presentationDateFormat = new SimpleDateFormat("d/M");
        
        for(int i = 13; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
        	cal.add(Calendar.DATE, i*-1);
            String date = dateFormat.format(cal.getTime());
    		int temperature = Util.getTemperatureForDate(deviceId, date).getKey();
            
            if (temperature != Integer.MIN_VALUE) {
        		if (temperature > highTemp) {
        			highTemp = temperature;
        		}
        		if (temperature < lowTemp) {
        			lowTemp = temperature;
        		}
                series.getData().add(new XYChart.Data<String, Integer>(presentationDateFormat.format(cal.getTime()), temperature));
            }
        }
        
        lineChart.getData().add(series);
        
        String color = "crimson";
        final String lineStyle = String.format("-fx-stroke: %s;", color);
        series.getNode().lookup(".chart-series-line").setStyle(lineStyle);
        final String symbolStyle = String.format("-fx-background-color: %s, whitesmoke;", color);
        for (Object data: series.getData())
            ((XYChart.Data<String, Integer>)data).getNode().lookup(".chart-line-symbol").setStyle(symbolStyle);
        
        XYChart.Series series2 = new XYChart.Series();
        
        for(int i = 13; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
        	cal.add(Calendar.DATE, i*-1);
            String date = dateFormat.format(cal.getTime());
    		int temperature = Util.getTemperatureForDate(deviceId, date).getValue();
    		
            if (temperature != Integer.MIN_VALUE) {
        		if (temperature > highTemp) {
        			highTemp = temperature;
        		}
        		if (temperature < lowTemp) {
        			lowTemp = temperature;
        		}
                series2.getData().add(new XYChart.Data<String, Integer>(presentationDateFormat.format(cal.getTime()), temperature));
            }
        }

        lineChart.getData().add(series2);
        color = "royalblue";
        final String lineStyle2 = String.format("-fx-stroke: %s;", color);
        series2.getNode().lookup(".chart-series-line").setStyle(lineStyle2);
        final String symbolStyle2 = String.format("-fx-background-color: %s, whitesmoke;", color);
        for (Object data: series2.getData())
            ((XYChart.Data<String, Integer>)data).getNode().lookup(".chart-line-symbol").setStyle(symbolStyle2);
        
        lineChart.setLegendVisible(false);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(lowTemp -2);
        yAxis.setUpperBound(highTemp +2);
        yAxis.setTickUnit(1);
        
        JFXButton okButton = new JFXButton("Close");
        okButton.setPrefSize(550, 40);
        okButton.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 20; -fx-background-color: #aabbcc;");
        okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					stage.hide();
				} catch (Exception ex) {
					Util.logException(ex);	
				}
			}
		});
        
        JFXButton alarmButton = null;
        if(Util.getAlarmTemperature(deviceId) != Integer.MIN_VALUE) 
		{
        	alarmButton = new JFXButton("Remove alarm");
		} else {
        	alarmButton = new JFXButton("Set alarm", new ImageView(new Image(getClass().getResource("/img/16x16/bell.png").openStream())));
		}
        alarmButton.setPrefSize(550, 40);
        alarmButton.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 20; -fx-background-color: #aabbcc;");
        alarmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					int temperature = Util.getAlarmTemperature(deviceId); //Will reset alarm if not changed
					if(temperature == Integer.MIN_VALUE) 
					{
				        //Get temp
	    				TemperatureStage stage = createTemperatureDialog(okButton.getScene().getWindow(), currentTemperature);
	    				disableScreenUpdate(true);
			            stage.showAndWait();
			            disableScreenUpdate(false);
	    				
	    				temperature = stage.GetTemperature();
					}
    				byte[] buffer = Util.concat(Util.intToByteArray(deviceId), Util.intToByteArray(temperature));
    				
    				BroadcastingClient bC = new BroadcastingClient();
    				bC.broadcastPacket(buffer);
					stage.hide();
				} catch (Exception ex) {
					Util.logException(ex);	
				}
			}
		});
        
        HBox buttonRoot = new HBox(10, alarmButton, okButton);
        
        
        VBox dialogRoot = new VBox(5, lineChart, buttonRoot);
        dialogRoot.setPadding(new Insets(10, 50, 0, 50));
        dialogRoot.setAlignment(Pos.CENTER);
        dialogRoot.setStyle("-fx-background-color: derive(white, 25%) ; -fx-border-color: black;"
                    + "-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
        final Scene scene = new Scene(dialogRoot, 620, 470,
                Color.TRANSPARENT);
        enableDragging(scene);
        stage.setScene(scene);
        centerStage(stage, 620, 470);
        return stage;
    }	
}
