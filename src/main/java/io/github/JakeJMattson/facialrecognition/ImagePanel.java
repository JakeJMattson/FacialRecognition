package io.github.JakeJMattson.facialrecognition;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Panel - holds image to display in GUI.
 *
 * @author JakeJMattson
 */
@SuppressWarnings("serial")
class ImagePanel extends JPanel
{
	/**
	 * Image to be displayed to the user
	 */
	private BufferedImage image;

	ImagePanel()
	{
		super();
	}

	void setImage(BufferedImage image)
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
			//Draw image onto panel
			g.drawImage(image, 0, 0, null);

			//Set panel size
			setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		}
	}
}