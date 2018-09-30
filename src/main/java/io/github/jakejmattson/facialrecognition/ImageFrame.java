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
package io.github.jakejmattson.facialrecognition;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Frame - GUI container for components (holds ImagePanel).
 *
 * @author JakeJMattson
 */
class ImageFrame implements ActionListener
{
	/**
	 * Whether or not the frame is currently open
	 */
	private boolean isOpen;
	/**
	 * Whether or not the face should be saved to the disk
	 */
	private boolean shouldSave;
	/**
	 * Color to draw components (boxes, text, etc) in
	 */
	private Color color;
	/**
	 * Panel to hold/display a BufferedImage
	 */
	private ImagePanel imagePanel;

	private JFrame frame;
	private JTextField txtFileName;
	private JButton btnSaveFile;
	private JButton btnSetColor;
	private JComboBox<String> colorDropDown;

	//Class constants
	private static final Color DEFAULT_COLOR = Color.BLUE;

	ImageFrame()
	{
		color = DEFAULT_COLOR;
		buildGUI();
	}

	/**
	 * Construct the display and its children.
	 */
	private void buildGUI()
	{
		frame = new JFrame("Facial Recognition");

		//Set frame preferences
		frame.addWindowListener(createWindowListener());
		frame.setLayout(new BorderLayout());

		//Create panel for image
		imagePanel = new ImagePanel();

		//Add panels to frame
		frame.add("Center", imagePanel);
		frame.add("South", createToolbarPanel());

		//Show frame
		frame.setVisible(true);
		isOpen = true;
	}

	/**
	 * Create a listener to monitor the frame closing event.
	 *
	 * @return WindowListener
	 */
	private WindowListener createWindowListener()
	{
		return new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent windowClosed)
			{
				isOpen = false;
			}
		};
	}

	/**
	 * Create a panel to hold all non-image display elements.
	 *
	 * @return Panel
	 */
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

	/**
	 * Create a panel to display saving options to the user.
	 *
	 * @return Panel
	 */
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

	/**
	 * Create a panel to display color options to the user.
	 *
	 * @return Panel
	 */
	private JPanel createColorPanel()
	{
		//Create panel
		JPanel colorPanel = new JPanel();
		colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		//Drop down options
		String[] colorOptions = {"BLUE", "CYAN", "GREEN", "MAGENTA", "ORANGE", "RED"};

		//Instantiate GUI components
		colorDropDown = new JComboBox<>();
		btnSetColor = new JButton("Set Color");
		btnSetColor.addActionListener(this);

		for (String option : colorOptions)
			colorDropDown.addItem(option);

		//Add components to panel
		colorPanel.add(colorDropDown);
		colorPanel.add(btnSetColor);

		return colorPanel;
	}

	/**
	 * Externally called to see if display frame is still open.
	 *
	 * @return Open status
	 */
	boolean isOpen()
	{
		return isOpen;
	}

	/**
	 * Return whether or not the face in frame should be saved to the disk.
	 * Set the state to false.
	 *
	 * @return state
	 */
	boolean shouldSave()
	{
		boolean prevState = shouldSave;
		shouldSave = false;
		return prevState;
	}

	/**
	 * Get the name of the person in frame (user input).
	 *
	 * @return name
	 */
	String getFileName()
	{
		return txtFileName.getText();
	}

	/**
	 * Return the selected text color as an OpenCV Scalar.
	 *
	 * @return Scalar
	 */
	Scalar getTextColor()
	{
		return new Scalar(color.getBlue(), color.getGreen(), color.getRed());
	}

	/**
	 * Display an image in the frame.
	 *
	 * @param image
	 * 		Image to be shown
	 */
	void showImage(Mat image)
	{
		//Send image to panel
		imagePanel.setImage(convertMatToImage(image));

		//Redraw frame
		frame.repaint();

		//Resize frame to fit image
		frame.pack();
	}

	/**
	 * Convert an OpenCV Mat to a Java BufferedImage.
	 *
	 * @param matrix
	 * 		OpenCV Mat
	 *
	 * @return BufferedImage
	 */
	private static BufferedImage convertMatToImage(Mat matrix)
	{
		//Get image dimensions
		int width = matrix.width();
		int height = matrix.height();

		int type = matrix.channels() != 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;

		if (type == BufferedImage.TYPE_3BYTE_BGR)
			Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2RGB);

		//Get matrix data
		byte[] data = new byte[width * height * (int) matrix.elemSize()];
		matrix.get(0, 0, data);

		//Create image with matrix data
		BufferedImage out = new BufferedImage(width, height, type);
		out.getRaster().setDataElements(0, 0, width, height, data);

		return out;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener
	 * #actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent click)
	{
		Object src = click.getSource();

		if (src == btnSetColor)
			try
			{
				//Get color from string name
				Field field = Color.class.getField((String) Objects.requireNonNull(colorDropDown.getSelectedItem()));
				color = (Color) field.get(null);
			}
			catch (NoSuchFieldException | IllegalAccessException e)
			{
				color = DEFAULT_COLOR;
			}
		else if (src == btnSaveFile)
			shouldSave = true;
	}
}