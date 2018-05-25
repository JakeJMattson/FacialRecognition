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
	/**
	 * Valid path to new File
	 */
	private static String path;

	/**
	 * Directory on the disk containing faces
	 */
	private static final File DATABASE = new File("Database");

	/**
	 * Converts a base (person) name to a non-duplicate file name.
	 *
	 * @param name
	 *            Name of the person in the image
	 */
	public static void setName(String name)
	{
		File destination;
		String extension = ".png";

		//Simplest file name
		File basic = new File(DATABASE + "/" + name + extension);

		if (!basic.exists())
			destination = basic;
		else
		{
			int index = 0;

			//Avoid overwriting files by adding numbers to a duplicate
			do
				destination = new File(DATABASE + "/" + name + " (" + index++ + ")" + extension);
			while (destination.exists());
		}

		path = destination.getAbsolutePath();
	}

	/**
	 * Attempt to save a file to the database.
	 *
	 * @param image
	 *            Image to be written to the file
	 */
	public static void save(Mat image)
	{
		if (path != null)
			Imgcodecs.imwrite(path, image);

		//Set path to null after saving to limit to 1 save
		path = null;
	}

	/**
	 * Get an array of all files within the database.
	 *
	 * @return Files
	 */
	public static File[] getFiles()
	{
		//Create folder to store saved faces
		if (!DATABASE.exists())
			DATABASE.mkdir();

		return DATABASE.listFiles();
	}
}