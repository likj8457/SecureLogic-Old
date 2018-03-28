package cam;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class MJpegFXViewer extends Pane {
    private final Canvas canvas;
    private static Image img = null;
    private boolean painting = false;

    public MJpegFXViewer() {
        canvas = new Canvas(getWidth(), getHeight());
        getChildren().add(canvas);
        widthProperty().addListener(e -> canvas.setWidth(getWidth()));
        heightProperty().addListener(e -> canvas.setHeight(getHeight()));
    }
    
    public void setBufferedImage(Image newImg)
    {
	    img = newImg;
	    requestLayout();
    }
    
    public boolean isPainting() {
    	synchronized(this) {
    		return painting;
    	}
    }

    @Override
    protected void layoutChildren() {
    	synchronized (this) {
    		painting = true;
    	}
        super.layoutChildren();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Paint your custom image here:
        gc.drawImage(img, 0,0, 600, 400);
        synchronized(this) {
        	painting = false;
    	}
    }
}