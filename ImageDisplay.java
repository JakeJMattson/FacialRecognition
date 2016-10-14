import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageDisplay extends JPanel
{
	private BufferedImage image;
	private String text;
	
	public ImageDisplay()
	{
		super(); //JFrame
	}
	
	//Image and text are both set before use, but not at the same time
	public void setImage(BufferedImage image)
	{
		this.image = image;
	}
	
	public void setText(String text)
	{
		this.text = text;
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
		g.drawString(text, 50, 50);
	}
}