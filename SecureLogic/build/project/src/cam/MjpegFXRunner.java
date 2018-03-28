package cam;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import javax.imageio.ImageIO;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * Given an extended JPanel and URL read and create BufferedImages to be displayed from a MJPEG stream
 * @author shrub34 Copyright 2012
 * Free for reuse, just please give me a credit if it is for a redistributed package
 */
public class MjpegFXRunner implements Runnable
{
	private static final String CONTENT_LENGTH = "Content-Length: ";
	private static final String CONTENT_TYPE = "Content-Type: image/jpeg";
	private MJpegFXViewer viewer;
	private InputStream urlStream;
	private StringWriter stringWriter;
	private boolean processing = true;
	
	public MjpegFXRunner(MJpegFXViewer viewer, URL url, String userPass) throws IOException
	{
		this.viewer = viewer;
		URLConnection urlConn = url.openConnection();
		
		if (userPass != null && userPass.length() > 0) {
		    String authStr = Base64.getEncoder()
		    		.encodeToString(userPass.getBytes());
			//setting Authorization header
			urlConn.setRequestProperty("Authorization", "Basic " + authStr);
		}
		
		// change the timeout to taste, I like 1 second
		urlConn.setReadTimeout(10000);
		urlConn.connect();
		urlStream = urlConn.getInputStream();
		stringWriter = new StringWriter(128);
	}

	/**
	 * Stop the loop, and allow it to clean up
	 */
	public synchronized void stop()
	{
		processing = false;
	}
	
	/**
	 * Keeps running while process() returns true
	 * 
	 * Each loop asks for the next JPEG image and then sends it to our JPanel to draw
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		while(processing)
		{
			try
			{
				byte[] imageBytes = retrieveNextImage();
				ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
				
				BufferedImage image = ImageIO.read(bais);
		        
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

		        
				viewer.setBufferedImage(wr);
				
				viewer.requestLayout();
			}catch(Exception e){
				e.printStackTrace();
				viewer.requestLayout();
				stop();
			}
		}
		
		// close streams
		try
		{
			urlStream.close();
		}catch(IOException ioe){
			System.err.println("Failed to close the stream: " + ioe);
		}
	}
	
	public byte[] getImage() 
	{
		try {
		return retrieveNextImage();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Using the urlStream get the next JPEG image as a byte[]
	 * @return byte[] of the JPEG
	 * @throws IOException
	 */
	private byte[] retrieveNextImage() throws IOException
	{
		boolean almostHaveHeader = false; 
		boolean haveHeader = false;
		int currByte = -1;
		
		String header = null;
		// build headers
		// the DCS-930L stops it's headers
		while((currByte = urlStream.read()) > -1 && !haveHeader)
		{
			stringWriter.write(currByte);
			
			String tempString = stringWriter.toString(); 
			int indexOf = tempString.indexOf(CONTENT_LENGTH);
			if(indexOf > 0)
			{
				almostHaveHeader = true;
			}
			
			if (almostHaveHeader && tempString.endsWith("\r\n")) 
			{
				haveHeader = true;
				header = tempString;				
			}
		}		
		
		// 255 indicates the start of the jpeg image
		while((urlStream.read()) != 255)
		{
			// just skip extras
		}
		
		// rest is the buffer
		int contentLength = contentLength(header);
		byte[] imageBytes = new byte[contentLength + 1];
		// since we ate the original 255 , shove it back in
		imageBytes[0] = (byte)255;
		int offset = 1;
		int numRead = 0;
		while (offset < imageBytes.length
			&& (numRead=urlStream.read(imageBytes, offset, imageBytes.length-offset)) >= 0) 
		{
			offset += numRead;
		}       
		
		stringWriter = new StringWriter(128);
		
		return imageBytes;
	}

	// dirty but it works content-length parsing
	private static int contentLength(String header)
	{
		int indexOfContentLength = header.indexOf(CONTENT_LENGTH);
		int valueStartPos = indexOfContentLength + CONTENT_LENGTH.length();
		int indexOfEOL = header.indexOf('\n', indexOfContentLength);
		
		String lengthValStr = header.substring(valueStartPos, indexOfEOL).trim();
		
		int retValue = Integer.parseInt(lengthValStr);
		
		return retValue;
	}
}