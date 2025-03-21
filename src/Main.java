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


/**
 * Hauptklasse der Anwendung zum Anzeigen und Bearbeiten von PNG-Bildern.
 *
 * Diese Klasse erstellt das Hauptfenster der Anwendung und initialisiert
 * die Benutzeroberfläche, einschließlich Menüleiste, Zoom-Funktionen,
 * Farbschieber und Bildanzeige.
 * 
 * Author: Fynn
 * Datum: 17.03.2025
 * Version: 1.0
 */

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

    /**
     * Initialisiert die Benutzeroberfläche der Anwendung.
     * 
     * Beschreibt alle Parameter für die Anwending, wie Name grösse Usw.
     * 
     * Ausserdem auch die Verschiedenen Buttons und Sliders und ihre Position.
     */

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

        JButton resetColorButton = new JButton("Reset Color");
        resetColorButton.addActionListener(new ResetColorListener());
        menuBar.add(resetColorButton);

        // Create color sliders
        redSlider = new JSlider(-255, 255, 0);
        greenSlider = new JSlider(-255, 255, 0);
        blueSlider = new JSlider(-255, 255, 0);

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

    /**
     * Listener zum Öffnen eines PNG-Bildes.
     *
     * Diese Klasse implementiert den ActionListener, um auf das Drücken der
     * "Open PNG"-Schaltfläche zu reagieren. Sie öffnet einen Dateiauswahldialog,
     * um ein PNG-Bild auszuwählen und anzuzeigen.
     */
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

    /**
     * Listener zum Vergrößern des Bildes.
     *
     * Diese Klasse implementiert den ActionListener, um das Bild zu vergrößern,
     * wenn die "Zoom In"-Schaltfläche gedrückt wird.
     */

    private class ZoomInListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            zoomFactor *= 1.1;
            updateZoomedImage();
        }
    }

    /**
     * Listener zum Verkleinern des Bildes.
     *
     * Diese Klasse implementiert den ActionListener, um das Bild zu verkleinern,
     * wenn die "Zoom Out"-Schaltfläche gedrückt wird.
     */

    private class ZoomOutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            zoomFactor /= 1.1;
            updateZoomedImage();
        }
    }

    private class ResetColorListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            redSlider.setValue(0);
            greenSlider.setValue(0);
            blueSlider.setValue(0);
        }
    }

    /**
     * Listener zum Umwandeln des Bildes in Graustufen.
     *
     * Diese Klasse implementiert den ActionListener, um das Bild in Graustufen
     * umzuwandeln, wenn die "Grayscale"-Schaltfläche gedrückt wird.
     */

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

    /**
     * Listener für Farbschieber-Änderungen.
     *
     * Diese Klasse implementiert den ChangeListener, um Änderungen an den
     * Farbschiebern zu erkennen und die Farbwerte des Bildes entsprechend
     * anzupassen.
     */
    private class ColorSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (originalImage != null && !isGrayscale) {
                applyColorAdjustments();
            }
        }
    }

    /**
     * Passt die Farbwerte des Bildes basierend auf den Schieberwerten an.
     *
     * Diese Methode erstellt ein neues Bild, in dem die Farbwerte jedes Pixels
     * basierend auf den aktuellen Werten der Farbschieber angepasst werden.
     */
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
                newRed = Math.max(0, newRed);
                int newGreen = Math.min(255, color.getGreen() + greenValue);
                newGreen = Math.max(0, newGreen);
                int newBlue = Math.min(255, color.getBlue() + blueValue);
                newBlue = Math.max(0, newBlue);

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

    /**
     * Ein Record zur Speicherung der RGB-Werte und Position eines Pixels.
     *
     * @param red Der Rotwert des Pixels.
     * @param green Der Grünwert des Pixels.
     * @param blue Der Blauwert des Pixels.
     * @param x Die x-Koordinate des Pixels.
     * @param y Die y-Koordinate des Pixels.
     */
    public record Pixel(int red, int green, int blue, int x, int y) {
    }
}
