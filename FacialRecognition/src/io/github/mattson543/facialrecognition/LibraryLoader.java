package io.github.mattson543.facialrecognition;

import java.io.*;
import java.nio.file.Files;

import javax.swing.*;

import org.opencv.core.Core;

/**
 * Load the OpenCV library.
 *
 * @author mattson543
 */
public final class LibraryLoader
{
	private LibraryLoader()
	{
		throw new IllegalStateException("Utility class");
	}

	//String constants
	private static final String NEWLINE = System.lineSeparator();
	private static final String INTERNAL_ERROR = "Internal Error";
	private static final String USER_ERROR = "Try that again...";

	/**
	 * Initiate OpenCV loading based on the launch environment.
	 *
	 * @return Whether or not OpenCV was loaded
	 */
	public static boolean loadLibrary()
	{
		//Determines launch environment (IDE or JAR)
		String environment = LibraryLoader.class.getResource("LibraryLoader.class").toString();

		//Control flow
		boolean isSuccessful = false;

		if (environment.startsWith("file"))
		{
			//Load library from user library
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			//Mark load as successful
			isSuccessful = true;
		}
		else if (environment.startsWith("jar") || environment.startsWith("rsrc"))
		{
			//Path to library
			String libraryPath;

			//File to store library path
			File pathFile = new File("Library Path.txt");

			if (pathFile.exists())
				//Get path from file
				libraryPath = readFile(pathFile);
			else
			{
				//Get path to library
				libraryPath = getLibraryPath();

				//Create file with path
				if (libraryPath != null)
					createFile(pathFile, libraryPath);
			}

			if (libraryPath != null)
				//Load library from file
				isSuccessful = load(libraryPath);
		}
		else
			System.out.println("Unrecognized load type!");

		return isSuccessful;
	}

	/**
	 * Attempt to read a file containing the full OpenCV path.
	 * Note: this will not exist on the first run.
	 *
	 * @param pathFile
	 *            Path where the file is expected to be
	 * @return Path from file (if read)
	 */
	private static String readFile(File pathFile)
	{
		//Path to library
		String libraryPath = null;

		try (BufferedReader reader = new BufferedReader(new FileReader(pathFile)))
		{
			//Get path from file
			libraryPath = reader.readLine().trim();
		}
		catch (IOException e)
		{
			//Construct error
			String title = INTERNAL_ERROR + " (BufferedReader Error)";
			String message = "Failed read path from file:" + NEWLINE
					+ pathFile.getAbsolutePath();

			displayError(title, message, e);
		}

		return libraryPath;
	}

	/**
	 * Find the path from the base OpenCV directory to the core library file
	 * based on the operating system.
	 *
	 * @return Library path
	 */
	private static String getLibraryPath()
	{
		//Get library path from user
		String opencvPath = createChooser();

		if (opencvPath == null)
			return null;

		//OS-specific variables
		String osName = System.getProperty("os.name");
		String javaDir = "", extension = "";
		boolean isValidOS = false;

		if (osName.startsWith("Windows"))
		{
			//Get system architecture
			int bitness = Integer.parseInt(System.getProperty("sun.arch.data.model"));

			//Get library path
			javaDir = opencvPath + "\\build\\java\\" + (bitness == 64 ? "x64" : "x86");

			//Set OS-specific library extension
			extension = ".dll";

			//Mark OS as valid
			isValidOS = true;
		}
		else if (osName.equals("Mac OS X"))
		{
			//OpenCV version folder
			String verison = new File(opencvPath).listFiles()[1].getAbsolutePath(); //Ignore .DS_Store at [0]

			//Get library path
			javaDir = verison + "/Share/OpenCV/Java/";

			//Set OS-specific library extension
			extension = ".dylib";

			//Mark OS as valid
			isValidOS = true;
		}
		else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix"))
		{
			//Get library path
			javaDir = opencvPath + "/build/lib/";

			//Set OS-specific library extension
			extension = ".so";

			//Mark OS as valid
			isValidOS = true;
		}
		else
			JOptionPane.showMessageDialog(null, "Unsupported Operating System", "Sorry...",
					JOptionPane.INFORMATION_MESSAGE);

		//Path determined to be library file
		String libraryPath = null;

		if (isValidOS)
		{
			File directory = new File(javaDir);

			if (directory.exists())
			{
				//Get all files in directory
				File[] files = directory.listFiles();

				//Find library file by extension
				for (File file : files)
					if (file.getName().endsWith(extension))
					{
						libraryPath = file.getAbsolutePath();
						break;
					}

				if (libraryPath == null)
				{
					//Construct error
					String title = USER_ERROR + " (Selection Error)";
					String message = "Could not find library file in directory:" + NEWLINE
							+ directory.getAbsolutePath() + NEWLINE
							+ "that contained the proper extension ("
							+ extension + ")";

					displayError(title, message, new Exception());
				}
			}
			else
			{
				//Construct error
				String title = USER_ERROR + " (Selection Error)";
				String message = "Failed to find directory:" + NEWLINE
						+ directory.getAbsolutePath();

				displayError(title, message, new Exception());
			}
		}

		return libraryPath;
	}

