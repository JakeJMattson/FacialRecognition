/*Ideas and Notes:
 * Log processed image information into .txt files. 
 * Image wouldn't need to be processed every frame.
 * Compare speed: file reading to image processing.
 * 23.373 ms vs. xx.xxx ms over 1000 frames.
 * (Test incomplete)
 * 
 * Alter similarity threshold to increase accuracy.
 */

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class FacialRecognition
{
	File[] captures = new File("Captures").listFiles();
	
	public static void main(String[] args)
	{	    
		FacialRecognition driver = new FacialRecognition();
		driver.start();
	}
	
	public void start()
	{
		//Load OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		ImageDisplay displayFrame = createDisplayFrame();
		capture(displayFrame);
		
		System.exit(0);
	}
	
	private ImageDisplay createDisplayFrame()
	{
		//Local variables
		JFrame frame = new JFrame();
	    ImageDisplay display = new ImageDisplay();
	    
	    //Set frame preferences
	    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    frame.setTitle("Facial Recognition");
	    frame.setSize(656, 519);
	    frame.addWindowListener(new WindowAdapter() 
	    {
	        @Override
	        public void windowClosing(WindowEvent windowClosed) 
	        {
	            display.setStatus(false);
	            frame.dispose();
	        }
	    });
	    frame.setContentPane(display);
	    frame.setVisible(true);
	    
	    return display;
	}
		
	public void capture(ImageDisplay display)
	{
		//Local variables
	    Mat rawImage = new Mat();
	    Mat newImage = new Mat();
	    BufferedImage convertedImage;
	    MatToImg converter = new MatToImg();
	    
	    //Start camera
	    VideoCapture camera = new VideoCapture(0);

	    //While frame is not closed
    	while (display.getStatus())
    	{
    		camera.read(rawImage);
			newImage = detectFaces(rawImage, display);
			converter.setMatrix(newImage, ".png");
			convertedImage = converter.getBufferedImage();
			display.setImage(convertedImage);
			display.repaint();
    	}
    	
    	//Return camera control to OS
    	camera.release();
	}
	
 	public Mat detectFaces(Mat image, ImageDisplay display)
	{
		//Local variables
		CascadeClassifier faceDetector = new CascadeClassifier("lbpcascade_frontalface.xml");
		MatOfRect faceDetections = new MatOfRect();
		Rect faceRect = null;
		Mat croppedImage;
		
		//Detect faces in image
		faceDetector.detectMultiScale(image, faceDetections);
		
		//Set message text
		display.setMessage(faceDetections.toArray().length + " face(s) detected!", 50, 50);
		
		//Draws a rectangle around each detection
		for (Rect rect : faceDetections.toArray())
		{			
			faceRect = new Rect(rect.x, rect.y, rect.width, rect.height);
			
			if (!(faceRect == null))
			{
				//Crop image to detection and save
				croppedImage = new Mat(image, faceRect);
				Imgcodecs.imwrite("croppedImage.png", croppedImage);
				
				//Add ID above detection
				display.setMessage("ID: " + identifyFace(croppedImage), rect.x + 8, rect.y - 1);
			}
			
			//Draw rectangle onto image
			Imgproc.rectangle(image, new Point(rect.x, rect.y), 
					new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(255, 0, 255)); //Blue, Green, Red
		}
		return image;
	}

	public String identifyFace(Mat image)
	{		
		//Local variables
		String name;
		String faceID = "???";
		int similarities = 0;
		
		//Check files for matches
		for (int i = 0; i < captures.length; i++)
		{
			//Get name from file
			name = captures[i].getName();
			name = name.substring(0, name.indexOf(".")).trim();
			try
			{
				similarities = compareFaces(image, captures[i].getCanonicalPath());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			//Margin of error
			if (similarities > 20)
			{
				faceID = name;
				break;
			}
		}
		return faceID;
	}

	public int compareFaces(Mat image, String fileName)
	{
		//Local variables
		int similarity = 0;
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
		DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
		MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
		Mat descriptors1 = new Mat();
		Mat descriptors2 = new Mat();
		DMatch[] match;
		
		//Images to compare
		Mat currentImage = image;
		Mat compareImage = Imgcodecs.imread(fileName);
		
		//Detect key points
		detector.detect(currentImage, keypoints1);
		detector.detect(compareImage, keypoints2);
		extractor.compute(currentImage, keypoints1, descriptors1);
		extractor.compute(compareImage, keypoints2, descriptors2);
		
		//Match points
		MatOfDMatch matches = new MatOfDMatch();
		  
		if (descriptors2.cols() == descriptors1.cols()) 
		{
			matcher.match(descriptors1, descriptors2, matches);
			 
			//Check matches of key points
			match = matches.toArray();

			//Determine similarity
			for (int i = 0; i < descriptors1.rows(); i++) 
			{
				//at 10, Lena != Lena
				//at 100, face == Lena
				if (match[i].distance <= 45) 
				{
					System.out.println(match[i].distance);
					similarity++;
				}
				else
					System.out.println(match[i].distance);
			}
		}
		return similarity;
	}
}