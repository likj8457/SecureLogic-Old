package Tab;

import application.Util;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import zwave.fibaro.ZWaveDevice;

public class IndoorLightsTab extends ZWaveDeviceTab {
	public IndoorLightsTab () {
		try {
			Image indoorLightingIcon = new Image(getClass().getResource("/img/48x48/lamp.png").openStream());
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
		
	    devices.add(new ZWaveDevice("  Kitchen", ZWaveDevice.DeviceTypes.OnOff, new int[]{389, 390}));
		devices.add(new ZWaveDevice("  Kitchen table", ZWaveDevice.DeviceTypes.OnOff, new int[]{349}));
		devices.add(new ZWaveDevice("  Kitchen sink", ZWaveDevice.DeviceTypes.RGBDimmable, new int[]{372}));
		devices.add(new ZWaveDevice("  L-room general", ZWaveDevice.DeviceTypes.OnOff, new int[]{350}));
		devices.add(new ZWaveDevice("  Hallway", ZWaveDevice.DeviceTypes.OnOff, new int[]{355, 356}));
		devices.add(new ZWaveDevice("  Big lights", ZWaveDevice.DeviceTypes.OnOff, new int[]{364}));
		devices.add(new ZWaveDevice("  Bedroom tree", ZWaveDevice.DeviceTypes.OnOff, new int[]{368}));
		devices.add(new ZWaveDevice("  Glass cabinet", ZWaveDevice.DeviceTypes.OnOff, new int[]{366}));;
		
	    initialize();
	}
}