	/**
	 * Create a file chooser to let the user select the base OpenCV folder.
	 *
	 * @return OpenCV path
	 */
	private static String createChooser()
	{
		//Create file chooser
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select the OpenCV folder");

		//Display file chooser
		int buttonPressed = chooser.showOpenDialog(null);

		//Get selected path
		String path = null;
		if (buttonPressed == JFileChooser.APPROVE_OPTION)
			path = chooser.getSelectedFile().getAbsolutePath();

		return path;
	}

	/**
	 * Write the OpenCV path to a file for future launches.
	 *
	 * @param pathFile
	 *            File to contain the path
	 * @param libraryPath
	 *            Full path to OpenCV library
	 */
	private static void createFile(File pathFile, String libraryPath)
	{
		try
		{
			Files.write(pathFile.toPath(), libraryPath.getBytes());
		}
		catch (IOException e)
		{
			//Construct error
			String title = INTERNAL_ERROR + " (File write Error)";
			String message = "Failed write path to file:" + NEWLINE
					+ pathFile.getAbsolutePath();

			displayError(title, message, e);
		}
	}

	/**
	 * Load the OpenCV library into the system.
	 *
	 * @param libraryPath
	 *            Full path to OpenCV library
	 * @return Whether or not the operation was successful
	 */
	private static boolean load(String libraryPath)
	{
		File libraryFile = new File(libraryPath);
		File tempFile = null;

		//Create temporary file
		try
		{
			String name = libraryFile.getName();
			String extension = name.substring(name.indexOf('.'));
			tempFile = File.createTempFile("lib", extension);
		}
		catch (IOException e)
		{
			//Construct error
			String title = INTERNAL_ERROR + " (File Error)";
			String message = "Failed to create temporary file!";

			displayError(title, message, e);

			//Stop processing
			return false;
		}

		//Write library data to file
		try (FileInputStream in = new FileInputStream(libraryFile.getAbsolutePath());
				OutputStream out = new FileOutputStream(tempFile))
		{
			in.transferTo(out);
		}
		catch (IOException e)
		{
			//Construct error
			String title = INTERNAL_ERROR + " (Stream Error)";
			String message = "Failed to transfer library ("
					+ libraryFile.getAbsolutePath()
					+ ") to temporary file ("
					+ tempFile.getAbsolutePath()
					+ ")";

			displayError(title, message, e);

			//Stop processing
			return false;
		}

		//Load library
		System.load(tempFile.toString());

		//Assume successful load if statement reached
		return true;
	}

	private static void displayError(String title, String message, Exception e)
	{
		//Create dialog box
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
	}
}