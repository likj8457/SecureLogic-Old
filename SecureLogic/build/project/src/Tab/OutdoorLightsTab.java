package Tab;

import application.Util;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import zwave.fibaro.ZWaveDevice;

public class OutdoorLightsTab extends ZWaveDeviceTab {
	public OutdoorLightsTab() {
		try {
			Image indoorLightingIcon = new Image(getClass().getResource("/img/48x48/streetlight.png").openStream());
			setGraphic(new ImageView(indoorLightingIcon));
	    } catch (Exception e) {
	    	Util.logException(e);
	    }
		
		try {
			deviceOn = new Image(getClass().getResource("/img/40x40/lightbulb_on.png").openStream());
	    } catch (Exception e) {
	    	Util.logException(e);
	    }
		
	    try {
	    	deviceOff = new Image(getClass().getResource("/img/40x40/lightbulb_off.png").openStream());
		} catch (Exception e) {
			Util.logException(e);
		}
		
	    devices.add(new ZWaveDevice("  Patio", ZWaveDevice.DeviceTypes.RGBDimmable, new int[]{312}));
		devices.add(new ZWaveDevice("  Garage", ZWaveDevice.DeviceTypes.RGBDimmable, new int[]{412}));
		devices.add(new ZWaveDevice("  Wall", ZWaveDevice.DeviceTypes.OnOff, new int[]{490}));
		
	    initialize();
	}
}
