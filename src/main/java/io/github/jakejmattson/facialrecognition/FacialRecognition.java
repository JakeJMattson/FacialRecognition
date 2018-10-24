/**
 * The MIT License
 * Copyright © 2016 Jake Mattson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/*
 * Project Description:
 * Facial detection and recognition using OpenCV.
 */

package io.github.jakejmattson.facialrecognition;

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
		Loader.load(opencv_java.class);
		capture();
		System.exit(0);
	}

	private static void capture()
	{
		File classifier = new File("lbpcascade_frontalface_improved.xml");

		if (!classifier.exists())
		{
			displayFatalError("Unable to find classifier!");
			return;
		}

		CascadeClassifier faceDetector = new CascadeClassifier(classifier.toString());
		VideoCapture camera = new VideoCapture(0);

		if (!camera.isOpened())
		{
			displayFatalError("No camera detected!");
			return;
		}

		if (!DATABASE.exists())
			DATABASE.mkdir();

		ImageFrame frame = new ImageFrame();

		while (frame.isOpen() && camera.isOpened())
		{
			Mat rawImage = new Mat();
			camera.read(rawImage);
			Mat newImage = detectFaces(rawImage, faceDetector, frame);
			frame.showImage(newImage);
		}

		camera.release();
	}

	private static Mat detectFaces(Mat image, CascadeClassifier faceDetector, ImageFrame frame)
	{
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		Rect[] faces = faceDetections.toArray();
		boolean shouldSave = frame.shouldSave();
		String name = frame.getFileName();
		Scalar color = frame.getTextColor();

		for (Rect face : faces)
		{
			Mat croppedImage = new Mat(image, face);

			if (shouldSave)
				saveImage(croppedImage, name);

			Imgproc.putText(image, "ID: " + identifyFace(croppedImage), face.tl(), Font.BOLD, 1.5, color);
			Imgproc.rectangle(image, face.tl(), face.br(), color);
		}

		int faceCount = faces.length;
		String message = faceCount + (faceCount == 1 ? "face" : "faces") + " detected!";
		Imgproc.putText(image, message, new Point(3, 25), Font.BOLD, 2, color);

		return image;
	}

	private static String identifyFace(Mat image)
	{
		String faceID = "";
		int errorThreshold = 3;
		int mostSimilar = 0;

		for (File capture : Objects.requireNonNull(DATABASE.listFiles()))
		{
			int similarities = compareFaces(image, capture.getAbsolutePath());

			if (similarities > mostSimilar)
			{
				mostSimilar = similarities;
				faceID = capture.getName();
			}
		}

		if (mostSimilar > errorThreshold)
		{
			String delimiter = faceID.contains(" (") ? "(" : ".";
			faceID = faceID.substring(0, faceID.indexOf(delimiter)).trim();
		}
		else
			faceID = "???";

		return faceID;
	}

	private static int compareFaces(Mat currentImage, String fileName)
	{
		Mat compareImage = Imgcodecs.imread(fileName);
		ORB orb = ORB.create();
		int similarity = 0;

		MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
		MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
		orb.detect(currentImage, keypoints1);
		orb.detect(compareImage, keypoints2);

		Mat descriptors1 = new Mat();
		Mat descriptors2 = new Mat();
		orb.compute(currentImage, keypoints1, descriptors1);
		orb.compute(compareImage, keypoints2, descriptors2);

		if (descriptors1.cols() == descriptors2.cols())
		{
			MatOfDMatch matchMatrix = new MatOfDMatch();
			DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
			matcher.match(descriptors1, descriptors2, matchMatrix);
			DMatch[] matches = matchMatrix.toArray();

			for (DMatch match : matches)
				if (match.distance <= 50)
					similarity++;
		}

		return similarity;
	}

	private static void saveImage(Mat image, String name)
	{
		File destination;
		String extension = ".png";
		String baseName = DATABASE + File.separator + name;
		File basic = new File(baseName + extension);

		if (!basic.exists())
			destination = basic;
		else
		{
			int index = 0;

			do
				destination = new File(baseName + " (" + index++ + ")" + extension);
			while (destination.exists());
		}

		Imgcodecs.imwrite(destination.toString(), image);
	}

	private static void displayFatalError(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
	}
}