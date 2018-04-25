import java.awt.Font;
import java.io.File;

import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class FacialRecognition
{
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
		CascadeClassifier faceDetector = new CascadeClassifier("lbpcascade_frontalface_improved.xml");

		//Create display
		DisplayFrame frame = new DisplayFrame();

		//Start camera
		VideoCapture camera = new VideoCapture(0);

		//While frame is not closed
		while (frame.isOpen() && camera.isOpened())
		{
			Mat rawImage = new Mat();
			camera.read(rawImage);

			if (rawImage.empty())
				break;

			Mat newImage = detectFaces(rawImage, faceDetector, frame.getTextColor());

			frame.showImage(newImage);
		}

		//Return camera control to OS
		camera.release();
	}

	public Mat detectFaces(Mat image, CascadeClassifier faceDetector, Scalar color)
	{
		//Detect faces in image
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		Rect[] faces = faceDetections.toArray();

		for (Rect face : faces)
		{
			//Crop image to detection
			Mat croppedImage = new Mat(image, face);

			//Attempt to save face
			FileSaver.save(croppedImage);

			//Add ID above detection
			Imgproc.putText(image, "ID: " + identifyFace(croppedImage), face.tl(), Font.BOLD, 1.5, color);

			//Draw rectangle around the detection
			Imgproc.rectangle(image, face.tl(), face.br(), color);
		}

		//Display number of detections
		int faceCount = faces.length;
		String message = faceCount + (faceCount == 1 ? "face" : "faces") + " detected!";
		Imgproc.putText(image, message, new Point(3, 25), Font.BOLD, 2, color);

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
		File[] captures = FileSaver.getFiles();

		//Check files for matches
		for (File capture : captures)
		{
			//Calculate similarity between face on screen and face in database
			similarities = compareFaces(image, capture.getAbsolutePath());

			//Find most similar face in list
			if (similarities > mostSimilar)
			{
				mostSimilar = similarities;

				//Get name from file
				faceID = capture.getName();
			}
		}

		//Margin of error
		if (mostSimilar > errorThreshold)
			faceID = faceID.substring(0, faceID.indexOf(".")).trim();
		else
			faceID = "???";

		return faceID;
	}

	@SuppressWarnings("deprecation")
	public int compareFaces(Mat currentImage, String fileName)
	{
		//Local variables
		int similarity = 0;

		//Images to compare
		Mat compareImage = Imgcodecs.imread(fileName);

		FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
		DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		MatOfKeyPoint keypoints1 = new MatOfKeyPoint(), keypoints2 = new MatOfKeyPoint();
		Mat descriptors1 = new Mat(), descriptors2 = new Mat();

		//Detect key points
		detector.detect(currentImage, keypoints1);
		detector.detect(compareImage, keypoints2);
		extractor.compute(currentImage, keypoints1, descriptors1);
		extractor.compute(compareImage, keypoints2, descriptors2);

		if (descriptors2.cols() == descriptors1.cols())
		{
			//Check matches of key points
			MatOfDMatch matches = new MatOfDMatch();
			DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
			matcher.match(descriptors1, descriptors2, matches);
			DMatch[] match = matches.toArray();

			//Determine similarity
			for (int i = 0; i < descriptors1.rows(); i++)
				if (match[i].distance <= 50)
					similarity++;
		}

		return similarity;
	}
}