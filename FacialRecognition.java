import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Core;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

@SuppressWarnings("serial")
public class FacialRecognition extends JFrame 
{
	JLabel lblPicture;
	int startingNumber;
	int runningShift;
	
	public static void main(String[] args) throws InterruptedException, IOException 
	{	    
		FacialRecognition driver = new FacialRecognition();
		driver.start();
	}
	
	public void start() throws InterruptedException
	{
		ImageDisplay displayFrame;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		displayFrame = createDisplayFrame();
		
		try
		{
			capture(displayFrame);
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private ImageDisplay createDisplayFrame()
	{
		JFrame frame = new JFrame();
	    ImageDisplay display = new ImageDisplay();
	    
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setTitle("Facial Detection");
	    frame.setSize(680, 540);
	    frame.setContentPane(display);
	    frame.setVisible(true);
	    
	    return display;
	}
		
	public void capture(ImageDisplay display) throws InterruptedException, IOException
	{
	    Mat rawImage = new Mat();
	    Mat newImage = new Mat();
	    BufferedImage convertedImage;
	    MatToImg converter = new MatToImg();
	    
	    VideoCapture camera = new VideoCapture(0);
		camera.open(0);
		    
	    if (camera.isOpened())
	    {
	    	while (true)
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
	    			System.out.println("Camera error!");
	    			break;
	    		}
	    	}
	    	camera.release();
	    }
	}
	
	public Mat detectFaces(Mat image, ImageDisplay display) throws IOException
	{
		CascadeClassifier faceDetector = new CascadeClassifier("lbpcascade_frontalface.xml");
		Rect rectCrop = null;
		
		// Detect faces in the image.
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		
		display.setText(String.format("%s face(s) detected!", faceDetections.toArray().length));
		
		// Draw a bounding box around each face.
		for (Rect rect : faceDetections.toArray()) 
		{
			Imgproc.rectangle(image, new Point(rect.x, rect.y), 
					new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(255, 0, 255)); //Blue, Green, Red
			
			rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
		}
		
		// Save the visualized detection.
		if (!(rectCrop == null))
		{
			Mat croppedImage = new Mat(image, rectCrop);
			Imgcodecs.imwrite("croppedImage.jpg", croppedImage);
		}

		//identifyFace();
		return image;
	}
/*	
	public void identifyFace() throws IOException
	{
		File[] captures = new File("./Captures/").listFiles();
		for (int i = 0; i < captures.length; i++)
		{
        Mat srcImgMat = Imgcodecs.imread(sourcePath);
        System.out.println("Loaded image at " + sourcePath);
        MatOfKeyPoint matOfKeyPoints = new MatOfKeyPoint();
        FeatureDetector blobDetector = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        blobDetector.detect(srcImgMat, matOfKeyPoints);
        System.out.println("Detected " + matOfKeyPoints.size()+ " blobs in the image");
        List<KeyPoint> keyPoints = matOfKeyPoints.toList();
	}
	*/
}