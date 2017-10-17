import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class FacialRecognition
{
	File[] captures;
	
	public static void main(String[] args)
	{
		FacialRecognition driver = new FacialRecognition();
		driver.start();
	}
	
	public void start()	
	{
		//Load OpenCV
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//Begin Capture
		capture();
		
		//Exit
		System.exit(0);
	}
	
	public void capture()
	{
		//Local variables
	    Mat rawImage = new Mat(), newImage = new Mat();
	    BufferedImage convertedImage;
	    ImageDisplay display = new ImageDisplay();
		CaptureGUI frame = new CaptureGUI(display);
		CascadeClassifier faceDetector = new CascadeClassifier("lbpcascade_frontalface_improved.xml");
	    
	    //Start camera
	    VideoCapture camera = new VideoCapture(0);
	    camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
	    camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
	    
		//Wait for camera to get images
	    while (rawImage.empty())
	    {
			camera.read(rawImage);
	    }
	    
	    //While frame is not closed
    	while (frame.getStatus())
    	{
    		camera.read(rawImage);
			newImage = detectFaces(rawImage, frame, display, faceDetector);
			Imgproc.cvtColor(newImage, newImage, Imgproc.COLOR_BGR2RGB);
			convertedImage = convertMatToImage(newImage);
			display.setImage(convertedImage);
			display.repaint();
    	}
    	
    	//Return camera control to OS
    	camera.release();
	}
	
	public BufferedImage convertMatToImage(Mat matrix)
	{
		//Local variables
        int width = matrix.width();
        int height = matrix.height();
        int type = BufferedImage.TYPE_BYTE_GRAY;
        byte[] data = new byte[width * height * (int)matrix.elemSize()];
        BufferedImage out;
        
        //Get matrix data
        matrix.get(0, 0, data);

        //Determine type
        if(matrix.channels() != 1)
            type = BufferedImage.TYPE_3BYTE_BGR;

        //Create image and pass matrix data
        out = new BufferedImage(width, height, type);
        out.getRaster().setDataElements(0, 0, width, height, data);
        
        return out;	
	}
	
 	public Mat detectFaces(Mat image, CaptureGUI frame, ImageDisplay display, CascadeClassifier faceDetector)
	{
		//Local variables
		MatOfRect faceDetections = new MatOfRect();
		Rect faceRect = null;
		Mat croppedImage;
		Color color = frame.getTextColor();
		Scalar currentColor = new Scalar(color.getBlue(), color.getGreen(), color.getRed());
		
		//Detect faces in image
		faceDetector.detectMultiScale(image, faceDetections);
		
		//Set message text
		display.setMessage(faceDetections.toArray().length + " face(s) detected!", 50, 50, color);
		
		//Draws a rectangle around each detection
		for (Rect rect : faceDetections.toArray())
		{			
			faceRect = new Rect(rect.x, rect.y, rect.width, rect.height);
			
			if (faceRect != null)
			{
				//Crop image to detection
				croppedImage = new Mat(image, faceRect);
				
				//Attempt to save face
				FileSaver.save(croppedImage);
				
				//Add ID above detection
				display.setMessage("ID: " + identifyFace(croppedImage), rect.x + 8, rect.y - 1, color);
			}
			
			//Draw detection
			Imgproc.rectangle(image, new Point(rect.x, rect.y), 
					new Point(rect.x + rect.width, rect.y + rect.height),
					currentColor);
		}
		return image;
	}

	public String identifyFace(Mat image)
	{		
		//Local variables
		String faceID = "";
		int errorThreshold = 3;
		int similarities = 0;
		int mostSimilar = 0;
		
		//Refresh files
		captures = FileSaver.getFiles();
		
		//Check files for matches
		for (int i = 0; i < captures.length; i++)
		{			
			try
			{
				//Calculate similarity between face on screen and face in database
				similarities = compareFaces(image, captures[i].getCanonicalPath());
				
				//Find most similar face in list
				if (similarities > mostSimilar)
				{
					mostSimilar = similarities;
					
					//Get name from file
					faceID = captures[i].getName();
					faceID = faceID.substring(0, faceID.indexOf(".")).trim();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		//Margin of error
		if (mostSimilar < errorThreshold)
			faceID = "???";
		
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
			//Check matches of key points
			matcher.match(descriptors1, descriptors2, matches);
			match = matches.toArray();
			
			//Determine similarity
			for (int i = 0; i < descriptors1.rows(); i++) 
			{
				//at 10, Lena != Lena
				//at 100, face == Lena
				if (match[i].distance <= 40)
					similarity++;
			}
		}
		return similarity;
	}
}