/*
 * Project Description:
 * Facial detection and recognition using OpenCV.
 */

package io.github.JakeJMattson.facialrecognition;

import org.bytedeco.javacpp.*;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.swing.JOptionPane;
import java.awt.Font;
import java.io.File;
import java.util.Objects;

/**
 * Demo (main) class to run functions.
 *
 * @author JakeJMattson
 */
class FacialRecognition
{
	/**
	 * Directory on the disk containing faces
	 */
	private static final File DATABASE = new File("Database");

	public static void main(String[] args)
	{
		//Load OpenCV
		Loader.load(opencv_java.class);

		//Start program
		new FacialRecognition().capture();

		//Force exit
		System.exit(0);
	}

	private void capture()
	{
		File classifier = new File("lbpcascade_frontalface_improved.xml");

		if (!classifier.exists())
		{
			displayFatalError("Unable to find classifier!");
			return;
		}

		//Load classifier to detect faces
		CascadeClassifier faceDetector = new CascadeClassifier(classifier.toString());

		//Start camera
		VideoCapture camera = new VideoCapture(0);

		//Do not start if no camera is available
		if (!camera.isOpened())
		{
			displayFatalError("No camera detected!");
			return;
		}

		//Create folder to store saved faces
		if (!DATABASE.exists())
			DATABASE.mkdir();

		//Create display
		ImageFrame frame = new ImageFrame();

		while (frame.isOpen() && camera.isOpened())
		{
			//Get image from camera
			Mat rawImage = new Mat();
			camera.read(rawImage);

			//Detect and label faces
			Mat newImage = detectFaces(rawImage, faceDetector, frame);

			//Display result
			frame.showImage(newImage);
		}

		//Return camera control to OS
		camera.release();
	}

	private Mat detectFaces(Mat image, CascadeClassifier faceDetector, ImageFrame frame)
	{
		//Detect faces in image
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		Rect[] faces = faceDetections.toArray();
		boolean shouldSave = frame.shouldSave();
		String name = frame.getFileName();
		Scalar color = frame.getTextColor();

		for (Rect face : faces)
		{
			//Crop image to detection
			Mat croppedImage = new Mat(image, face);

			//Save face image to disk
			if (shouldSave)
				saveImage(croppedImage, name);

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

	private String identifyFace(Mat image)
	{
		//Local variables
		String faceID = "";
		int errorThreshold = 3, mostSimilar = 0;

		//Check files for matches
		for (File capture : Objects.requireNonNull(DATABASE.listFiles()))
		{
			//Calculate similarity between face on screen and face in database
			int similarities = compareFaces(image, capture.getAbsolutePath());

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
		{
			String delimiter = faceID.contains(" (") ? "(" : ".";
			faceID = faceID.substring(0, faceID.indexOf(delimiter)).trim();
		}
		else
			faceID = "???";

		return faceID;
	}

	private int compareFaces(Mat currentImage, String fileName)
	{
		//Local variables
		int similarity = 0;

		//Image to compare
		Mat compareImage = Imgcodecs.imread(fileName);

		//Create key point detector and descriptor extractor
		ORB orb = ORB.create();

		//Detect key points
		MatOfKeyPoint keypoints1 = new MatOfKeyPoint(), keypoints2 = new MatOfKeyPoint();
		orb.detect(currentImage, keypoints1);
		orb.detect(compareImage, keypoints2);

		//Extract descriptors
		Mat descriptors1 = new Mat(), descriptors2 = new Mat();
		orb.compute(currentImage, keypoints1, descriptors1);
		orb.compute(compareImage, keypoints2, descriptors2);

		if (descriptors1.cols() == descriptors2.cols())
		{
			//Check matches of key points
			MatOfDMatch matchMatrix = new MatOfDMatch();
			DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
			matcher.match(descriptors1, descriptors2, matchMatrix);
			DMatch[] matches = matchMatrix.toArray();

			//Determine similarity
			for (DMatch match : matches)
				if (match.distance <= 50)
					similarity++;
		}

		return similarity;
	}

	private void saveImage(Mat image, String name)
	{
		File destination;
		String extension = ".png";
		String baseName = DATABASE + File.separator + name;

		//Simplest file name
		File basic = new File(baseName + extension);

		if (!basic.exists())
			destination = basic;
		else
		{
			int index = 0;

			//Avoid overwriting files by adding numbers to a duplicate
			do
				destination = new File(baseName + " (" + index++ + ")" + extension);
			while (destination.exists());
		}

		Imgcodecs.imwrite(destination.toString(), image);
	}

	private void displayFatalError(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
	}
}