package Tab;

import application.Util;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import zwave.fibaro.ZWaveDevice;

public class IrrigationTab extends ZWaveDeviceTab {
	public IrrigationTab () {
		try {
			Image irrigationIcon = new Image(getClass().getResource("/img/48x48/irrigation.png").openStream());
			setGraphic(new ImageView(irrigationIcon));
	    } catch (Exception e) {
	    	Util.logException(e);
	    }
		
		try {
			deviceOn = new Image(getClass().getResource("/img/40x40/wateringCanOn.png").openStream());
	    } catch (Exception e) {
	    	Util.logException(e);
	    }
		
	    try {
	    	deviceOff = new Image(getClass().getResource("/img/40x40/wateringCanOff.png").openStream());
		} catch (Exception e) {
			Util.logException(e);
		}
		
	    devices.add(new ZWaveDevice("  Backyard", ZWaveDevice.DeviceTypes.OnOff, new int[]{389, 390}));
		devices.add(new ZWaveDevice("  East", ZWaveDevice.DeviceTypes.OnOff, new int[]{349}));
		devices.add(new ZWaveDevice("  Frontyard", ZWaveDevice.DeviceTypes.OnOff, new int[]{372}));
		devices.add(new ZWaveDevice("  Hedge", ZWaveDevice.DeviceTypes.OnOff, new int[]{350}));
		devices.add(new ZWaveDevice("  Flowerbed", ZWaveDevice.DeviceTypes.OnOff, new int[]{355, 356}));
		devices.add(new ZWaveDevice("  Lawn 30m", ZWaveDevice.DeviceTypes.Scene, new int[]{364}));
		devices.add(new ZWaveDevice("  Lawn 1h", ZWaveDevice.DeviceTypes.Scene, new int[]{368}));
		devices.add(new ZWaveDevice("  Lawn 2h", ZWaveDevice.DeviceTypes.Scene, new int[]{366}));
		
	    initialize();
	}
}
