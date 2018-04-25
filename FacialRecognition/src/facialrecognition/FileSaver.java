package facialrecognition;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public final class FileSaver
{
	private static final File DATABASE = new File("Captures");
	private static final String EXTENSION = ".png";
	private static String path;

	public static void setName(String name)
	{
		//Create folder to store saved faces
		if (!DATABASE.exists())
			DATABASE.mkdir();

		//Avoid overwriting files by adding a space to a repeated name
		File destination;
		boolean foundValidPath = false;

		do
		{
			destination = new File(DATABASE + "/" + name + EXTENSION);

			if (!destination.exists())
				foundValidPath = true;
			else
				name += " ";

		} while (!foundValidPath);

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
		return DATABASE.listFiles();
	}
}