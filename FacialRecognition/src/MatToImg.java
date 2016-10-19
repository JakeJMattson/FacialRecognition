import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class MatToImg
{
	Mat matrix;
	MatOfByte byteMatrix;
	String fileExtension;
		
	public BufferedImage getBufferedImage()
	{
		Imgcodecs.imencode(fileExtension, matrix, byteMatrix);
		byte[] byteArray = byteMatrix.toArray();
		BufferedImage image = null;
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
			image = ImageIO.read(in);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return image;
	}

	public void setMatrix(Mat matrix, String fileExtension)
	{
		this.matrix = matrix;
		this.fileExtension = fileExtension;
		this.byteMatrix = new MatOfByte();
	}
}