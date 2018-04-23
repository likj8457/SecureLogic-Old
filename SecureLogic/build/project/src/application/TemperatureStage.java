package application;

import javafx.stage.Stage;

public class TemperatureStage extends Stage {
	private int temperature;
	
	public void setTemperature(int temp) {
		temperature = temp;
	}
	
	public int GetTemperature() {
		return temperature;
	}
}
