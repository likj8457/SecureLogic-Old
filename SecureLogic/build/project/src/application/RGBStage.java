package application;

import javafx.stage.Stage;

public class RGBStage extends Stage {
	private String color;
	private int level;
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public String getColor() {
		return color;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
}
