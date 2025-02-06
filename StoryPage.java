import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.net.URL;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class StoryPage extends JPanel {
    private JButton playGameButton;
    private JFXPanel jfxPanel;

    public StoryPage() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        Platform.runLater(() -> {
            try {
                // Load and display the video
                URL videoURL = getClass().getResource("/comicvideo.mp4");
                if (videoURL != null) {
                    Media media = new Media(videoURL.toExternalForm());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    MediaView mediaView = new MediaView(mediaPlayer);
                    mediaView.setFitWidth(jfxPanel.getWidth());
                    mediaView.setFitHeight(jfxPanel.getHeight());
                    mediaView.setPreserveRatio(true);
                    Scene scene = new Scene(new javafx.scene.Group(mediaView));
                    jfxPanel.setScene(scene);
                    mediaPlayer.play();
                } else {
                    JLabel errorLabel = new JLabel("Video not found", SwingConstants.CENTER);
                    errorLabel.setForeground(Color.WHITE);
                    add(errorLabel, BorderLayout.CENTER);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JLabel errorLabel = new JLabel("Error loading video", SwingConstants.CENTER);
                errorLabel.setForeground(Color.WHITE);
                add(errorLabel, BorderLayout.CENTER);
            }
        });

        playGameButton = new JButton("Play Game");
        playGameButton.setFont(new Font("Arial", Font.PLAIN, 24));
        playGameButton.setBackground(Color.YELLOW);
        playGameButton.setForeground(Color.BLACK);
        playGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame gameFrame = new JFrame("PAC MAN");
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gameFrame.add(new PacMan());
                gameFrame.pack();
                gameFrame.setLocationRelativeTo(null);
                gameFrame.setVisible(true);
                ((JFrame) SwingUtilities.getWindowAncestor(StoryPage.this)).dispose();
            }
        });
        add(playGameButton, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Story Page");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new StoryPage());
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
