package cam;

public class SecureCamera {
	private String stillImageUrl;
	private String cameraFeedUrl;
	private String userName;
	private String password;
	
	public SecureCamera (String cameraFeedUrl, String strillUrl) {
		this.setCameraFeedUrl(cameraFeedUrl);
		this.setStillImageUrl(strillUrl);
	}
	
	public SecureCamera(String cameraFeedUrl, String strillUrl, String userName, String password) {
		this.setCameraFeedUrl(cameraFeedUrl);
		this.setStillImageUrl(strillUrl);
		this.setUserName(userName);
		this.setPassword(password);		
	}
	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getStillImageUrl() {
		return stillImageUrl;
	}

	public void setStillImageUrl(String stillImageUrl) {
		this.stillImageUrl = stillImageUrl;
	}

	public String getCameraFeedUrl() {
		return cameraFeedUrl;
	}

	public void setCameraFeedUrl(String cameraFeedUrl) {
		this.cameraFeedUrl = cameraFeedUrl;
	}
}
