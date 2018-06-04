package io.github.mattson543.facialrecognition;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Panel - holds image to display in GUI
 *
 * @author mattson543
 */
@SuppressWarnings("serial")
public class ImagePanel extends JPanel
{
	/**
	 * Image to be displayed to the user
	 */
	private BufferedImage image;

	public ImagePanel()
	{
		super();
	}

	public void setImage(BufferedImage image)
	{
		this.image = image;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		if (image != null)
		{
			//Get image dimensions
			int width = image.getWidth();
			int height = image.getHeight();

			//Draw image onto panel
			g.drawImage(image, 0, 0, width, height, null);

			//Set panel size
			setPreferredSize(new Dimension(width, height));
		}
	}
}