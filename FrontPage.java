import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FrontPage extends JPanel implements ActionListener {
    private JButton startButton;
    private Image backgroundImage;

    public FrontPage() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // Load the background image
        backgroundImage = new ImageIcon(getClass().getResource("./picfront.jpeg")).getImage();

        startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.PLAIN, 24));
        startButton.setBackground(Color.YELLOW);
        startButton.setForeground(Color.BLACK);
        startButton.addActionListener(this);
        add(startButton, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            JFrame storyFrame = new JFrame("Story Page");
            storyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            storyFrame.add(new StoryPage());
            storyFrame.pack();
            storyFrame.setSize(800, 600); // Ensure the frame size is set
            storyFrame.setLocationRelativeTo(null);
            storyFrame.setVisible(true);
            ((JFrame) SwingUtilities.getWindowAncestor(this)).dispose();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("PAC MAN");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new FrontPage());
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
