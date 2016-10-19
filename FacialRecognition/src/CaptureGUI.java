import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class CaptureGUI extends JFrame
						implements ActionListener
{
	private boolean openStatus;
	private Color color;
	private final Color DEFAULT_COLOR = Color.BLUE;
	private JComboBox<String> colorDropDown = new JComboBox<String>();
	private JTextField txtFileName;
	private JButton btnSaveFile;
	private JButton btnSetColor;
	
	public CaptureGUI(ImageDisplay display)
	{
		this.color = DEFAULT_COLOR;
		FileSaver.initialSetup();
		buildGUI(display);
	}

	private void buildGUI(ImageDisplay display)
	{
		setPreferences();
		setLayout(new BorderLayout());
		
		JPanel toolbarPanel = createToolbarPanel();
		
		add("Center", display);
		add("South", toolbarPanel);
		
		setVisible(true);
	    openStatus = true;
	}
	
	private void setPreferences()
	{
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    setTitle("Facial Recognition");
	    setSize(656, 560);
	    addWindowListener(new WindowAdapter() 
	    {
	        @Override
	        public void windowClosing(WindowEvent windowClosed) 
	        {
	        	openStatus = false;
	            dispose();
	        }
	    });
	}

	private JPanel createToolbarPanel()
	{
		JPanel toolbarPanel = new JPanel();
		JPanel savePanel = createSavePanel();
		JPanel colorPanel = createColorPanel();
		
		toolbarPanel.add(savePanel);
		toolbarPanel.add(colorPanel);
		toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		return toolbarPanel;
	}
	
	private JPanel createSavePanel()
	{
		//Create panel
		JPanel savePanel = new JPanel();
		
		//Create GUI components
		JLabel lblFileName;
		JLabel lblExtension;
		
		//Instantiate GUI components
		lblFileName  = new JLabel("Name of person in frame: ");
		txtFileName  = new JTextField("");
		btnSaveFile  = new JButton("Save Face");
		lblExtension = new JLabel(FileSaver.getExtension());
		
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
		for (int i = 0; i < colors.length; i++)
			colorDropDown.addItem(colors[i]);
		
		btnSetColor = new JButton("Set Color");
		
		btnSetColor.addActionListener(this);
		
		//Add components to panel		
		colorPanel.add(colorDropDown);
		colorPanel.add(btnSetColor);
		
		return colorPanel;
	}

	public boolean getStatus()
	{
		return this.openStatus;
	}

	public Color getTextColor()
	{
		return this.color;
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
				color = (Color)field.get(null);
			}
			catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException | IllegalAccessException e)
			{
				color = DEFAULT_COLOR;
				e.printStackTrace();
			}
		}
		
		else if (click.getSource() == btnSaveFile)
			FileSaver.setName(txtFileName.getText());
	}
}