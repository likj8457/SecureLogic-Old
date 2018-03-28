package application;

import javafx.stage.Stage;

public class ConfirmStage extends Stage {
	private boolean confirmed;
	
	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}
	
	public boolean getConfirmed() {
		return confirmed;
	}
}
