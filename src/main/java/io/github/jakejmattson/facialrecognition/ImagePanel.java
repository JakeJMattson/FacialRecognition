package io.github.jakejmattson.facialrecognition;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Panel - holds image to display in GUI.
 *
 * @author JakeJMattson
 */
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

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		if (image != null)
		{
			g.drawImage(image, 0, 0, null);
			setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		}
	}
}