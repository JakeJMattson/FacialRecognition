/**
 * Class Description:
 * File IO - Save images to disk
 */

package facialrecognition;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public final class FileSaver
{
	private static String path;

	//Class constants
	private static final File DATABASE = new File("Database");
	private static final String EXTENSION = ".png";

	public static void setName(String name)
	{
		File destination;

		//Simplest file name
		File basic = new File(DATABASE + "/" + name + EXTENSION);

		if (!basic.exists())
			destination = basic;
		else
		{
			int index = 0;

			//Avoid overwriting files by adding numbers to a duplicate
			do
				destination = new File(DATABASE + "/" + name + " (" + index++ + ")" + EXTENSION);
			while (destination.exists());
		}

		path = destination.getAbsolutePath();
	}

	public static void save(Mat image)
	{
		if (path != null)
			Imgcodecs.imwrite(path, image);

		//Set path to null after saving to limit to 1 save
		path = null;
	}

	public static File[] getFiles()
	{
		//Create folder to store saved faces
		if (!DATABASE.exists())
			DATABASE.mkdir();

		return DATABASE.listFiles();
	}
}