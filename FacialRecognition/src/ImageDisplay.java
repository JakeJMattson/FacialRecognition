import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageDisplay extends JPanel
{
	private BufferedImage image;

	public ImageDisplay()
	{
		super();
	}

	public void setImage(BufferedImage image)
	{
		//Save image
		this.image = image;

		//Draw image onto panel
		this.repaint();
	}

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