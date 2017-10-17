import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageDisplay extends JPanel
{
	private BufferedImage image;
	private List<String> messages = new ArrayList<String>();
	private List<Integer> positions = new ArrayList<Integer>();
	private Color textColor;
	
	public ImageDisplay()
	{
		super(); //JPanel
	}
	
	//Image and message are both set before use, but independently
	public void setImage(BufferedImage image)
	{
		this.image = image;
		this.setSize(image.getWidth(), image.getHeight());
	}
	
	public void setMessage(String text, int xPosition, int yPosition, Color color)
	{
		messages.add(text);
		positions.add(xPosition);
		positions.add(yPosition);
		textColor = color;
	}
	
	//Add content to frame
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (image != null)
		{
			//Add image
			g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
			
			//Set text preferences
			g.setFont(new Font("ariel", 2, 20));
			g.setColor(textColor);
			
			//Add text
			for (int i = 0; i < positions.size(); i += 2)
			{
				g.drawString(messages.get((i/2)), positions.get(i), positions.get(i + 1));
			}
			
			//Reset lists after use
			messages = new ArrayList<String>();
			positions = new ArrayList<Integer>();
		}
	}
}