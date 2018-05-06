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
	private static final File DATABASE = new File("Database");
	private static final String EXTENSION = ".png";
	private static String path;

	public static void setName(String name)
	{
		//Avoid overwriting files by adding numbers to a duplicate
		File destination;

		//Create potential files
		File basic = new File(DATABASE + "/" + name + EXTENSION);
		File firstIndexed = new File(DATABASE + "/" + name + " (1)" + EXTENSION);

		if (!basic.exists())
			destination = basic;
		else if (!firstIndexed.exists())
			destination = firstIndexed;
		else
		{
			File[] files = DATABASE.listFiles();
			String fileName = "";

			//Find last duplicate file name
			for (File file : files)
				if (file.getName().startsWith(name + " ("))
					fileName = file.getName();

			//Determine new file number
			String lastNum = fileName.substring(fileName.indexOf("(") + 1, fileName.indexOf(")"));
			int nextNum = Integer.parseInt(lastNum) + 1;
			String duplication = " (" + nextNum + ")";

			//Create new file
			destination = new File(DATABASE + "/" + name + duplication + EXTENSION);
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

	public static String getExtension()
	{
		return EXTENSION;
	}

	public static File[] getFiles()
	{
		//Create folder to store saved faces
		if (!DATABASE.exists())
			DATABASE.mkdir();

		return DATABASE.listFiles();
	}
}