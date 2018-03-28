package application;

import javafx.stage.Stage;

public class PinStage extends Stage {
	private String pin;
	private boolean confirmed;
	
	public void AddPinDigit(char c) {
		if (pin != null) {
			pin = pin + c;
		} else {
			pin = String.valueOf(c);
		}
	}
	
	public void RemoveLastPinDigit() {
		if (pin != null && pin.length() > 0) {
			pin = pin.substring(0, pin.length() -1);
		}
	}
	
	public String GetPin() {
		return pin;
	}
	
	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}
	
	public boolean getConfirmed() {
		return confirmed;
	}
}
