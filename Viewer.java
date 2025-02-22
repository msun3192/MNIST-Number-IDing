// Matthew Sun and Sean Nayebi
// Algorithms
// May 30, 2024
import java.util.concurrent.CountDownLatch;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Viewer {

    private Attributes attributes;  // Invocation parameters
    private final Image[] images;   // List of images to display
    private Image image;            // Current image being displayed
    private int current;            // Index of current image


    // -- Attributes (invocation parameters) -------------------------------------------

    public static class Attributes {

        private boolean showLabels = true;  // Show the image label (which digit)
        private boolean classify = false;   // Show the classify (labeling) buttons
        private int labelFontSize = 50;     // Font size for digit labels
        private int idFontSize = 20;        // Font size for image numbers
        private int scale = 15;             // Scaling factor (in pixels)
        private int sleep = 0;              // Milliseconds to wait between display updates

        public void showLabels(boolean show) {
            this.showLabels = show;
        }

        public void showLabels() {
            this.showLabels = true;
        }

        public void hideLabels() {
            this.showLabels = false;
        }

        public void showClassify(boolean show) {
            this.classify = show;
        }

        public void showClassify() {
            this.classify = true;
        }

        public void hideClassify() {
            this.classify = false;
        }

        public void scale(int scale) {
            this.scale = scale;
        }

        public void idFontSize(int size) {
            this.idFontSize = size;
        }

        public void labelFontSize(int size) {
            this.labelFontSize = size;
        }

        public void sleep(int milliseconds) {
            this.sleep = milliseconds;
        }
    }


    // -- Buttons ----------------------------------------------------------------------

    private static abstract class SimpleButton extends JButton implements ActionListener {
        public SimpleButton(String label) {
            super(label);
            this.addActionListener(this);
        }

        public abstract void actionPerformed(ActionEvent event);
    }

    private SimpleButton firstButton = new SimpleButton("First") {
        @Override
        public void actionPerformed(ActionEvent event) {
            current = 1;
            display(current);
        }
    };

    private SimpleButton lastButton = new SimpleButton("Last") {
        @Override
        public void actionPerformed(ActionEvent event) {
            current = images.length-1;
            display(current);
        }
    };

    private SimpleButton nextButton = new SimpleButton("Next") {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (current < images.length-1) {
                display(++current);
            }
        }
    };

    private SimpleButton prevButton = new SimpleButton("Prev") {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (current > 0) {
                display(--current);
            }
        }
    };

    private SimpleButton pauseButton = new SimpleButton("Pause") {

        private boolean running = true;

        @Override
        public void actionPerformed(ActionEvent event) {
            if (running) {
                this.setText("Resume");
                animator.pause();
                running = false;
            } else {
                this.setText("Pause");
                animator.resume();
                running = true;
            }
        }
    };

    private JButton doneButton = new SimpleButton("Done") {
        @Override
        public void actionPerformed(ActionEvent event) {
            done.countDown();
            frame.dispose();
        }
    };


    private class DigitButton extends SimpleButton {

        private int digit;

        public DigitButton(int digit) {
            super(Integer.toString(digit));
            this.digit = digit;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            image.label(digit);
            if (current < images.length-1) {
                display(++current);
            }
        }
    }

    // -- Labels -----------------------------------------------------------------------

    private static class CenteredLabel extends JLabel {
        public CenteredLabel(int fontsize) {
            super("0");
            Font font = this.getFont();
            this.setFont(new Font(font.getName(), Font.BOLD, fontsize));
            this.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            this.setAlignmentY(JLabel.CENTER_ALIGNMENT);
        }
    }

    private class ImageID extends CenteredLabel {
        public ImageID() {
            super(attributes.idFontSize);
        }

        public void setID(int id) {
            if (attributes.classify) {
                this.setText(String.format("Cluster #%d", id));
            } else {
                this.setText(String.format("Image #%d", id));
            }
        }
    }

    private class ImageLabel extends CenteredLabel {
        public ImageLabel() {
            super(attributes.labelFontSize);
        }

        public void setLabel(int label) {
            this.setText(Integer.toString(label));
        }
    }

    // -- Image Panel ------------------------------------------------------------------

    private class ImagePanel extends JPanel {

        private Image image;
        private final int scale;
        private final int height;
        private final int width;

        public ImagePanel(int rows, int columns, int scale) {
            this.scale = scale;
            this.height = scale * rows;
            this.width = scale * columns;
            this.setPreferredSize(new Dimension(width, height));
        }

        public void setImage(Image image) {
            this.image = image;
        }

        @Override
        public void paint(Graphics g) {
            int offset = (this.getWidth() - this.width) / 2;
            for (int row = 0; row < image.rows(); row++) {
                for (int col = 0; col < image.columns(); col++) {
                    g.setColor(grayToColor(image.get(row, col)));
                    g.fillRect(offset + col*scale, row*scale, scale, scale);
                }
            }
        }

        private static Color grayToColor(int shade) {
            shade = 255 - shade;
            return new Color(shade, shade, shade);
        }
    }

    // -- GUI Constructor --------------------------------------------------------------

    private JFrame     frame;
    private ImageID    imageID;
    private ImageLabel imageLabel;
    private ImagePanel imagePanel;
    private Animator   animator;

    private Viewer(Image[] images, String title, Attributes attributes) {
        this.attributes = attributes;
        this.image = images[0];
        this.images = images;
        this.current = 0;

        this.frame = new JFrame(title);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int rows = Image.rows(images);
        int cols = Image.columns(images);
        this.imageID = new ImageID();
        this.imageLabel = new ImageLabel();
        this.imagePanel = new ImagePanel(rows, cols, attributes.scale);
        this.imagePanel.setImage(this.image);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        if (attributes.showLabels && !attributes.classify) {
            labelPanel.add(this.imageLabel);
        }
        labelPanel.add(this.imageID);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(firstButton);
        buttonPanel.add(nextButton);
        if (attributes.classify) {
            buttonPanel.add(doneButton);
        } else if(attributes.sleep > 0) {
            buttonPanel.add(pauseButton);
        }
        buttonPanel.add(prevButton);
        buttonPanel.add(lastButton);

        JPanel digitPanel = new JPanel();
        digitPanel.setLayout(new GridLayout(2, 5));
        digitPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        digitPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        for (int digit = 0; digit < 10; digit++) {
            digitPanel.add(new DigitButton(digit));
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        panel.add(imagePanel);
        panel.add(labelPanel);
        panel.add(buttonPanel);
        if (attributes.classify) panel.add(digitPanel);
        panel.doLayout();

        Container content = frame.getContentPane();
        content.add(panel);
        frame.pack();
        frame.setVisible(true);

        if (attributes.sleep > 0) {
            pauseButton.requestFocus();
        } else {
            nextButton.requestFocus();
        }

        display(0);
        if (attributes.sleep > 0) {
            this.animator = new Animator(attributes.sleep);
        }
    }

    // -- Display Updater --------------------------------------------------------------

    private void display(Image image) {
        this.image = image;
        this.imagePanel.setImage(image);
        this.imageID.setID(image.id());
        this.imageLabel.setLabel(image.label());
        this.frame.update(frame.getGraphics());
        // frame.update();
    }

    private void display(int index) {
        this.current = index;
        display(this.images[index]);
    }

    private void display() {
        display(this.current);
    }

    private class Animator implements ActionListener {

        private Timer timer;

        public Animator(int speed) {
            this.timer = new Timer(speed, this);
            this.timer.setInitialDelay(2*speed);
            this.timer.start();
        }

        public void pause() {
            this.timer.stop();
        }

        public void resume() {
            this.timer.restart();
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (current < images.length-1) {
                display(++current);
            } else {
                // Change the label on the pause button
                pauseButton.actionPerformed(null);
                this.timer.stop();
            }
        }
    }


    private static CountDownLatch done = new CountDownLatch(1);

    public static void invoke(Image[] images, String title, Attributes attributes) {
        try {
            SwingUtilities.invokeAndWait(
                    new Runnable() {
                        @Override
                        public void run() {
                            new Viewer(images, title, attributes);
                        }
                    });
            done.await();

        } catch (InterruptedException e) {
            // Ignore

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
