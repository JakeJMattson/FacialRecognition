import java.io.File;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public final class FileSaver
{
	private static final String FOLDER_NAME = "Captures";
	private static File databasePath = new File(FOLDER_NAME);
	private static final String EXTENSION = ".png";
	private static String path;
	
	public static void setName(String personName)
	{
		setValidPath(personName);
	}
	
	private static void setValidPath(String name)
	{
		//Create folder to store saved faces
		if (!databasePath.exists())
			databasePath.mkdir();
		
		//Avoid overwriting files by adding a space to a repeated name
		while ((new File(databasePath + "/" + name + EXTENSION)).exists())
		{
			name += " ";
		}
		path = databasePath + "/" + name + EXTENSION;
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
		return databasePath.listFiles();
	}
}