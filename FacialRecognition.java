import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class FacialRecognition
{
	public static void main(String[] args) throws InterruptedException, IOException 
	{	    
		FacialRecognition driver = new FacialRecognition();
		driver.start();
	}
	
	public void start() throws InterruptedException
	{
		//Load OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		ImageDisplay displayFrame = createDisplayFrame();
		
		try
		{
			capture(displayFrame);
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		//Addition ideas:
/*
		Rescan captured faces to reduce error
		for (int i = 0; i < captures.length; i++)
			if (!faceFound)
				deleteFile(file);
*/
		
		//Open 'captures' directory to allow naming on exit
	}
	
	private ImageDisplay createDisplayFrame()
	{
		//Local variables
		JFrame frame = new JFrame();
	    ImageDisplay display = new ImageDisplay();
	    
	    //Set frame preferences
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setTitle("Facial Detection");
	    frame.setSize(680, 540);
	    frame.setContentPane(display);
	    frame.setVisible(true);
	    
	    return display;
	}
		
	public void capture(ImageDisplay display) throws InterruptedException, IOException
	{
		//Local variables
	    Mat rawImage = new Mat();
	    Mat newImage = new Mat();
	    BufferedImage convertedImage;
	    MatToImg converter = new MatToImg();
	    
	    //Start camera
	    VideoCapture camera = new VideoCapture(0);
		camera.open(0);
		    
	    if (camera.isOpened())
	    {
	    	//Add WindowListener to ImageDisplay
	    	while (true) //change to: while (frame exists)
	    	{
	    		camera.read(rawImage);
	    		
	    		if (!rawImage.empty())
	    		{
	    			newImage = detectFaces(rawImage, display);
	    			converter.setMatrix(newImage, ".jpg");
	    			convertedImage = converter.getBufferedImage();
	    			display.setImage(convertedImage);
	    			display.repaint();
	    		}
	    		else
	    		{
	    			break;
	    		}
	    	}
	    	//Return camera control to OS
	    	camera.release();
	    }
	}
	
	public Mat detectFaces(Mat image, ImageDisplay display) throws IOException
	{
		//Local variables
		MatOfRect faceDetections = new MatOfRect();
		Rect rectCrop = null;
		CascadeClassifier faceDetector = new CascadeClassifier("lbpcascade_frontalface.xml");
		
		//Detect faces in image
		faceDetector.detectMultiScale(image, faceDetections);
		
		//Set message text
		display.setMessage(faceDetections.toArray().length + " face(s) detected!", 50, 50);
		
		//Draws a rectangle around each detection
		for (Rect rect : faceDetections.toArray()) //for each rectangle
		{
			Imgproc.rectangle(image, new Point(rect.x, rect.y), 
					new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(255, 0, 255)); //Blue, Green, Red
			
			rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
			
			display.setMessage("Person Name" /*identifyFace(image)*/, rect.x + 8, rect.y);
		}
	
		//Save the detection
		if (!(rectCrop == null))
		{
			Mat croppedImage = new Mat(image, rectCrop);
			//Overwrite to save memory in current state
			Imgcodecs.imwrite("croppedImage.jpg", croppedImage);
		}

		return image;
	}
/*	
	public String identifyFace(BufferedImage image) throws IOException
	{
		File[] captures = new File("./Captures/").listFiles();
		for (int i = 0; i < captures.length; i++)
		{
			//find comparison algorithm
			//compare (image, captures[i]);
			
			if (percentSimilarity >= errorThreshold)
			{
				return fileName (trim file extension);
			}
		}
		
		return "???" //unidentified person
	}
*/
}