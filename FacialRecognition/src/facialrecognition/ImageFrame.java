/**
 * Class Description:
 * Frame - GUI container for components (holds ImagePanel)
 */

package facialrecognition;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import javax.swing.*;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

@SuppressWarnings("serial")
public class ImageFrame extends JFrame implements ActionListener
{
	private boolean isOpen;
	private Color color;

	//Components
	private ImagePanel imagePanel;
	private JTextField txtFileName;
	private JButton btnSaveFile;
	private JButton btnSetColor;
	private JComboBox<String> colorDropDown;

	//Class constants
	private final Color DEFAULT_COLOR = Color.BLUE;

	public ImageFrame()
	{
		color = DEFAULT_COLOR;
		buildGUI();
	}

	private void buildGUI()
	{
		//Set frame preferences
		addWindowListener(createWindowListener());
		setTitle("Facial Recognition");
		setLayout(new BorderLayout());

		//Create panel for image
		imagePanel = new ImagePanel();

		//Add panels to frame
		add("Center", imagePanel);
		add("South", createToolbarPanel());

		//Show frame
		setVisible(true);
		isOpen = true;
	}

	private WindowListener createWindowListener()
	{
		WindowListener listener = new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent windowClosed)
			{
				isOpen = false;
			}
		};

		return listener;
	}

	private JPanel createToolbarPanel()
	{
		//Create panels
		JPanel toolbarPanel = new JPanel(new FlowLayout());
		JPanel savePanel = createSavePanel();
		JPanel colorPanel = createColorPanel();

		//Combine panels
		toolbarPanel.add(savePanel);
		toolbarPanel.add(colorPanel);

		return toolbarPanel;
	}

	private JPanel createSavePanel()
	{
		//Create panels
		JPanel namePanel = new JPanel(new GridLayout(0, 2));
		JPanel savePanel = new JPanel(new FlowLayout());
		savePanel.setBorder(BorderFactory.createLineBorder(Color.black));

		//Create GUI components
		JLabel lblFileName = new JLabel("Name of person in frame: ");
		txtFileName = new JTextField("");
		btnSaveFile = new JButton("Save Face");
		btnSaveFile.addActionListener(this);

		//Add components to panel
		namePanel.add(lblFileName);
		namePanel.add(txtFileName);
		savePanel.add(namePanel);
		savePanel.add(btnSaveFile);

		return savePanel;
	}

	private JPanel createColorPanel()
	{
		//Create panel
		JPanel colorPanel = new JPanel();
		colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		//Drop down options
		String[] colors = {"BLUE", "CYAN", "GREEN", "MAGENTA", "ORANGE", "RED"};

		//Instantiate GUI components
		colorDropDown = new JComboBox<>();
		btnSetColor = new JButton("Set Color");
		btnSetColor.addActionListener(this);

		for (String color : colors)
			colorDropDown.addItem(color);

		//Add components to panel
		colorPanel.add(colorDropDown);
		colorPanel.add(btnSetColor);

		return colorPanel;
	}

	public boolean isOpen()
	{
		return isOpen;
	}

	public Scalar getTextColor()
	{
		return new Scalar(color.getBlue(), color.getGreen(), color.getRed());
	}

	public void showImage(Mat image)
	{
		//Send image to panel
		imagePanel.setImage(convertMatToImage(image));

		//Resize frame to fit image
		pack();
	}

	public BufferedImage convertMatToImage(Mat matrix)
	{
		//Get image dimensions
		int width = matrix.width();
		int height = matrix.height();

		//Determine image type
		int type;

		if (matrix.channels() != 1)
		{
			type = BufferedImage.TYPE_3BYTE_BGR;
			Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2RGB);
		}
		else
			type = BufferedImage.TYPE_BYTE_GRAY;

		//Get matrix data
		byte[] data = new byte[width * height * (int) matrix.elemSize()];
		matrix.get(0, 0, data);

		//Create image with matrix data
		BufferedImage out = new BufferedImage(width, height, type);
		out.getRaster().setDataElements(0, 0, width, height, data);

		return out;
	}

	@Override
	public void actionPerformed(ActionEvent click)
	{
		if (click.getSource() == btnSetColor)
			try
			{
				//Get color from string name
				Field field = Class.forName("java.awt.Color").getField((String) colorDropDown.getSelectedItem());
				color = (Color) field.get(null);
			}
			catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException
					| IllegalAccessException e)
			{
				color = DEFAULT_COLOR;
				e.printStackTrace();
			}
		else if (click.getSource() == btnSaveFile)
			FileSaver.setName(txtFileName.getText());
	}
}