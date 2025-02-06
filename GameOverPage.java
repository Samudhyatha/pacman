import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOverPage extends JPanel {
    private Image image;
    private int score;
    private JButton nextButton;

    public GameOverPage(int score) {
        this.score = score;
        if (score > 10) {
            image = new ImageIcon(getClass().getResource("./winnerpage.jpg")).getImage();
            nextButton = new JButton("Next Level");
            nextButton.setBounds(550, 500, 200, 100);
            nextButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Code to navigate to the next level
                    System.out.println("Next Level button clicked");
                    // Implement navigation to the next level here
                    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(GameOverPage.this);
                    topFrame.getContentPane().removeAll();
                    topFrame.getContentPane().add(new NextLevel());
                    topFrame.revalidate();
                    topFrame.repaint();
                }
            });
            setLayout(null);
            add(nextButton);
        } else {
            image = new ImageIcon(getClass().getResource("./loser.jpg")).getImage();
        }
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, getWidth() / 2 - 80, getHeight() - 50);
    }
}
