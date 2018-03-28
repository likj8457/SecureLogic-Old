package Tab;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXButton.ButtonType;

import application.Util;
import cam.MJpegFXViewer;
import cam.MjpegFXRunner;
import cam.SecureCamera;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class CameraTab extends SecureTab {
	private HashMap<SecureCamera, JFXButton> cameras = new HashMap<SecureCamera, JFXButton>();
	private long lastThumbUpdate;
	
	public CameraTab() {
		try {
			Image lockIcon = new Image(getClass().getResource("/img/48x48/security-camera.png").openStream());
			setGraphic(new ImageView(lockIcon));
	    } catch (Exception e) {
	    	Util.logException(e);
	    }

		cameras.put(new SecureCamera("http://192.168.0.101/axis-cgi/mjpg/video.cgi?resolution=640x400", "http://192.168.0.101/axis-cgi/jpg/image.cgi?camera=1&resolution=160x120&compression=50"), new JFXButton());
		cameras.put(new SecureCamera("http://192.168.0.102/video2.mjpg", "http://192.168.0.102/image/jpeg.cgi", "admin", ""), new JFXButton(" "));
		
	    initialize();
	}
	
	private void initialize() {
		GridPane grid = new GridPane();
	    grid.setHgap(30);
	    grid.setVgap(0);
	    grid.setMinHeight(-1);
	    grid.setMinWidth(720);
	    grid.setPrefHeight(-1);
	    grid.setPrefWidth(-1);
	    grid.setAlignment(Pos.CENTER);
        //grid.setStyle("-fx-background-color: white; -fx-grid-lines-visible: true");
		
	    ColumnConstraints col1 = new ColumnConstraints();
	    col1.setHalignment(HPos.LEFT);
	    col1.setHgrow(Priority.ALWAYS);
        col1.setPercentWidth(0);
        ColumnConstraints col2 = new ColumnConstraints();
	    col2.setHalignment(HPos.CENTER);
	    col2.setHgrow(Priority.ALWAYS);
        col2.setPercentWidth(50);    
        ColumnConstraints col3= new ColumnConstraints();
	    col3.setHalignment(HPos.CENTER);
	    col3.setHgrow(Priority.ALWAYS);
        col3.setPercentWidth(50);
        ColumnConstraints col4 = new ColumnConstraints();
	    col4.setHalignment(HPos.LEFT);
	    col4.setHgrow(Priority.ALWAYS);
        col4.setPercentWidth(0);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);
        
	    //Left spacing
	    Pane pane1 = new Pane();
	    pane1.minHeight(10);
	    grid.add(pane1, 0, 0, 1, 1);
	    GridPane.setVgrow(pane1, Priority.ALWAYS);
	    
	    int index = 0;
	    for (SecureCamera camera : cameras.keySet()) {
	    	    	
	    	JFXButton button = cameras.get(camera);
	    	
	    	button.setMaxSize(150, 200);
	    	button.setPrefSize(150, 200);
	    	button.setAlignment(Pos.CENTER);
	    	button.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 30; -fx-background-color: #aabbcc;");
	    	button.setButtonType(ButtonType.RAISED);
	    	
	    	grid.add(button, index+1, 1, 1, 1);
	    	
	    	String tempUserPass = null;
			if(camera.getUserName() != null) {
				tempUserPass = camera.getUserName().trim();
			}
			
			if (camera.getPassword() != null) {
				tempUserPass += ":" + camera.getPassword();
			}
			
			final String userPass = tempUserPass;
			
			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent e) {
					try {
						Stage stage = createVideoDialog(button.getScene().getWindow(), camera.getCameraFeedUrl(), userPass);
						disableScreenUpdate(true);
			            stage.showAndWait();
			            disableScreenUpdate(false);
					} catch (Exception ex) {
						Util.logException(ex);
					}
				}
			});
	    
			index++;			
	    }
	    
	    //Middle spacing
	    Pane pane2 = new Pane();
	    pane2.minHeight(30);
	    grid.add(pane2, 0, 2, 1, 1);
	    GridPane.setVgrow(pane2, Priority.ALWAYS);
	    
	    setContent(grid);
	}
	
	public void Update() {
    	//Thumbs on cam buttons, only once per minute
		if (System.currentTimeMillis() - lastThumbUpdate > 60000) {
			lastThumbUpdate = System.currentTimeMillis();
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					for(SecureCamera camera : cameras.keySet()) {
						String userPass = null;
						if(camera.getUserName() != null) {
							userPass = camera.getUserName().trim();
						}
						
						if (camera.getPassword() != null) {
							userPass += ":" + camera.getPassword();
						}
						
						ImageView iW1 = ReadCamImageForButton(camera.getStillImageUrl(), userPass);
						cameras.get(camera).setGraphic(iW1);
					}
				}
			});
		}
	}
	
	private ImageView ReadCamImageForButton (String url, String credentials) {
		try {
			URLConnection urlConn = new URL(url).openConnection();
			
			if (credentials != null && credentials.length() > 0) {
			    String authStr = Base64.getEncoder()
			    		.encodeToString(credentials.getBytes());
				//setting Authorization header
				urlConn.setRequestProperty("Authorization", "Basic " + authStr);
			}
			
			
			// change the timeout to taste, I like 1 second
			urlConn.setReadTimeout(10000);
			urlConn.connect();
			InputStream urlStream = urlConn.getInputStream();
			
			BufferedImage image = resize(ImageIO.read(urlStream), 200, 200);
			WritableImage wr = null;
	        if (image != null) {
	            wr = new WritableImage(image.getWidth(), image.getHeight());
	            
	            PixelWriter pw = wr.getPixelWriter();
	            for (int x = 0; x < image.getWidth(); x++) {
	                for (int y = 0; y < image.getHeight(); y++) {
	                    pw.setArgb(x, y, image.getRGB(x, y));
	                }
	            }
	        }
	        ImageView iV = new ImageView();
	        iV.setImage(wr);
			return iV;
			
		} catch (Exception e) {
			Util.logException(e);
		}	
		return null;
	}
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
	    java.awt.Image tmp = img.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    java.awt.Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}
	
	private Stage createVideoDialog(Window owner, String url, String userPass) throws Exception {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

    	MJpegFXViewer b = new MJpegFXViewer();
		final MjpegFXRunner r = new MjpegFXRunner(b, new URL(url), userPass);
		(new Thread(r)).start();
		b.setPrefSize(600,  400);
        
        JFXButton okButton = new JFXButton("Close");
        okButton.setPrefSize(550, 40);
        okButton.setStyle("-fx-font-family: 'Roboto Thin'; -fx-font-size: 20; -fx-background-color: #aabbcc;");
        okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				try {
					stage.hide();
					r.stop();
				} catch (Exception ex) {
					Util.logException(ex);	
				}
			}
		});
        VBox dialogRoot = new VBox(5, b, okButton);
        dialogRoot.setPadding(new Insets(10, 50, 0, 50));
        dialogRoot.setAlignment(Pos.CENTER);
        dialogRoot.setStyle("-fx-background-color: derive(white, 25%) ; -fx-border-color: black;"
                    + "-fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
        final Scene scene = new Scene(dialogRoot, 620, 470,
                Color.TRANSPARENT);
        enableDragging(scene);
        stage.setScene(scene);
        centerStage(stage, 620, 470);
        return stage;
    }
}