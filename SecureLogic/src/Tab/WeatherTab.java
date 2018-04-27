package Tab;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.jfoenix.controls.JFXButton;

import application.TimeService;
import application.Util;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import weather.WeatherObject;
import weather.WeatherObject.WeatherObjectTypes;
import weather.WeatherService;
import zwave.fibaro.HC2Interface;

public class WeatherTab extends SecureTab {

	private WeatherService weatherService;

	private ImageView outdoorClimateImg;
	private Text outdoorTemperatureTxt;
	private Text outdoorDescTxt1;
	private Group outdoorBlend;
	private ImageView outdoorAlarmIcon;

	private Text indoorTemperatureTxt;
	private Text indoorDescTxt1;
	private Group indoorBlend;
	private ImageView indoorAlarmIcon;

	private ImageView[] climateImg = new ImageView[5];
	private Text[] dayTemp = new Text[5];
	private int[] dayOfMonthAtIndex = new int[5];
	private Text[] forecastDay = new Text[5];

	private static final String DEGREE = "\u00b0";

	public WeatherTab() {
		weatherService = new WeatherService();

		try {
			Image weatherIcon = new Image(getClass().getResource("/img/48x48/broken-clouds-day.png").openStream());
			setGraphic(new ImageView(weatherIcon));
		} catch (Exception e) {
			Util.logException(e);
		}

		try {
			Image icon = new Image(getClass().getResource("/img/16x16/bell.png").openStream());
			indoorAlarmIcon = new ImageView(icon);
			indoorAlarmIcon.setBlendMode(BlendMode.SRC_ATOP);
			outdoorAlarmIcon = new ImageView(icon);
			outdoorAlarmIcon.setBlendMode(BlendMode.SRC_ATOP);
		} catch (Exception e) {
			Util.logException(e);
		}

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(0);
		grid.setMinHeight(-1);
		grid.setMinWidth(720);
		grid.setPrefHeight(-1);
		grid.setPrefWidth(-1);
		grid.setAlignment(Pos.CENTER);
		// grid.setStyle("-fx-background-color: white; -fx-grid-lines-visible: true");

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(50);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(50);
		grid.getColumnConstraints().addAll(col1, col2);

		// Top spacing
		Pane pane1 = new Pane();
		pane1.minHeight(40);
		grid.add(pane1, 0, 0, 2, 1);
		GridPane.setVgrow(pane1, Priority.ALWAYS);

		// Time
		Text timeTxt = new Text();
		timeTxt.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 110;");
		grid.add(timeTxt, 0, 1, 2, 1);
		GridPane.setHalignment(timeTxt, HPos.CENTER);

		// Date
		Text dateTxt = new Text();
		dateTxt.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
		grid.add(dateTxt, 0, 2, 2, 1);
		GridPane.setHalignment(dateTxt, HPos.CENTER);

		TimeService.subscribeToTimeUpdates(timeTxt, dateTxt);

		// Spacing
		Pane pane2 = new Pane();
		pane2.minHeight(30);
		pane2.maxHeight(30);
		grid.add(pane2, 0, 3, 2, 1);
		GridPane.setVgrow(pane2, Priority.ALWAYS);

		// Outdoor climate
		GridPane grid2 = new GridPane();
		grid2.setHgap(20);
		grid2.setAlignment(Pos.CENTER);

		try {
			Image oCImg = new Image(getClass().getResource("/img/70x70/clear-day.png").openStream());
			outdoorClimateImg = new ImageView(oCImg);
			outdoorBlend = new Group(outdoorClimateImg);
		} catch (Exception e) {
			Util.logException(e);
		}
		grid2.add(outdoorBlend, 0, 0);
		outdoorTemperatureTxt = new Text();
		grid2.add(outdoorTemperatureTxt, 1, 0);
		grid2.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			try {
				Stage stage = createStatisticsStage(grid2.getScene().getWindow(), 341, outdoorTemperatureTxt.getText());
				disableScreenUpdate(true);
				stage.showAndWait();
				disableScreenUpdate(false);
			} catch (Exception ex) {
				Util.logException(ex);
			}
		});

		grid.add(grid2, 0, 4);

		outdoorDescTxt1 = new Text();
		grid.add(outdoorDescTxt1, 0, 5);
		GridPane.setHalignment(outdoorDescTxt1, HPos.CENTER);

		// Indoor climate
		GridPane grid3 = new GridPane();
		grid3.setHgap(20);
		grid3.setAlignment(Pos.CENTER);
		try {
			Image iCImg = new Image(getClass().getResource("/img/70x70/house.png").openStream());
			ImageView indoorClimateImg = new ImageView(iCImg);
			indoorBlend = new Group(indoorClimateImg);
		} catch (Exception e) {
			Util.logException(e);
		}
		grid3.add(indoorBlend, 0, 0);
		indoorTemperatureTxt = new Text();
		grid3.add(indoorTemperatureTxt, 1, 0);
		grid3.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			try {
				Stage stage = createStatisticsStage(grid3.getScene().getWindow(), 436, indoorTemperatureTxt.getText());
				disableScreenUpdate(true);
				stage.showAndWait();
				disableScreenUpdate(false);
			} catch (Exception ex) {
				Util.logException(ex);
			}
		});
		grid.add(grid3, 1, 4);

		indoorDescTxt1 = new Text();
		grid.add(indoorDescTxt1, 1, 5);
		GridPane.setHalignment(indoorDescTxt1, HPos.CENTER);

		// Spacing
		Pane pane3 = new Pane();
		pane3.minHeight(40);
		grid.add(pane3, 0, 6, 2, 1);
		GridPane.setVgrow(pane3, Priority.ALWAYS);

		// Forecasts
		GridPane grid4 = new GridPane();
		grid4.setHgap(10);
		grid4.setPrefWidth(10);
		grid4.setAlignment(Pos.CENTER);

		ColumnConstraints col41 = new ColumnConstraints();
		col41.setHalignment(HPos.CENTER);
		col41.setHgrow(Priority.ALWAYS);
		col41.setPercentWidth(20);
		ColumnConstraints col42 = new ColumnConstraints();
		col42.setHalignment(HPos.CENTER);
		col42.setHgrow(Priority.ALWAYS);
		col42.setPercentWidth(20);
		ColumnConstraints col43 = new ColumnConstraints();
		col43.setHalignment(HPos.CENTER);
		col43.setHgrow(Priority.ALWAYS);
		col43.setPercentWidth(20);
		ColumnConstraints col44 = new ColumnConstraints();
		col44.setHalignment(HPos.CENTER);
		col44.setHgrow(Priority.ALWAYS);
		ColumnConstraints col45 = new ColumnConstraints();
		col45.setHalignment(HPos.CENTER);
		col45.setHgrow(Priority.ALWAYS);
		col45.setPercentWidth(20);
		grid4.getColumnConstraints().addAll(col41, col42, col43, col44, col45);

		// Forecasts
		GridPane[] grid4x = new GridPane[5];
		for (int i = 0; i < 5; i++) {
			grid4x[i] = new GridPane();
			grid4x[i].setHgap(20);
			grid4x[i].setAlignment(Pos.CENTER);
			try {
				Image innerImg = new Image(getClass().getResource("/img/50x50/clear-day.png").openStream());
				climateImg[i] = new ImageView(innerImg);
			} catch (Exception e) {
				Util.logException(e);
			}
			grid4x[i].add(climateImg[i], 0, 0);
			dayTemp[i] = new Text();
			grid4x[i].add(dayTemp[i], 1, 0);
			grid4.add(grid4x[i], i, 0);
			forecastDay[i] = new Text();
			grid4.add(forecastDay[i], i, 1); 
			final int finalI = i;
			grid4x[i].addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
				try {
					Stage stage = createForecastStage(grid2.getScene().getWindow(), finalI);
					disableScreenUpdate(true);
					stage.showAndWait();
					disableScreenUpdate(false);
				} catch (Exception ex) {
					Util.logException(ex);
				}
			});
		}

		grid.add(grid4, 0, 7, 2, 1);

		// Spacing
		Pane pane4 = new Pane();
		pane4.minHeight(20);
		grid.add(pane4, 0, 8, 2, 1);
		GridPane.setVgrow(pane4, Priority.ALWAYS);

		setContent(grid);

		setWeatherCSS();
	}

	public void UpdateTemperatures() {		
		try {
			outdoorTemperatureTxt.setText(HC2Interface.getTempDeviceStatus(341));
		} catch (Exception e) {
			Util.logException(e);
		}
		if (Util.getAlarmTemperature(341) != Integer.MIN_VALUE
				&& !outdoorBlend.getChildren().contains(outdoorAlarmIcon)) {
			Platform.runLater(() -> {
				try {
					outdoorBlend.getChildren().add(outdoorAlarmIcon);
				} catch (Exception e) {
					Util.logException(e);
				}
			});
		} else if (Util.getAlarmTemperature(341) == Integer.MIN_VALUE
				&& outdoorBlend.getChildren().contains(outdoorAlarmIcon)) {
			Platform.runLater(() -> {
				try {
					outdoorBlend.getChildren().remove(outdoorAlarmIcon);
				} catch (Exception e) {
					Util.logException(e);
				}
			});
		}

		try {
			indoorTemperatureTxt.setText(HC2Interface.getTempDeviceStatus(436));
		} catch (Exception e) {
			Util.logException(e);
		}
		indoorDescTxt1.setText("Livingroom");
		if (Util.getAlarmTemperature(436) != Integer.MIN_VALUE
				&& !indoorBlend.getChildren().contains(indoorAlarmIcon)) {
			Platform.runLater(() -> {
				try {
					indoorBlend.getChildren().add(indoorAlarmIcon);
				} catch (Exception e) {
					Util.logException(e);
				}
			});
		} else if (Util.getAlarmTemperature(436) == Integer.MIN_VALUE
				&& indoorBlend.getChildren().contains(indoorAlarmIcon)) {
			Platform.runLater(() -> {
				try {
					indoorBlend.getChildren().remove(indoorAlarmIcon);
				} catch (Exception e) {
					Util.logException(e);
				}
			});
		}
	}

	public void updateWeather() {
		weatherService.UpdateForecastProvider();
		
		try {
			WeatherObject cWO = weatherService.getCurrentWeather();
			List<WeatherObject> fWList = weatherService.getCondensedForecast();
			List<WeatherObject> fWTarget = new ArrayList<WeatherObject>();
			// Sort the list low to high.. = 1,2,3,30,31
			Collections.sort(fWList, (w1, w2) -> w1.DayOfMonth.compareTo(w2.DayOfMonth));
			// Sort the list in coming day order... 30th might be closer than the 1st of
			// next month for instance... = 30,31,1,2,3 (if today is 30)
			int last = fWList.get(0).DayOfMonth;
			for (int i = 0; i < fWList.size(); i++) {
				WeatherObject currentO = fWList.get(i);
				if (currentO.DayOfMonth != last + 1 && currentO.DayOfMonth > last) {
					// Ohoh... We need to move this item up.. //This will be the first of the "big
					// numbers 30 from the example above
					if (fWTarget.size() > 0) {
						for (int j = 0; j < fWTarget.size(); j++) {
							if (currentO.DayOfMonth > fWTarget.get(j).DayOfMonth) {
								fWTarget.add(j, currentO);
								break;
							}
						}
					} else {
						fWTarget.add(0, currentO);
					}
				} else {
					int j = 0;
					for (; j < fWTarget.size(); j++) {
						if (fWTarget.get(j).DayOfMonth + 1 == currentO.DayOfMonth) {
							j++;// When we have found the right index, increase it and break to get the right
								// index for the new item
							break;
						}
					}
					fWTarget.add(j, currentO);
				}
				last = currentO.DayOfMonth;
			}

			if (getTabPane() != null && getTabPane().getSelectionModel().getSelectedIndex() == 0) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						try {
							outdoorClimateImg.setImage(cWO.Icon);
							outdoorDescTxt1.setText(cWO.WindSpeed + "m/s - " + cWO.Description);

							Date toDay = new Date(System.currentTimeMillis());
							Calendar toDayCal = Calendar.getInstance();
							toDayCal.setTime(toDay);
							Calendar tempCal = Calendar.getInstance();
							Calendar tomorrowCal = Calendar.getInstance();
							tomorrowCal.setTime(toDay);
							tomorrowCal.add(Calendar.DAY_OF_YEAR, 1);

							final int year = toDayCal.get(Calendar.YEAR);
							final int month = toDayCal.get(Calendar.MONTH);
							final int day = toDayCal.get(Calendar.DAY_OF_MONTH);

							for (int index = 0; index < fWTarget.size(); index++) {
								WeatherObject item = fWTarget.get(index);

								if (item != null) {
									if (index < 5) {
										climateImg[index].setImage(item.Icon);
										dayTemp[index].setText(item.OutdoorTemperature + DEGREE);
										dayOfMonthAtIndex[index] = item.DayOfMonth;
										
										String dayName = "?";

										if (day == item.DayOfMonth) {
											dayName = "Today";
										} else if (tomorrowCal.get(Calendar.DAY_OF_MONTH) == item.DayOfMonth) {
											dayName = "Tomorrow";
										} else if (item.DayOfMonth < day) {
											int tempMonth = month;
											int tempYear = year;
											if (month == 11) {
												tempMonth = 0;
												tempYear++;
											} else {
												tempMonth++;
											}
											tempCal.set(tempYear, tempMonth, item.DayOfMonth);
											dayName = WeatherService.WEEKDAYS[tempCal.get(Calendar.DAY_OF_WEEK) - 1];
										} else {
											tempCal.set(year, month, item.DayOfMonth);
											dayName = WeatherService.WEEKDAYS[tempCal.get(Calendar.DAY_OF_WEEK) - 1];
										}
										forecastDay[index].setText(dayName);
									}
								}

							}

						} catch (Exception e) {
							Util.logException(e);
						}
					}
				});
			}
		} catch (Exception e) {
			Util.logException(e);
		}
	}

	private void setWeatherCSS() {
		outdoorTemperatureTxt.setTextAlignment(TextAlignment.CENTER);
		outdoorTemperatureTxt.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 70;");

		outdoorDescTxt1.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
		outdoorDescTxt1.setTextAlignment(TextAlignment.CENTER);

		indoorTemperatureTxt.setTextAlignment(TextAlignment.CENTER);
		indoorTemperatureTxt.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 70;");

		indoorDescTxt1.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
		indoorDescTxt1.setTextAlignment(TextAlignment.CENTER);

		// Forecasts - 5 days
		for (int i = 0; i < 5; i++) {
			dayTemp[i].setTextAlignment(TextAlignment.CENTER);
			dayTemp[i].setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30;");
			forecastDay[i].setTextAlignment(TextAlignment.CENTER);
			forecastDay[i].setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 20;");
		}
	}

	private Stage createForecastStage(Window owner, int dayIndex) throws Exception {
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
		col1.setHalignment(HPos.RIGHT);
		col1.setHgrow(Priority.ALWAYS);
		col1.setPercentWidth(20);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHalignment(HPos.CENTER);
		col2.setHgrow(Priority.ALWAYS);
		col2.setPercentWidth(10);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setHalignment(HPos.CENTER);
		col3.setHgrow(Priority.ALWAYS);
		col3.setPercentWidth(15);
		ColumnConstraints col4 = new ColumnConstraints();
		col4.setHalignment(HPos.LEFT);
		col4.setHgrow(Priority.ALWAYS);
		col4.setPercentWidth(20);
		ColumnConstraints col5 = new ColumnConstraints();
		col5.setHalignment(HPos.LEFT);
		col5.setHgrow(Priority.ALWAYS);
		col5.setPercentWidth(15);
		ColumnConstraints col6 = new ColumnConstraints();
		col6.setHalignment(HPos.LEFT);
		col6.setHgrow(Priority.ALWAYS);
		col6.setPercentWidth(25);
		root.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6);

		Date toDay = new Date(System.currentTimeMillis());
		Calendar dayCal = Calendar.getInstance();
		dayCal.setTime(toDay);
		int todayDayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH);
		if (todayDayOfMonth > dayOfMonthAtIndex[dayIndex]) {
			dayCal.add(Calendar.MONTH, 1);
		}
		dayCal.set(Calendar.DAY_OF_MONTH, dayOfMonthAtIndex[dayIndex]);

		final int day = dayOfMonthAtIndex[dayIndex];
		final String weekDay = WeatherService.WEEKDAYS[dayCal.get(Calendar.DAY_OF_WEEK) - 1];

		HashMap<Integer, WeatherObject> sortedForecastList = new HashMap<>(); // Key is time, value is data
		List<WeatherObject> forecastList = weatherService.getFullForecast();
		for (WeatherObject wO : forecastList) {
			if (wO.DayOfMonth == day) {
				if (!sortedForecastList.containsKey(wO.Hour)
						|| (sortedForecastList.get(wO.Hour).Type == WeatherObjectTypes.Forecast
								&& wO.Type == WeatherObjectTypes.Observation))
					sortedForecastList.put(wO.Hour, wO);
			}
		}

		// Caption
		Text caption = new Text("Forecast for " + weekDay);
		caption.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 40;");
		GridPane.setHalignment(caption, HPos.CENTER);
		root.add(caption, 0, 0, 6, 1);

		// Headers
		Text timeHeader = new Text("");
		timeHeader.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 25;");
		root.add(timeHeader, 0, 1);
		Text tempHeader = new Text("C" + DEGREE);
		tempHeader.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 25;");
		root.add(tempHeader, 1, 1);
		Text imgHeader = new Text("");
		imgHeader.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 25;");
		root.add(imgHeader, 2, 1);
		Text windHeader = new Text("Wind");
		windHeader.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 25;");
		root.add(windHeader, 3, 1);
		Text rainHeader = new Text("Rain");
		rainHeader.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 25;");
		root.add(rainHeader, 4, 1);
		Text descHeader = new Text("Desc");
		descHeader.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 25;");
		root.add(descHeader, 5, 1);

		for (int hour : sortedForecastList.keySet()) {
			WeatherObject wO = sortedForecastList.get(hour);
			int row = weatherService.getRowIndexForWeatherObject(wO) + 2;
			//String timeString = hour < 10 ? "0" + String.valueOf(hour) + ":00" : String.valueOf(hour) + ":00";

			Text time = new Text(weatherService.getRowHeaderForWeatherObject(wO));
			time.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 25;");
			root.add(time, 0, row);
			Text temp = new Text(wO.OutdoorTemperature + DEGREE);
			temp.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 25;");
			root.add(temp, 1, row);
			root.add(new ImageView(wO.Icon), 2, row);
			Text wind = new Text(wO.WindSpeed + " m/s");
			wind.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 25;");
			root.add(wind, 3, row);
			Text rain = new Text(wO.Rain != null && wO.Rain.length() > 0 ? wO.Rain + " mm" : "0" + " mm");
			rain.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 25;");
			root.add(rain, 4, row);
			Text desc = new Text(wO.Description);
			desc.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 20;");
			root.add(desc, 5, row);
		}

		JFXButton okButton = new JFXButton("Close");
		okButton.setPrefSize(550, 40);
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
		GridPane.setHalignment(okButton, HPos.CENTER);
		root.add(okButton, 0, 7, 6, 1);
		int height = (sortedForecastList.size() + 3) * 60;
		final Scene scene = new Scene(root, 620, height, Color.TRANSPARENT);
		enableDragging(scene);
		stage.setScene(scene);
		centerStage(stage, 620, height);
		return stage;
	}
}
