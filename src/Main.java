import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main extends JFrame {
    private JLabel imageLabel;
    private JScrollPane scrollPane;
    private Pixel[][] pixels;
    private BufferedImage originalImage;
    private BufferedImage grayscaleImage;
    private BufferedImage modifiedImage;
    private boolean isGrayscale = false;
    private double zoomFactor = 1.0;
    private JSlider redSlider;
    private JSlider greenSlider;
    private JSlider blueSlider;

    public Main() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("PNG Image Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);
        setLocationRelativeTo(null);

        // Create menu bar and open button
        JMenuBar menuBar = new JMenuBar();
        JButton openButton = new JButton("Open PNG");
        openButton.addActionListener(new OpenImageListener());
        menuBar.add(openButton);

        // Create zoom buttons
        JButton zoomInButton = new JButton("Zoom In");
        zoomInButton.addActionListener(new ZoomInListener());
        menuBar.add(zoomInButton);

        JButton zoomOutButton = new JButton("Zoom Out");
        zoomOutButton.addActionListener(new ZoomOutListener());
        menuBar.add(zoomOutButton);

        // Create grayscale and original buttons
        JButton grayscaleButton = new JButton("Grayscale");
        grayscaleButton.addActionListener(new GrayscaleListener());
        menuBar.add(grayscaleButton);

        JButton originalButton = new JButton("Original");
        originalButton.addActionListener(new OriginalListener());
        menuBar.add(originalButton);

        // Create color sliders
        redSlider = new JSlider(0, 255, 0);
        greenSlider = new JSlider(0, 255, 0);
        blueSlider = new JSlider(0, 255, 0);

        redSlider.addChangeListener(new ColorSliderListener());
        greenSlider.addChangeListener(new ColorSliderListener());
        blueSlider.addChangeListener(new ColorSliderListener());

        JPanel sliderPanel = new JPanel();
        sliderPanel.add(new JLabel("Red:"));
        sliderPanel.add(redSlider);
        sliderPanel.add(new JLabel("Green:"));
        sliderPanel.add(greenSlider);
        sliderPanel.add(new JLabel("Blue:"));
        sliderPanel.add(blueSlider);

        // Create image display area
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        scrollPane = new JScrollPane(imageLabel);
        scrollPane.setVisible(false);

        // Add components to frame
        setJMenuBar(menuBar);
        add(sliderPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private class OpenImageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(".png") || f.isDirectory();
                }

                public String getDescription() {
                    return "PNG Images (*.png)";
                }
            });

            int result = fileChooser.showOpenDialog(Main.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    originalImage = ImageIO.read(selectedFile);
                    if (originalImage != null) {
                        isGrayscale = false;
                        displayImage(originalImage);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Main.this,
                            "Error loading image: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class ZoomInListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            zoomFactor *= 1.1;
            updateZoomedImage();
        }
    }

    private class ZoomOutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            zoomFactor /= 1.1;
            updateZoomedImage();
        }
    }

    private class GrayscaleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            isGrayscale = true;
            createGrayscaleImage();
            updateZoomedImage();
        }
    }

    private class OriginalListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            isGrayscale = false;
            updateZoomedImage();
        }
    }

    private class ColorSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (originalImage != null && !isGrayscale) {
                applyColorAdjustments();
            }
        }
    }

    private void applyColorAdjustments() {
        int redValue = redSlider.getValue();
        int greenValue = greenSlider.getValue();
        int blueValue = blueSlider.getValue();

        modifiedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < originalImage.getHeight(); y++) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                int rgb = originalImage.getRGB(x, y);
                Color color = new Color(rgb, true);

                int newRed = Math.min(255, color.getRed() + redValue);
                int newGreen = Math.min(255, color.getGreen() + greenValue);
                int newBlue = Math.min(255, color.getBlue() + blueValue);

                Color newColor = new Color(newRed, newGreen, newBlue, color.getAlpha());
                modifiedImage.setRGB(x, y, newColor.getRGB());
            }
        }

        updateZoomedImage();
    }

    private void createGrayscaleImage() {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = originalImage.getRGB(x, y);
                Color color = new Color(rgb, true);

                int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                Color grayColor = new Color(gray, gray, gray);

                grayscaleImage.setRGB(x, y, grayColor.getRGB());
            }
        }
    }

    private void updateZoomedImage() {
        if (originalImage != null) {
            BufferedImage imageToDisplay = isGrayscale ? grayscaleImage : (modifiedImage != null ? modifiedImage : originalImage);
            int width = (int) (imageToDisplay.getWidth() * zoomFactor);
            int height = (int) (imageToDisplay.getHeight() * zoomFactor);
            Image scaledImage = imageToDisplay.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon imageIcon = new ImageIcon(scaledImage);
            imageLabel.setIcon(imageIcon);
            scrollPane.setVisible(true);
            revalidate();
            repaint();
        }
    }

    private void displayImage(BufferedImage image) {
        ImageIcon imageIcon = new ImageIcon(image);
        imageLabel.setIcon(imageIcon);
        scrollPane.setVisible(true);
        revalidate();
        repaint();

        // Create a 2D array to store Pixel objects
        int width = image.getWidth();
        int height = image.getHeight();
        pixels = new Pixel[width][height];

        // Populate the 2D array with Pixel objects
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb, true);
                pixels[x][y] = new Pixel(color.getRed(), color.getGreen(), color.getBlue(), x, y);
            }
        }

        // Display the original image
        updateZoomedImage();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }

    // Pixel class to store RGB values and position
    private static class Pixel {
        private int red;
        private int green;
        private int blue;
        private int x;
        private int y;

        public Pixel(int red, int green, int blue, int x, int y) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.x = x;
            this.y = y;
        }

        public int getRed() {
            return red;
        }

        public int getGreen() {
            return green;
        }

        public int getBlue() {
            return blue;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
