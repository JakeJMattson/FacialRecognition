import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageDisplay extends JPanel
{
	private BufferedImage image;
	private List<String> messages = new ArrayList<String>();
	private List<Integer> positions = new ArrayList<Integer>();
	
	public ImageDisplay()
	{
		super(); //JFrame
	}
	
	//Image and messages are both set before use, but independently
	public void setImage(BufferedImage image)
	{
		this.image = image;
	}
	
	public void setMessage(String text, int xPosition, int yPosition)
	{
		messages.add(text);
		positions.add(xPosition);
		positions.add(yPosition);
	}
	
	//Add content to frame
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (image == null)
		{
			return;
		}
		
		//Add image
		g.drawImage(image, 10, 10, image.getWidth(), image.getHeight(), null);
		
		//Add text
		g.setFont(new Font("ariel", 2, 20));
		g.setColor(Color.MAGENTA);
		
		for (int i = 0; i < positions.size(); i += 2)
		{
			g.drawString(messages.get((i/2)), positions.get(i), positions.get(i + 1));
		}
		
		//Reset lists after use
		messages = new ArrayList<String>();
		positions = new ArrayList<Integer>();
	}
}