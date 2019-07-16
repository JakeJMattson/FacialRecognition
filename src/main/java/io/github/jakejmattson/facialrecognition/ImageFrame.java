package io.github.jakejmattson.facialrecognition;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Frame - GUI container for components (holds ImagePanel).
 *
 * @author JakeJMattson
 */
class ImageFrame
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
        imagePanel = new ImagePanel();
        isOpen = true;

        frame = new JFrame("Facial Recognition");
        frame.addWindowListener(createWindowListener());
        frame.setLayout(new BorderLayout());
        frame.add("Center", imagePanel);
        frame.add("South", createToolbarPanel());
        frame.setVisible(true);
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
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS));

        toolbarPanel.add(createSavePanel());
        toolbarPanel.add(Box.createHorizontalGlue());
        toolbarPanel.add(createColorPanel());

        return toolbarPanel;
    }

    /**
     * Create a panel to display saving options to the user.
     *
     * @return Panel
     */
    private JPanel createSavePanel()
    {
        JPanel savePanel = new JPanel();
        savePanel.setBorder(BorderFactory.createLineBorder(Color.black));

        txtFileName = new JTextField(20);
        JButton btnSaveFile = new JButton("Save Face");
        btnSaveFile.addActionListener(actionEvent -> shouldSave = true);

        JPanel namePanel = new JPanel();
        namePanel.add(new JLabel("Name of person in frame: "));
        namePanel.add(txtFileName);
        savePanel.add(namePanel, btnSaveFile);

        return savePanel;
    }

    /**
     * Create a panel to display color options to the user.
     *
     * @return Panel
     */
    private JPanel createColorPanel()
    {
        JPanel colorPanel = new JPanel();
        colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        JComboBox<String> colorDropDown = new JComboBox<>();
        String[] colorOptions = {"BLUE", "CYAN", "GREEN", "MAGENTA", "ORANGE", "RED"};
        Arrays.stream(colorOptions).forEach(colorDropDown::addItem);

        colorDropDown.addActionListener(actionEvent -> {
            try
            {
                Field field = Color.class.getField((String) Objects.requireNonNull(colorDropDown.getSelectedItem()));
                color = (Color) field.get(null);
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                color = DEFAULT_COLOR;
            }
        });

        colorPanel.add(colorDropDown);

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
        imagePanel.setImage(convertMatToImage(image));
        frame.repaint();
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
        int width = matrix.width();
        int height = matrix.height();
        int type = matrix.channels() != 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;

        if (type == BufferedImage.TYPE_3BYTE_BGR)
            Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2RGB);

        byte[] data = new byte[width * height * (int) matrix.elemSize()];
        matrix.get(0, 0, data);

        BufferedImage out = new BufferedImage(width, height, type);
        out.getRaster().setDataElements(0, 0, width, height, data);

        return out;
    }
}