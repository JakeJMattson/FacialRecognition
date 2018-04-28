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
public class ImageFrame extends JFrame
		implements ActionListener
{
	private boolean isOpen;
	private Color color;

	private ImagePanel imagePanel;
	private JTextField txtFileName;
	private JButton btnSaveFile;
	private JButton btnSetColor;

	private final Color DEFAULT_COLOR = Color.BLUE;
	private final JComboBox<String> colorDropDown = new JComboBox<>();

	public ImageFrame()
	{
		color = DEFAULT_COLOR;
		buildGUI();
	}

	private void buildGUI()
	{
		setTitle("Facial Recognition");
		addWindowListener(createWindowListener());
		setLayout(new BorderLayout());

		imagePanel = new ImagePanel();

		add("Center", imagePanel);
		add("South", createToolbarPanel());

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
		JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel savePanel = createSavePanel();
		JPanel colorPanel = createColorPanel();

		//Combine panels
		toolbarPanel.add(savePanel);
		toolbarPanel.add(colorPanel);

		return toolbarPanel;
	}

	private JPanel createSavePanel()
	{
		//Create panel
		JPanel savePanel = new JPanel();
		savePanel.setBorder(BorderFactory.createLineBorder(Color.black));

		//Create GUI components
		JLabel lblFileName = new JLabel("Name of person in frame: ");
		JLabel lblExtension = new JLabel(FileSaver.getExtension());

		//Instantiate GUI components
		txtFileName = new JTextField("");
		btnSaveFile = new JButton("Save Face");

		txtFileName.setPreferredSize(new Dimension(150, 24));
		btnSaveFile.addActionListener(this);

		//Add components to panel
		savePanel.add(lblFileName);
		savePanel.add(txtFileName);
		savePanel.add(lblExtension);
		savePanel.add(btnSaveFile);

		return savePanel;
	}

	private JPanel createColorPanel()
	{
		//Create panel
		JPanel colorPanel = new JPanel();

		//Local variables
		String[] colors = {"BLUE", "CYAN", "GREEN", "MAGENTA", "ORANGE", "RED"};

		//Instantiate GUI components
		for (String color : colors)
			colorDropDown.addItem(color);

		btnSetColor = new JButton("Set Color");
		btnSetColor.addActionListener(this);

		colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));

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
		BufferedImage convertedImage = convertMatToImage(image);

		//Send image to panel
		imagePanel.setImage(convertedImage);

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
		{
			Field field;
			try
			{
				//Get color from string name
				field = Class.forName("java.awt.Color").getField((String) colorDropDown.getSelectedItem());
				color = (Color) field.get(null);
			}
			catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException
					| IllegalAccessException e)
			{
				color = DEFAULT_COLOR;
				e.printStackTrace();
			}
		}

		else if (click.getSource() == btnSaveFile)
			FileSaver.setName(txtFileName.getText());
	}
}