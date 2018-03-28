package zwave.fibaro;

public class ZWaveDevice {
	public enum DeviceTypes {OnOff, RGBDimmable, Door, Scene};
	private int[] devices;
	private String name;
	private DeviceTypes deviceType;
	private boolean state;
	private String imageURL;
	private String reqSystemState;
	
	public ZWaveDevice (String name, DeviceTypes deviceType, int... deviceIds) 
	{
		this.name = name;
		this.deviceType = deviceType;
		this.devices = deviceIds;
	}
	
	public ZWaveDevice (String name, DeviceTypes deviceType, String imageURL, int... deviceIds) 
	{
		this.name = name;
		this.deviceType = deviceType;
		this.imageURL = imageURL;
		this.devices = deviceIds;
	}	
	
	public ZWaveDevice (String name, DeviceTypes deviceType, String imageURL, String reqSystemState, int... deviceIds) 
	{
		this.name = name;
		this.deviceType = deviceType;
		this.imageURL = imageURL;
		this.reqSystemState = reqSystemState;
		this.devices = deviceIds;
	}

	public int[] getDeviceIds() {
		return devices;
	}

	public void setDeviceIds(int[] devices) {
		this.devices = devices;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DeviceTypes getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceTypes deviceType) {
		this.deviceType = deviceType;
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getReqSystemState() {
		return reqSystemState;
	}

	public void setReqSystemState(String reqSystemState) {
		this.reqSystemState = reqSystemState;
	}
}
